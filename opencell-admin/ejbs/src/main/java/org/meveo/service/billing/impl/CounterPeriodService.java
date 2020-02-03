/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.billing.impl;

import java.math.BigDecimal;
import java.util.Date;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.ICounterEntity;

import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.CounterInstance;
import org.meveo.model.billing.CounterPeriod;
import org.meveo.model.billing.ServiceInstance;
import org.meveo.model.billing.Subscription;
import org.meveo.model.billing.UserAccount;
import org.meveo.model.jaxb.customer.Customer;
import org.meveo.model.jaxb.customer.CustomerAccount;
import org.meveo.service.base.PersistenceService;

/**
 * The CounterPeriod service class
 * @author Khalid HORRI
 * @lastModifiedVersion 9.0
 */

@Stateless
public class CounterPeriodService extends PersistenceService<CounterPeriod> {

    /**
     * Find an existing counter period matching a given date
     *
     * @param counterInstance Counter instance
     * @param date            Date to match
     * @return Counter period
     * @throws BusinessException Business exception
     */
    public CounterPeriod getCounterPeriod(CounterInstance counterInstance, Date date) throws BusinessException {
        Query query = getEntityManager().createNamedQuery("CounterPeriod.findByPeriodDate");
        query.setParameter("counterInstance", counterInstance);
        query.setParameter("date", date, TemporalType.TIMESTAMP);
        try {
            return (CounterPeriod) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Return the counter value of a ICounterEntity UserAccount, BillingAccount, Subscription or ServiceInstance
     *
     * @param entity      the ICounterEntity
     * @param counterCode the counter code
     * @return the counter value.
     */
    public BigDecimal getCounterValue(ICounterEntity entity, String counterCode) {
        return getSingleCounterValue(entity, counterCode, null);
    }

    /**
     * Return the counter value of a ICounterEntity UserAccount, BillingAccount, Subscription or ServiceInstance where the startDate<=date<endDate
     *
     * @param entity      the ICounterEntity
     * @param counterCode the counter code
     * @param date the date to be compared to start and end date of a CounterPeriod
     * @return the counter value.
     */
    public BigDecimal getCounterValueByDate(ICounterEntity entity, String counterCode, Date date) {
        return getSingleCounterValue(entity, counterCode, date);
    }

    private BigDecimal getSingleCounterValue(ICounterEntity entity, String counterCode, Date date) {
        Query query = getEntityManager().createNamedQuery("CounterPeriod.findByCounterEntity");
        if (date != null) {
            query = getEntityManager().createNamedQuery("CounterPeriod.findByCounterEntityAndPeriodDate");
            query.setParameter("date", date, TemporalType.TIMESTAMP);
        }
        query.setParameter("serviceInstance", null);
        query.setParameter("subscription", null);
        query.setParameter("billingAccount", null);
        query.setParameter("userAccount", null);
        query.setParameter("customerAccount", null);
        query.setParameter("customer", null);
        query.setParameter("counterCode", counterCode);

        if (entity instanceof ServiceInstance) {
            query.setParameter("serviceInstance", entity);
        }
        if (entity instanceof Subscription) {
            query.setParameter("subscription", entity);
        }
        if (entity instanceof BillingAccount) {
            query.setParameter("billingAccount", entity);
        }
        if (entity instanceof UserAccount) {
            query.setParameter("userAccount", entity);
        }
        if (entity instanceof CustomerAccount) {
            query.setParameter("customerAccount", entity);
        }
        if (entity instanceof Customer) {
            query.setParameter("customer", entity);
        }
        try {
            CounterPeriod cp = (CounterPeriod) query.getSingleResult();
            return cp.getValue();
        } catch (NoResultException e) {
            return null;
        }
    }
}