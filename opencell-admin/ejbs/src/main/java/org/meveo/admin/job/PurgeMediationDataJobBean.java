package org.meveo.admin.job;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.admin.job.logging.JobLoggingInterceptor;
import org.meveo.interceptor.PerformanceInterceptor;
import org.meveo.model.billing.RatedTransactionStatusEnum;
import org.meveo.model.billing.WalletOperationStatusEnum;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.rating.EDRStatusEnum;
import org.meveo.model.shared.DateUtils;
import org.meveo.service.billing.impl.EdrService;
import org.meveo.service.billing.impl.RatedTransactionService;
import org.meveo.service.billing.impl.WalletOperationService;
import org.slf4j.Logger;

import static org.meveo.admin.job.PurgeMediationDataJob.PURGE_MEDIATION_DATA_JOB_EDR_STATUS_CF;
import static org.meveo.admin.job.PurgeMediationDataJob.PURGE_MEDIATION_DATA_JOB_RT_STATUS_CF;
import static org.meveo.admin.job.PurgeMediationDataJob.PURGE_MEDIATION_DATA_JOB_WO_STATUS_CF;

/**
 * The Class job bean to remove not open EDR, WO, RTx between two dates.
 *
 * @author Khalid HORRI
 * @lastModifiedVersion 7.3
 */
@Stateless
public class PurgeMediationDataJobBean extends BaseJobBean {

    @Inject
    private Logger log;

    @Inject
    private EdrService edrService;

    @Inject
    private WalletOperationService walletOperationService;

    @Inject
    private RatedTransactionService ratedTransactionService;

    @Interceptors({ JobLoggingInterceptor.class, PerformanceInterceptor.class })
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void execute(JobExecutionResultImpl result, JobInstance jobInstance) {
        log.debug("Running with parameter={}", jobInstance.getParametres());
        try {
            Date firstTransactionDate = (Date) this.getParamOrCFValue(jobInstance, "PurgeMediationDataJob_firstTransactionDate");
            Date lastTransactionDate = (Date) this.getParamOrCFValue(jobInstance, "PurgeMediationDataJob_lastTransactionDate");
            if (lastTransactionDate == null) {
                lastTransactionDate = new Date();
            }
            long nbItems = 0;
            String report = "";
            String formattedStartDate = DateUtils.formatDateWithPattern(firstTransactionDate, "yyyy-MM-dd");
            String formattedEndDate = DateUtils.formatDateWithPattern(lastTransactionDate, "yyyy-MM-dd");
            List<WalletOperationStatusEnum> woStatusList = getTargetStatusList(jobInstance, WalletOperationStatusEnum.class, PURGE_MEDIATION_DATA_JOB_WO_STATUS_CF);
            if (!woStatusList.isEmpty()) {
                log.info("=> starting purge wallet operation between {} and {}", formattedStartDate, formattedEndDate);
                nbItems = walletOperationService.purge(firstTransactionDate, lastTransactionDate, woStatusList);
                log.info("==>{} WOs rows purged", nbItems);
                report += "WOs :" + nbItems;
            }
            List<RatedTransactionStatusEnum> rtStatusList = getTargetStatusList(jobInstance, RatedTransactionStatusEnum.class, PURGE_MEDIATION_DATA_JOB_RT_STATUS_CF);
            if (!rtStatusList.isEmpty()) {
                log.info("=> starting purge rated transactions between {} and {}", formattedStartDate, formattedEndDate);
                long itemsRemoved = ratedTransactionService.purge(firstTransactionDate, lastTransactionDate, rtStatusList);
                log.info("==>{} RTs rows purged", itemsRemoved);
                report += ", RTs : " + itemsRemoved;
                nbItems += itemsRemoved;
            }
            List<EDRStatusEnum> edrStatusList = getTargetStatusList(jobInstance, EDRStatusEnum.class, PURGE_MEDIATION_DATA_JOB_EDR_STATUS_CF);
            if (!edrStatusList.isEmpty()) {
                log.info("=> starting purge rated transactions between {} and {}", formattedStartDate, formattedEndDate);
                long itemsRemoved = edrService.purge(firstTransactionDate, lastTransactionDate, edrStatusList);
                log.info("==>{} EDRs rows purged ", itemsRemoved);
                report += ", EDRs : " + itemsRemoved;
                nbItems += itemsRemoved;
            }
            result.setNbItemsToProcess(nbItems);
            result.setNbItemsCorrectlyProcessed(nbItems);
            result.addReport(report);
        } catch (Exception e) {
            log.error("Failed to run purge EDR/WO/RT job", e);
            result.registerError(e.getMessage());
            result.addReport(e.getMessage());
        }
    }
}