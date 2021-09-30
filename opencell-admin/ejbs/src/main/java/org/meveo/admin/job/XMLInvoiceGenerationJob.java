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
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.jobs.JobCategoryEnum;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.jobs.MeveoJobCategoryEnum;
import org.meveo.service.job.Job;

/**
 * The Class XMLInvoiceGenerationJob generate XML for all valid invoices that dont have it..
 * @author Abdellatif BARI
 * @lastModifiedVersion 7.0
 */
@Stateless
public class XMLInvoiceGenerationJob extends Job {

    /** The xml invoice generation job bean. */
    @Inject
    private XMLInvoiceGenerationJobBean xmlInvoiceGenerationJobBean;


    @Override
    @TransactionAttribute(TransactionAttributeType.NEVER)
    protected void execute(JobExecutionResultImpl result, JobInstance jobInstance) throws BusinessException {
        xmlInvoiceGenerationJobBean.execute(result, jobInstance.getParametres(), jobInstance);
    }

    @Override
    public JobCategoryEnum getJobCategory() {
        return MeveoJobCategoryEnum.INVOICING;
    }

    @Override
    public Map<String, CustomFieldTemplate> getCustomFields() {
        Map<String, CustomFieldTemplate> result = new HashMap<String, CustomFieldTemplate>();
        
        final String APPLIES_TO = "JobInstance_XMLInvoiceGenerationJob";

        CustomFieldTemplate customFieldNbRuns = new CustomFieldTemplate();
        customFieldNbRuns.setCode("nbRuns");
        customFieldNbRuns.setAppliesTo(APPLIES_TO);
        customFieldNbRuns.setActive(true);
        customFieldNbRuns.setDescription(resourceMessages.getString("jobExecution.nbRuns"));
        customFieldNbRuns.setFieldType(CustomFieldTypeEnum.LONG);
        customFieldNbRuns.setValueRequired(false);
        customFieldNbRuns.setDefaultValue("-1");
        result.put("nbRuns", customFieldNbRuns);

        CustomFieldTemplate customFieldNbWaiting = new CustomFieldTemplate();
        customFieldNbWaiting.setCode("waitingMillis");
        customFieldNbWaiting.setAppliesTo(APPLIES_TO);
        customFieldNbWaiting.setActive(true);
        customFieldNbWaiting.setDescription(resourceMessages.getString("jobExecution.waitingMillis"));
        customFieldNbWaiting.setFieldType(CustomFieldTypeEnum.LONG);
        customFieldNbWaiting.setValueRequired(false);
        customFieldNbWaiting.setDefaultValue("0");
        result.put("waitingMillis", customFieldNbWaiting);
        
        CustomFieldTemplate customFieldInvToProcess = new CustomFieldTemplate();
		final String cfInvToProcessCode = "invoicesToProcess";
		
		Map<String, String> invoicesToProcessValues = new HashMap<String, String>();
		invoicesToProcessValues.put(InvoicesToProcessEnum.FinalOnly.name(), InvoicesToProcessEnum.FinalOnly.name());
		invoicesToProcessValues.put(InvoicesToProcessEnum.DraftOnly.name(), InvoicesToProcessEnum.DraftOnly.name());
		invoicesToProcessValues.put(InvoicesToProcessEnum.All.name(), InvoicesToProcessEnum.All.name());
		
		customFieldInvToProcess.setCode(cfInvToProcessCode);
		customFieldInvToProcess.setAppliesTo(APPLIES_TO);
		customFieldInvToProcess.setActive(true);
		customFieldInvToProcess.setDefaultValue(InvoicesToProcessEnum.FinalOnly.name());
		customFieldInvToProcess.setDescription(resourceMessages.getString("InvoicesToProcessEnum.label"));
		customFieldInvToProcess.setFieldType(CustomFieldTypeEnum.LIST);
		customFieldInvToProcess.setValueRequired(true);
		customFieldInvToProcess.setListValues(invoicesToProcessValues);
		result.put(cfInvToProcessCode, customFieldInvToProcess);

        CustomFieldTemplate customFieldBR = new CustomFieldTemplate();
        customFieldBR.setCode("billingRuns");
        customFieldBR.setAppliesTo(APPLIES_TO);
        customFieldBR.setActive(true);
        customFieldBR.setDescription(resourceMessages.getString("jobExecution.billingRuns"));
        customFieldBR.setFieldType(CustomFieldTypeEnum.ENTITY);
        customFieldBR.setStorageType(CustomFieldStorageTypeEnum.LIST);
        customFieldBR.setEntityClazz("org.meveo.model.billing.BillingRun");
        customFieldBR.setValueRequired(false);
        result.put("billingRuns", customFieldBR);

        return result;
    }

}