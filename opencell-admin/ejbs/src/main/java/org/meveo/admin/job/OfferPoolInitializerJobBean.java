package org.meveo.admin.job;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.async.OfferPoolInitializerAsync;
import org.meveo.admin.async.SubListCreator;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.job.logging.JobLoggingInterceptor;
import org.meveo.interceptor.PerformanceInterceptor;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.shared.DateUtils;
import org.meveo.security.MeveoUser;
import org.slf4j.Logger;

/**
 * @author Mounir BOUKAYOUA
 */
@Stateless
public class OfferPoolInitializerJobBean extends BaseJobBean {

    private static final String DATE_PATTERN = "MM/yyyy";

//    private static final int TERMINATION_DELAY = 90; // 3 months

//    //Old version
//    private static final String FROM_CLAUSE = "from billing_subscription sub \n"
//            + "inner join cat_offer_template offer on sub.offer_id = offer.id \n"
//            + "where cast(offer.cf_values as json)#>>'{sharingLevel, 0, string}' = 'OF' \n"
//            + "and sub.status='ACTIVE' \n"
//            + "and sub.subscription_date <= :counterEndDate \n"
//            + "and (sub.termination_date is null or (sub.termination_date - INTERVAL ':termination_delay DAYS') >= :counterStartDate ) \n"
//            + "group by sub.user_account_id, offerId \n"
//            + "having count(sub.id) > 0";

    private static final String FROM_CLAUSE = "from\n"
            + "( select sub.user_account_id,\n"
            + "    sub.offer_id,\n"
            + "    case\n"
            + "        when sub.cardWithExemption is not null and sub.cardWithExemption=true then sub.exemptionEndDate\n"
            + "        else sub.subscription_date\n"
            + "    end as activated_at\n"
            + "    from billing_service_instance si\n"
            + "    join (select s.id,\n"
            + "                (cast(s.cf_values as json)#>>'{cardWithExemption, 0, boolean}')\\:\\:boolean as cardWithExemption,\n"
            + "                (cast(s.cf_values as json)#>>'{exemptionEndDate, 0, date}')\\:\\:timestamp as exemptionEndDate,\n"
            + "                s.subscription_date,\n"
            + "                s.user_account_id,\n"
            + "                s.offer_id\n"
            + "              from billing_subscription s\n"
            + "              join cat_offer_template offer on s.offer_id=offer.id\n"
            + "              where cast(offer.cf_values as json)#>>'{sharingLevel, 0, string}'='OF'\n"
            + "              and s.status='ACTIVE'\n"
            + "              and ( s.termination_date is null or \n"
            + "                    (cast(s.cf_values as json)#>>'{dateTerminated, 0, date}')\\:\\:timestamp >= :counterStartDate \n"
            + "                  )\n"
            + "          ) sub on si.subscription_id = sub.id\n"
            + "    where si.code like '%_SUBSCRIPTION'\n"
            + "    and si.status = 'ACTIVE'\n"
            + ") t\n"
            + "where t.activated_at <= :counterEndDate \n"
            + "group by t.user_account_id, t.offer_id";

    private static final String OFFER_INIT_COUNT_QUERY = "select count(1) from " + "(select t.user_account_id, t.offer_id, count(*) \n" + FROM_CLAUSE + ") c";

    private static final String OFFERS_TO_INITILIZE = "select distinct t.offer_id \n" + FROM_CLAUSE;

    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    @Inject
    private Logger log;

    @Inject
    private OfferPoolInitializerAsync offerPoolInitializerAsync;

    @Interceptors({ JobLoggingInterceptor.class, PerformanceInterceptor.class })
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void execute(JobExecutionResultImpl result, JobInstance jobInstance) {
        log.debug("Running Job with parameter={}", jobInstance.getParametres());

        Long nbRuns = (Long) this.getParamOrCFValue(jobInstance, "nbRuns", -1L);
        if (nbRuns == -1) {
            nbRuns = (long) Runtime.getRuntime().availableProcessors();
        }
        Long waitingMillis = (Long) this.getParamOrCFValue(jobInstance, "waitingMillis", 0L);

        Date counterStartDate = getCounterStartDate(jobInstance);
        Date counterEndDate = getCounterEndDate(counterStartDate);

        try {
            BigInteger offerCountersNbr = (BigInteger) emWrapper.getEntityManager()
                .createNativeQuery(OFFER_INIT_COUNT_QUERY)
                .setParameter("counterStartDate", counterStartDate)
                .setParameter("counterEndDate", counterEndDate)
                .getSingleResult();

            log.info("Total of agencies/offers counters to be initialized: {}", offerCountersNbr.longValue());
            result.setNbItemsToProcess(offerCountersNbr.longValue());

            @SuppressWarnings("unchecked")
            List<BigInteger> offerIds = emWrapper.getEntityManager().createNativeQuery(OFFERS_TO_INITILIZE)
                .setParameter("counterStartDate", counterStartDate)
                .setParameter("counterEndDate", counterEndDate)
                .getResultList();

            log.info("Total of sahred offers that pools should be initialized: {}", offerIds.size());

            SubListCreator<BigInteger> subListCreator = new SubListCreator<>(offerIds, nbRuns.intValue());
            List<Future<String>> futures = new ArrayList<>();
            MeveoUser lastCurrentUser = currentUser.unProxy();

            while (subListCreator.isHasNext()) {
                futures
                    .add(offerPoolInitializerAsync.launchAndForget(subListCreator.getNextWorkSet(), counterStartDate, counterEndDate, result, lastCurrentUser));
                try {
                    Thread.sleep(waitingMillis.longValue());

                } catch (InterruptedException e) {
                    log.error("", e);
                }
            }

            // Wait for all async methods to finish
            for (Future<String> future : futures) {
                try {
                    future.get();

                } catch (InterruptedException e) {
                    // It was cancelled from outside - no interest

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    result.registerError(cause.getMessage());
                    log.error("Failed to execute async method", cause);
                }
            }

            if (result.getNbItemsProcessedWithError() == 0) {
                result.addReport("OfferPoolInitializer has been executed for the month: " + DateUtils.formatDateWithPattern(counterEndDate, DATE_PATTERN));
            }
            result.setDone(true);

        } catch (Exception e) {
            log.error("Failed to initialize offers shared pools ", e);
            result.registerError(e.getClass().getSimpleName() + " : " + e.getMessage());
        }
    }

    /**
     * Get start date from param
     *
     * @param jobInstance
     * @return
     */
    private Date getCounterStartDate(JobInstance jobInstance) {
        Date date;

        String dateParam = (String) this.getParamOrCFValue(jobInstance, "DATE", "");
        if (!StringUtils.isBlank(dateParam)) {
            date = DateUtils.parseDateWithPattern(dateParam, DATE_PATTERN);
            if (date == null) {
                throw new BusinessException("The date format is incorrect, it must respect the format: " + DATE_PATTERN);
            }
        } else {
            date = new Date();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        cal.set(year, month, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * Get end date from param
     * 
     * @param date
     * @return
     */
    private Date getCounterEndDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        // Set last day of month
        int lastDay = cal.getActualMaximum(Calendar.DATE);
        cal.set(year, month, lastDay, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

}
