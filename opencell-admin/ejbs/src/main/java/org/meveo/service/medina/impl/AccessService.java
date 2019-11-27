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
package org.meveo.service.medina.impl;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.billing.Subscription;
import org.meveo.model.mediation.Access;
import org.meveo.service.base.PersistenceService;

@Stateless
public class AccessService extends PersistenceService<Access> {

    /**
     * Get a list of Accesses matching a given accessUserId value
     * 
     * @param accessUserId accessUserId
     * @return List of Access
     */
    public List<Access> getActiveAccessByUserId(String accessUserId) {
        return getEntityManager().createNamedQuery("Access.getAccessesByUserId", Access.class).setParameter("accessUserId", accessUserId).getResultList();
    }

    public boolean isDuplicateAndOverlaps(Access access) {
        String stringQuery ="SELECT COUNT(*) FROM " + Access.class.getName() + " a WHERE a.accessUserId=:accessUserId AND a.subscription.id=:subscriptionId"+
                " AND ((:start_date BETWEEN a.startDate AND a.endDate) OR (:end_date BETWEEN a.startDate AND a.endDate))";
        Query query = getEntityManager().createQuery(stringQuery);
        query.setParameter("accessUserId", access.getAccessUserId());
        query.setParameter("subscriptionId", access.getSubscription().getId());
        query.setParameter("start_date", access.getStartDate());
        query.setParameter("end_date", access.getEndDate());
        query.setHint("org.hibernate.flushMode", "NEVER");
        return ((Long) query.getSingleResult()).intValue() != 0;
    }

    public Access findByUserIdAndSubscription(String accessUserId, Subscription subscription, Date startDate, Date endDate) {
        try {
            String selectAccessByUserIdSubscriptionIdStartEndDateQuery ="SELECT a FROM " + Access.class.getName() +" a"
                    + " WHERE a.accessUserId=:accessUserId AND a.subscription.id=:subscriptionId"
                    + " AND ((:start_date BETWEEN a.startDate AND a.endDate) AND (:end_date BETWEEN a.startDate AND a.endDate))";
            Query query = getEntityManager().createQuery(selectAccessByUserIdSubscriptionIdStartEndDateQuery);
            query.setParameter("accessUserId", accessUserId);
            query.setParameter("subscriptionId", subscription.getId());
            query.setParameter("start_date", startDate);
            query.setParameter("end_date", endDate);
            return (Access) query.getSingleResult();
        } catch (NoResultException e) {
            log.warn("no result found");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Access> listBySubscription(Subscription subscription) {
        QueryBuilder qb = new QueryBuilder(Access.class, "c");
        qb.addCriterionEntity("subscription", subscription);

        try {
            return (List<Access>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException e) {
            log.warn("failed to get list Access by subscription", e);
            return null;
        }
    }
}