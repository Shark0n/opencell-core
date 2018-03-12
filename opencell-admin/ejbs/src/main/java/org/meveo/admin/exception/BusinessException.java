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
package org.meveo.admin.exception;

import javax.ejb.ApplicationException;
import javax.inject.Inject;

import org.meveo.commons.utils.EjbUtils;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.event.monitoring.CreateEventHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationException(rollback = true)
public class BusinessException extends Exception {
    private static final long serialVersionUID = 1L;

    @Inject
    private ParamBeanFactory paramBeanFactory;

    public BusinessException() {
        super();
        registerEvent();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        registerEvent();
    }

    public BusinessException(String message) {
        super(message);
        registerEvent();
    }

    public BusinessException(Throwable cause) {
        super(cause);
        registerEvent();
    }

    public void registerEvent() {
        if ("true".equals(paramBeanFactory.getInstance().getProperty("monitoring.sendException", "true"))) {
            try {
                CreateEventHelper createEventHelper = (CreateEventHelper) EjbUtils.getServiceInterface("CreateEventHelper");
                createEventHelper.register(this);
            } catch (Exception e) {
                Logger log = LoggerFactory.getLogger(this.getClass());
                log.error("Failed to access event helper", e);
            }
        }
    }
}
