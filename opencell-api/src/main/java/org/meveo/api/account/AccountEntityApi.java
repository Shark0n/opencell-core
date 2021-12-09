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

package org.meveo.api.account;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.api.BaseApi;
import org.meveo.api.dto.account.AccountDto;
import org.meveo.api.dto.billing.SubscriptionDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.AccountEntity;
import org.meveo.model.BusinessEntity;
import org.meveo.model.billing.Subscription;
import org.meveo.model.catalog.OfferTemplate;
import org.meveo.model.catalog.OneShotChargeTemplate;
import org.meveo.model.shared.Address;
import org.meveo.model.shared.ContactInformation;
import org.meveo.model.shared.Name;
import org.meveo.model.shared.Title;
import org.meveo.service.admin.impl.CountryService;
import org.meveo.service.base.IVersionedBusinessEntityService;
import org.meveo.service.catalog.impl.OneShotChargeTemplateService;
import org.meveo.service.catalog.impl.TitleService;

/**
 * @author Edward P. Legaspi
 * @lastModifiedVersion 5.2
 **/
@Stateless
public class AccountEntityApi extends BaseApi {

    @Inject
    private TitleService titleService;

    @Inject
    private CountryService countryService;

    @Inject
    private OneShotChargeTemplateService oneShotChargeTemplateService;

    public void populate(AccountDto postData, AccountEntity accountEntity) throws MeveoApiException {
        Address address = new Address();
        if (postData.getAddress() != null) {
            address.setAddress1(postData.getAddress().getAddress1());
            address.setAddress2(postData.getAddress().getAddress2());
            address.setAddress3(postData.getAddress().getAddress3());
            address.setZipCode(postData.getAddress().getZipCode());
            address.setCity(postData.getAddress().getCity());
            if (!StringUtils.isBlank(postData.getAddress().getCountry())) {
                address.setCountry(countryService.findByCode(postData.getAddress().getCountry()));
            }
            address.setState(postData.getAddress().getState());
        }

        Name name = new Name();
        if (postData.getName() != null) {
            name.setFirstName(postData.getName().getFirstName());
            name.setLastName(postData.getName().getLastName());
            if (!StringUtils.isBlank(postData.getName().getTitle())) {
                Title title = titleService.findByCode(postData.getName().getTitle());
                if (title == null) {
                    throw new EntityDoesNotExistsException(Title.class, postData.getName().getTitle());
                } else {
                    name.setTitle(title);
                }
            }
        }
        accountEntity.setCode(StringUtils.isBlank(postData.getUpdatedCode()) ? postData.getCode() : postData.getUpdatedCode());
        accountEntity.setDescription(postData.getDescription());
        accountEntity.setExternalRef1(postData.getExternalRef1());
        accountEntity.setExternalRef2(postData.getExternalRef2());
        accountEntity.setAddress(address);
        accountEntity.setName(name);
        accountEntity.setJobTitle(postData.getJobTitle());
        accountEntity.setVatNo(postData.getVatNo());
        accountEntity.setRegistrationNo(postData.getRegistrationNo());
        
        if (postData.getContactInformation() != null) {
            if (accountEntity.getContactInformation() == null) {
            	accountEntity.setContactInformation(new ContactInformation());
            }
            accountEntity.getContactInformation().setEmail(postData.getContactInformation().getEmail());
            accountEntity.getContactInformation().setPhone(postData.getContactInformation().getPhone());
            accountEntity.getContactInformation().setMobile(postData.getContactInformation().getMobile());
        }
        setMinimumAmountElSubscription(postData, accountEntity);
    }

    public void updateAccount(AccountEntity accountEntity, AccountDto postData) throws MeveoApiException {
        updateAccount(accountEntity, postData, true);
    }

