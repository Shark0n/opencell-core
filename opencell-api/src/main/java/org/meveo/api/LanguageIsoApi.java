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

package org.meveo.api;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.LanguageIsoDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.billing.Language;
import org.meveo.service.admin.impl.LanguageService;


/**
 * @author Mounir HAMMAM
 **/
@Stateless
public class LanguageIsoApi extends BaseApi {

    @Inject
    private LanguageService languageService;

    public void create(LanguageIsoDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
 
        handleMissingParameters();

        if (languageService.findByCode(postData.getCode()) != null) {
            throw new EntityAlreadyExistsException(Language.class, postData.getCode());
        }

        Language language = new Language();
        language.setLanguageCode(postData.getCode());
        language.setDescriptionEn(postData.getDescription());
        languageService.create(language);

    }

    public void update(LanguageIsoDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }

        handleMissingParameters();

        Language language = languageService.findByCode(postData.getCode());

        if (language == null) {
            throw new EntityDoesNotExistsException(Language.class, postData.getCode());
        }
        language.setDescriptionEn(postData.getDescription());

        languageService.update(language);
    }

    public LanguageIsoDto find(String languageCode) throws MeveoApiException {

        if (StringUtils.isBlank(languageCode)) {
            missingParameters.add("languageCode");
            handleMissingParameters();
        }

        LanguageIsoDto result = new LanguageIsoDto();

        Language language = languageService.findByCode(languageCode);
        if (language == null) {
            throw new EntityDoesNotExistsException(Language.class, languageCode);
        }

        result = new LanguageIsoDto(language);

        return result;
    }

    public void remove(String languageCode) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(languageCode)) {
            missingParameters.add("languageCode");
            handleMissingParameters();
        }

        Language language = languageService.findByCode(languageCode);
        if (language == null) {
            throw new EntityDoesNotExistsException(Language.class, languageCode);
        }

        languageService.remove(language);
    }

    public void createOrUpdate(LanguageIsoDto postData) throws MeveoApiException, BusinessException {

        Language language = languageService.findByCode(postData.getCode());
        if (language == null) {
            create(postData);
        } else {
            update(postData);
        }
    }
    
	public List<LanguageIsoDto> list() {
		List<LanguageIsoDto> result = new ArrayList<>();

		List<Language> languages = languageService.list();
		if (languages != null) {
			for (Language country : languages) {
				result.add(new LanguageIsoDto(country));
			}
		}

		return result;
	}
}