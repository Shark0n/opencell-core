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

package org.meveo.api.dto.billing;

import java.util.Date;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomFieldsDto;
import org.meveo.model.billing.DiscountPlanInstance;

/**
 * @author Edward P. Legaspi
 * @lastModifiedVersion 5.3
 */
public class DiscountPlanInstanceDto extends BaseEntityDto {

	private static final long serialVersionUID = 3302140811850985823L;

	/**
	 * The discount plan code.
	 */
	private String discountPlan;

	/**
	 * The billingAccount code.
	 */
	private String billingAccount;
	/**
	 * The subscription code.
	 */
	private String subscription;

	/**
	 * Effectivity start date
	 */
	private Date startDate;

	/**
	 * Effectivity end date
	 */
	private Date endDate;
	
	/** The custom fields. */
    private CustomFieldsDto customFields;

	/**
	 * Default constructor.
	 */
	public DiscountPlanInstanceDto() {
		
	}
	
	/**
	 * Initialized from {@link DiscountPlanInstance} entity.
	 * @param e the discount plan instance entity
	 */
	public DiscountPlanInstanceDto(DiscountPlanInstance e) {
		discountPlan = e.getDiscountPlan().getCode();
		billingAccount = e.getBillingAccount().getCode();
		startDate = e.getStartDate();
		endDate = e.getEndDate();
	}

	public DiscountPlanInstanceDto(DiscountPlanInstance e, CustomFieldsDto customFields) {
		discountPlan = e.getDiscountPlan().getCode();
		if(e.getBillingAccount()!=null){
			billingAccount = e.getBillingAccount().getCode();
		}else{
			subscription = e.getSubscription().getCode();
		}
		startDate = e.getStartDate();
		endDate = e.getEndDate();
		this.customFields = customFields;
	}

	/**
	 * Gets the discount plan code
	 * @return code
	 */
	public String getDiscountPlan() {
		return discountPlan;
	}

	/**
	 * Sets the discount plan code
	 * @param discountPlan code
	 */
	public void setDiscountPlan(String discountPlan) {
		this.discountPlan = discountPlan;
	}

	/**
	 * Gets the billing account code
	 * @return code
	 */
	public String getBillingAccount() {
		return billingAccount;
	}

	/**
	 * Sets the billing account code
	 * @param billingAccount code
	 */
	public void setBillingAccount(String billingAccount) {
		this.billingAccount = billingAccount;
	}

	/**
	 * Gets the subscription code
	 * @return code
	 */
	public String getSubscription() {
		return subscription;
	}

	/**
	 * Sets the subscription code
	 * @param subscription code
	 */
	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	/**
	 * Gets the effective start date.
	 * @return date
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Sets the effective start date.
	 * @param startDate date
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * Gets the effective end date.
	 * @return date
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Sets the effective end date.
	 * @param endDate end date
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public CustomFieldsDto getCustomFields() {
		return customFields;
	}

	public void setCustomFields(CustomFieldsDto customFields) {
		this.customFields = customFields;
	}

}
