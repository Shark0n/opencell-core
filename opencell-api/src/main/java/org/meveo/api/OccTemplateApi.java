package org.meveo.api;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.OccTemplateDto;
import org.meveo.api.dto.OccTemplatesDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.payments.OCCTemplate;
import org.meveo.service.payments.impl.OCCTemplateService;

@Stateless
public class OccTemplateApi extends BaseApi {

    @Inject
    private OCCTemplateService occTemplateService;

    public void create(OccTemplateDto postData) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getAccountCode())) {
            missingParameters.add("accountCode");
        }
        if (StringUtils.isBlank(postData.getOccCategory())) {
            missingParameters.add("occCategory");
        }

        handleMissingParametersAndValidate(postData);

        

        if (occTemplateService.findByCode(postData.getCode()) != null) {
            throw new EntityAlreadyExistsException(OCCTemplate.class, postData.getCode());
        }

        OCCTemplate occTemplate = new OCCTemplate();
        occTemplate.setCode(postData.getCode());
        occTemplate.setDescription(postData.getDescription());
        occTemplate.setAccountCode(postData.getAccountCode());
        occTemplate.setAccountCodeClientSide(postData.getAccountCodeClientSide());
        occTemplate.setOccCategory(postData.getOccCategory());

        occTemplateService.create(occTemplate);
    }

    public void update(OccTemplateDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getAccountCode())) {
            missingParameters.add("accountCode");
        }
        if (StringUtils.isBlank(postData.getOccCategory())) {
            missingParameters.add("occCategory");
        }

        handleMissingParametersAndValidate(postData);

        

        OCCTemplate occTemplate = occTemplateService.findByCode(postData.getCode());
        if (occTemplate == null) {
            throw new EntityDoesNotExistsException(OCCTemplate.class, postData.getCode());
        }
        occTemplate.setCode(StringUtils.isBlank(postData.getUpdatedCode()) ? postData.getCode() : postData.getUpdatedCode());
        occTemplate.setDescription(postData.getDescription());
        occTemplate.setAccountCode(postData.getAccountCode());
        occTemplate.setAccountCodeClientSide(postData.getAccountCodeClientSide());
        occTemplate.setOccCategory(postData.getOccCategory());

        occTemplateService.update(occTemplate);

    }

    public OccTemplateDto find(String code) throws MeveoApiException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("occTemplateCode");
            handleMissingParameters();
        }

        OCCTemplate occTemplate = occTemplateService.findByCode(code);
        if (occTemplate == null) {
            throw new EntityDoesNotExistsException(OCCTemplate.class, code);
        }

        return new OccTemplateDto(occTemplate);

    }

    public void remove(String code) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("occTemplateCode");
            handleMissingParameters();
        }

        OCCTemplate occTemplate = occTemplateService.findByCode(code);
        if (occTemplate == null) {
            throw new EntityDoesNotExistsException(OCCTemplate.class, code);
        }

        occTemplateService.remove(occTemplate);
    }

    /**
     * create or update occ template based on occ template code
     * 
     * @param postData

     * @throws MeveoApiException
     * @throws BusinessException 
     */
    public void createOrUpdate(OccTemplateDto postData) throws MeveoApiException, BusinessException {

        OCCTemplate occTemplate = occTemplateService.findByCode(postData.getCode());

        if (occTemplate == null) {
            create(postData);
        } else {
            update(postData);
        }
    }

    /**
     * retrieve a list of occ templates
     *
     * @param provider
     * @throws MeveoApiException
     * @throws BusinessException
     */
    public OccTemplatesDto list() throws MeveoApiException {
        OccTemplatesDto occTemplatesDto = new OccTemplatesDto();
        List<OCCTemplate> occTemplates = occTemplateService.list();

        if (occTemplates != null) {
            OccTemplateDto occTemplateDto;
            for (OCCTemplate occTemplate : occTemplates) {
                occTemplateDto = new OccTemplateDto(occTemplate);
                occTemplatesDto.getOccTemplate().add(occTemplateDto);
            }
        }

        return occTemplatesDto;
    }
}