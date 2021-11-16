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

package org.meveo.service.catalog.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.xml.bind.ValidationException;

import org.meveo.api.exception.BusinessApiException;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.billing.InvoiceSubCategory;
import org.meveo.model.catalog.ChargeTemplate;
import org.meveo.model.catalog.ChargeTemplateStatusEnum;
import org.meveo.model.catalog.TriggeredEDRTemplate;
import org.meveo.service.base.BusinessService;

/**
 * Charge Template service implementation.
 * 
 */
@Stateless
public class ChargeTemplateServiceAll extends BusinessService<ChargeTemplate> {

	@SuppressWarnings("unchecked")
	public List<ChargeTemplate> findByEDRTemplate(TriggeredEDRTemplate edrTemplate){
		QueryBuilder qb=new QueryBuilder(this.getEntityClass(),"c");
		qb.addCriterionEntityInList("edrTemplates", edrTemplate);
		return qb.find(getEntityManager());
	}
	
	@SuppressWarnings("unchecked")
	public List<ChargeTemplate> findByInvoiceSubCategory(InvoiceSubCategory invoiceSubCategory){
		QueryBuilder qb=new QueryBuilder(this.getEntityClass(),"c");
		qb.addCriterionEntity("invoiceSubCategory", invoiceSubCategory);
		return qb.find(getEntityManager());
	}

	/**
	 * @param chargeTemplate
	 * @param stringStatus
	 * @return
	 */
	public void updateStatus(ChargeTemplate chargeTemplate, String stringStatus) {
		ChargeTemplateStatusEnum status = ChargeTemplateStatusEnum.valueOf(stringStatus);
		try {
			chargeTemplate.setStatus(status);
			update(chargeTemplate);
		} catch (ValidationException e) {
			throw new BusinessApiException(e.getMessage());
		}
	}
}