/*
 * (C) Copyright 2015-2020 Opencell SAS (https://opencellsoft.com/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW. EXCEPT WHEN
 * OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM "AS
 * IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO
 * THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * For more information on the GNU Affero General Public License, please consult
 * <https://www.gnu.org/licenses/agpl-3.0.en.html>.
 */

package org.meveo.admin.job;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.jobs.JobCategoryEnum;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.jobs.MeveoJobCategoryEnum;
import org.meveo.service.job.Job;


/**
 * The Class AccountOperationsGenerationJob generate the invoice account operation for all invoices that dont have it yet.
 * @author Abdellatif BARI
 * @lastModifiedVersion 7.0
 */
@Stateless
public class AccountOperationsGenerationJob extends Job {

    /** The account operations generation job bean. */
    @Inject
    private AccountOperationsGenerationJobBean accountOperationsGenerationJobBean;
            
    
    @Override
    @TransactionAttribute(TransactionAttributeType.NEVER)
    protected void execute(JobExecutionResultImpl result, JobInstance jobInstance) throws BusinessException {
        accountOperationsGenerationJobBean.execute(result, jobInstance );
    }

    
    @Override
    public JobCategoryEnum getJobCategory() {
        return MeveoJobCategoryEnum.ACCOUNT_RECEIVABLES;
    }
    
    
    @Override
   	public Map<String, CustomFieldTemplate> getCustomFields() {
           Map<String, CustomFieldTemplate> result = new HashMap<String, CustomFieldTemplate>();

   		CustomFieldTemplate nbRuns = new CustomFieldTemplate();
   		nbRuns.setCode("nbRuns");
   		nbRuns.setAppliesTo("JobInstance_AccountOperationsGenerationJob");
   		nbRuns.setActive(true);
   		nbRuns.setDescription(resourceMessages.getString("jobExecution.nbRuns"));
   		nbRuns.setFieldType(CustomFieldTypeEnum.LONG);
   		nbRuns.setValueRequired(false);
   		nbRuns.setDefaultValue("-1");
   		result.put("nbRuns", nbRuns);

   		CustomFieldTemplate waitingMillis = new CustomFieldTemplate();
   		waitingMillis.setCode("waitingMillis");
   		waitingMillis.setAppliesTo("JobInstance_AccountOperationsGenerationJob");
   		waitingMillis.setActive(true);
   		waitingMillis.setDescription(resourceMessages.getString("jobExecution.waitingMillis"));
   		waitingMillis.setFieldType(CustomFieldTypeEnum.LONG);
   		waitingMillis.setValueRequired(false);
   		waitingMillis.setDefaultValue("0");
   		result.put("waitingMillis", waitingMillis);

   		return result;
   	}
}