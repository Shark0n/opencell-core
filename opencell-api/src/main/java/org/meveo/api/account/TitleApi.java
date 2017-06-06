package org.meveo.api.account;

import java.util.List;

import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.response.TitleDto;
import org.meveo.api.dto.response.TitlesDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.shared.Title;
import org.meveo.service.catalog.impl.TitleService;

public class TitleApi extends BaseApi {

    @Inject
    private TitleService titleService;

    /**
     * Creates a new Title entity.
     * 
     * @param postData
     * @throws MeveoApiException
     * @throws BusinessException 
     */
	public void create(TitleDto postData) throws MeveoApiException, BusinessException {

		String titleCode = postData.getCode();

		if (StringUtils.isBlank(titleCode)) {
			missingParameters.add("titleCode");
		}

		handleMissingParametersAndValidate(postData);

		Title title = titleService.findByCode(titleCode);

		if (title != null) {
			throw new EntityAlreadyExistsException(Title.class, titleCode);
		}

		Title newTitle = new Title();
		newTitle.setCode(titleCode);
		newTitle.setDescription(postData.getDescription());
		newTitle.setIsCompany(postData.getIsCompany());

		titleService.create(newTitle);
	}

    /**
     * Returns TitleDto based on title code.
     * 
     * @param titleCode
     * @return
     * @throws MeveoApiException
     */
    public TitleDto find(String titleCode) throws MeveoApiException {
        if (StringUtils.isBlank(titleCode)) {
            missingParameters.add("titleCode");
        }
        handleMissingParameters();

        Title title = titleService.findByCode(titleCode);
        if (title != null) {
            TitleDto titleDto = new TitleDto();
            titleDto.setCode(title.getCode());
            titleDto.setDescription(title.getDescription());
            titleDto.setIsCompany(title.getIsCompany());
            return titleDto;
        }
        throw new EntityDoesNotExistsException(Title.class, titleCode);
    }

    /**
     * Updates a Title Entity based on title code.
     * 
     * @param postData

     * @throws MeveoApiException
     * @throws BusinessException 
     */
	public void update(TitleDto postData) throws MeveoApiException, BusinessException {
		String titleCode = postData.getCode();
		if (StringUtils.isBlank(titleCode)) {
			missingParameters.add("titleCode");
		}

		handleMissingParametersAndValidate(postData);

		Title title = titleService.findByCode(titleCode);
		if (title != null) {
			title.setCode(
					StringUtils.isBlank(postData.getUpdatedCode()) ? postData.getCode() : postData.getUpdatedCode());
			title.setDescription(postData.getDescription());
			title.setIsCompany(postData.getIsCompany());
			titleService.update(title);
		} else {
			throw new EntityDoesNotExistsException(Title.class, titleCode);
		}
	}

    /**
     * Removes a title based on title code.
     * 
     * @param postData

     * @throws MeveoApiException
     * @throws BusinessException 
     */
    public void remove(String titleCode) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(titleCode)) {
            missingParameters.add("titleCode");
        }

        handleMissingParameters();

        Title title = titleService.findByCode(titleCode);
        if (title != null) {
            titleService.remove(title);
        } else {
            throw new EntityDoesNotExistsException(Title.class, titleCode);
        }
    }

    public void createOrUpdate(TitleDto postData) throws MeveoApiException, BusinessException {
        Title title = titleService.findByCode(postData.getCode());

        if (title == null) {
            // create
            create(postData);
        } else {
            // update
            update(postData);
        }
    }

    public TitlesDto list() throws MeveoApiException {
        TitlesDto titlesDto = new TitlesDto();
        List<Title> titles = titleService.list(true);

        if (titles != null) {
            for (Title title : titles) {
                TitleDto titleDto = new TitleDto();
                titleDto.setCode(title.getCode());
                titleDto.setDescription(title.getDescription());
                titleDto.setIsCompany(title.getIsCompany());
                titlesDto.getTitle().add(titleDto);
            }
        }

        return titlesDto;
    }
}