    public void updateAccount(AccountEntity accountEntity, AccountDto postData, boolean checkCustomFields) throws MeveoApiException {

        if (postData.getAddress() != null) {
            Address address = accountEntity.getAddress() == null ? new Address() : accountEntity.getAddress();

            if (postData.getAddress().getAddress1() != null) {
                address.setAddress1(postData.getAddress().getAddress1());
            }
            if (postData.getAddress().getAddress2() != null) {
                address.setAddress2(postData.getAddress().getAddress2());
            }
            if (postData.getAddress().getAddress3() != null) {
                address.setAddress3(postData.getAddress().getAddress3());
            }
            if (postData.getAddress().getZipCode() != null) {
                address.setZipCode(postData.getAddress().getZipCode());
            }
            if (postData.getAddress().getCity() != null) {
                address.setCity(postData.getAddress().getCity());
            }
            if (postData.getAddress().getCountry() != null) {
                address.setCountry(countryService.findByCode(postData.getAddress().getCountry()));
            }
            if (postData.getAddress().getState() != null) {
                address.setState(postData.getAddress().getState());
            }

            accountEntity.setAddress(address);
        }

        if (postData.getName() != null) {
            Name name = accountEntity.getName() == null ? new Name() : accountEntity.getName();

            if (!StringUtils.isBlank(postData.getName().getFirstName())) {
                name.setFirstName(postData.getName().getFirstName());
            }
            if (!StringUtils.isBlank(postData.getName().getLastName())) {
                name.setLastName(postData.getName().getLastName());
            }
            if (!StringUtils.isBlank(postData.getName().getTitle())) {
                Title title = titleService.findByCode(postData.getName().getTitle());
                if (title == null) {
                    throw new EntityDoesNotExistsException(Title.class, postData.getName().getTitle());
                } else {
                    name.setTitle(title);
                }
            }

            accountEntity.setName(name);
        }

        accountEntity.setCode(StringUtils.isBlank(postData.getUpdatedCode()) ? postData.getCode() : postData.getUpdatedCode());

        if (!StringUtils.isBlank(postData.getDescription())) {
            accountEntity.setDescription(postData.getDescription());
        }
        if (!StringUtils.isBlank(postData.getExternalRef1())) {
            accountEntity.setExternalRef1(postData.getExternalRef1());
        }
        if (!StringUtils.isBlank(postData.getExternalRef2())) {
            accountEntity.setExternalRef2(postData.getExternalRef2());
        }
        if (!StringUtils.isBlank(postData.getJobTitle())) {
            accountEntity.setJobTitle(postData.getJobTitle());
        }
        if (!StringUtils.isBlank(postData.getVatNo())) {
            accountEntity.setVatNo(postData.getVatNo());
        }
        if (!StringUtils.isBlank(postData.getRegistrationNo())) {
            accountEntity.setRegistrationNo(postData.getRegistrationNo());
        }

        if (postData.getContactInformation() != null) {
            if (accountEntity.getContactInformation() == null) {
            	accountEntity.setContactInformation(new ContactInformation());
            }
            if (!StringUtils.isBlank(postData.getContactInformation().getEmail())) {
            	accountEntity.getContactInformation().setEmail(postData.getContactInformation().getEmail());
            }
            if (!StringUtils.isBlank(postData.getContactInformation().getPhone())) {
            	accountEntity.getContactInformation().setPhone(postData.getContactInformation().getPhone());
            }
            if (!StringUtils.isBlank(postData.getContactInformation().getMobile())) {
                accountEntity.getContactInformation().setMobile(postData.getContactInformation().getMobile());
            }
            if (!StringUtils.isBlank(postData.getContactInformation().getFax())) {
                accountEntity.getContactInformation().setFax(postData.getContactInformation().getFax());
            }
        }
        setMinimumAmountElSubscription(postData, accountEntity);

    }

    private void setMinimumAmountElSubscription(AccountDto postData, AccountEntity accountEntity) {
        if (!StringUtils.isBlank(postData.getMinimumAmountEl())) {
            accountEntity.setMinimumAmountEl(postData.getMinimumAmountEl());
        }
        if (!StringUtils.isBlank(postData.getMinimumLabelEl())) {
            accountEntity.setMinimumLabelEl(postData.getMinimumLabelEl());
        }
        if (!StringUtils.isBlank(postData.getMinimumAmountElSpark())) {
            accountEntity.setMinimumAmountElSpark(postData.getMinimumAmountElSpark());
        }
        if (!StringUtils.isBlank(postData.getMinimumLabelElSpark())) {
            accountEntity.setMinimumLabelElSpark(postData.getMinimumLabelElSpark());
        }
        if (!StringUtils.isBlank(postData.getMinimumChargeTemplate())) {
            OneShotChargeTemplate minimumChargeTemplate = oneShotChargeTemplateService.findByCode(postData.getMinimumChargeTemplate());
            if (minimumChargeTemplate == null) {
                throw new EntityDoesNotExistsException(OneShotChargeTemplate.class, postData.getMinimumChargeTemplate());
            } else {
                accountEntity.setMinimumChargeTemplate(minimumChargeTemplate);
            }
        }
    }
}