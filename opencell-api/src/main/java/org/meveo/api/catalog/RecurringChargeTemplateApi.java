package org.meveo.api.catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.MeveoApiErrorCodeEnum;
import org.meveo.api.dto.LanguageDescriptionDto;
import org.meveo.api.dto.catalog.RecurringChargeTemplateDto;
import org.meveo.api.dto.catalog.TriggeredEdrTemplateDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.billing.CatMessages;
import org.meveo.model.billing.InvoiceSubCategory;
import org.meveo.model.billing.TradingLanguage;
import org.meveo.model.catalog.Calendar;
import org.meveo.model.catalog.LevelEnum;
import org.meveo.model.catalog.RecurringChargeTemplate;
import org.meveo.model.catalog.RoundingModeEnum;
import org.meveo.model.catalog.TriggeredEDRTemplate;
import org.meveo.model.finance.RevenueRecognitionRule;
import org.meveo.service.billing.impl.TradingLanguageService;
import org.meveo.service.catalog.impl.CalendarService;
import org.meveo.service.catalog.impl.CatMessagesService;
import org.meveo.service.catalog.impl.InvoiceSubCategoryService;
import org.meveo.service.catalog.impl.RecurringChargeTemplateService;
import org.meveo.service.catalog.impl.TriggeredEDRTemplateService;
import org.meveo.service.finance.RevenueRecognitionRuleService;

/**
 * @author Edward P. Legaspi
 **/
@Stateless
public class RecurringChargeTemplateApi extends BaseCrudApi<RecurringChargeTemplate, RecurringChargeTemplateDto> {

    @Inject
    private RecurringChargeTemplateService recurringChargeTemplateService;

    @Inject
    private InvoiceSubCategoryService invoiceSubCategoryService;

    @Inject
    private CalendarService calendarService;

    @Inject
    private CatMessagesService catMessagesService;

    @Inject
    private TriggeredEDRTemplateService triggeredEDRTemplateService;
    
    @Inject
    private RevenueRecognitionRuleService revenueRecognitionRuleService;
    
    @Inject
    private TradingLanguageService tradingLanguageService;

    public RecurringChargeTemplate create(RecurringChargeTemplateDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getInvoiceSubCategory())) {
            missingParameters.add("invoiceSubCategory");
        }
        if (StringUtils.isBlank(postData.getCalendar())) {
            missingParameters.add("calendar");
        }

        handleMissingParameters();
        

        
        // check if code already exists
        if (recurringChargeTemplateService.findByCode(postData.getCode()) != null) {
            throw new EntityAlreadyExistsException(RecurringChargeTemplate.class, postData.getCode());
        }

        InvoiceSubCategory invoiceSubCategory = invoiceSubCategoryService.findByCode(postData.getInvoiceSubCategory());
        if (invoiceSubCategory == null) {
            throw new EntityDoesNotExistsException(InvoiceSubCategory.class, postData.getInvoiceSubCategory());
        }

        Calendar calendar = calendarService.findByCode(postData.getCalendar());
        if (calendar == null) {
            throw new EntityDoesNotExistsException(Calendar.class, postData.getCalendar());
        }

        List<TradingLanguage> tradingLanguages = tradingLanguageService.list();
        if (!tradingLanguages.isEmpty()) {
            if (postData.getLanguageDescriptions() != null) {
                for (LanguageDescriptionDto ld : postData.getLanguageDescriptions()) {
                    boolean match = false;

                    for (TradingLanguage tl : tradingLanguages) {
                        if (tl.getLanguageCode().equals(ld.getLanguageCode())) {
                            match = true;
                            break;
                        }
                    }

                    if (!match) {
                        throw new MeveoApiException(MeveoApiErrorCodeEnum.GENERIC_API_EXCEPTION, "Language " + ld.getLanguageCode() + " is not supported by the provider.");
                    }
                }
            }
        }

        RecurringChargeTemplate chargeTemplate = new RecurringChargeTemplate();
        chargeTemplate.setCode(postData.getCode());
        chargeTemplate.setDescription(postData.getDescription());
        chargeTemplate.setDisabled(postData.isDisabled());
        chargeTemplate.setAmountEditable(postData.getAmountEditable());
        chargeTemplate.setDurationTermInMonth(postData.getDurationTermInMonth());
        chargeTemplate.setSubscriptionProrata(postData.getSubscriptionProrata());
        chargeTemplate.setTerminationProrata(postData.getTerminationProrata());
        chargeTemplate.setApplyInAdvance(postData.getApplyInAdvance());
        chargeTemplate.setShareLevel(LevelEnum.getValue(postData.getShareLevel()));
        chargeTemplate.setInvoiceSubCategory(invoiceSubCategory);
        chargeTemplate.setCalendar(calendar);
        chargeTemplate.setUnitMultiplicator(postData.getUnitMultiplicator());
        chargeTemplate.setRatingUnitDescription(postData.getRatingUnitDescription());
        chargeTemplate.setUnitNbDecimal(postData.getUnitNbDecimal());
        chargeTemplate.setInputUnitDescription(postData.getInputUnitDescription());
        chargeTemplate.setFilterExpression(postData.getFilterExpression());
        if (postData.getRoundingModeDtoEnum() != null) {
            chargeTemplate.setRoundingMode(postData.getRoundingModeDtoEnum());
        } else {
            chargeTemplate.setRoundingMode(RoundingModeEnum.NEAREST);
        }
        
        if(postData.getRevenueRecognitionRuleCode()!=null){
        	RevenueRecognitionRule revenueRecognitionRule = revenueRecognitionRuleService.findByCode(postData.getRevenueRecognitionRuleCode());
        	chargeTemplate.setRevenueRecognitionRule(revenueRecognitionRule);
        }

        if (postData.getTriggeredEdrs() != null) {
            List<TriggeredEDRTemplate> edrTemplates = new ArrayList<TriggeredEDRTemplate>();

            for (TriggeredEdrTemplateDto triggeredEdrTemplateDto : postData.getTriggeredEdrs().getTriggeredEdr()) {
                TriggeredEDRTemplate triggeredEdrTemplate = triggeredEDRTemplateService.findByCode(triggeredEdrTemplateDto.getCode());
                if (triggeredEdrTemplate == null) {
                    throw new EntityDoesNotExistsException(TriggeredEDRTemplate.class, triggeredEdrTemplateDto.getCode());
                }

                edrTemplates.add(triggeredEdrTemplate);
            }

            chargeTemplate.setEdrTemplates(edrTemplates);
        }

        // populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), chargeTemplate, true);
        } catch (MissingParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        recurringChargeTemplateService.create(chargeTemplate);

        // create cat messages
        if (postData.getLanguageDescriptions() != null) {
            for (LanguageDescriptionDto ld : postData.getLanguageDescriptions()) {
                CatMessages catMsg = new CatMessages(RecurringChargeTemplate.class.getSimpleName() , chargeTemplate.getCode(), ld.getLanguageCode(), ld.getDescription());

                catMessagesService.create(catMsg);
            }
        }

        return chargeTemplate;
    }

    public RecurringChargeTemplate update(RecurringChargeTemplateDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getInvoiceSubCategory())) {
            missingParameters.add("invoiceSubCategory");
        }
        if (StringUtils.isBlank(postData.getCalendar())) {
            missingParameters.add("calendar");
        }

        handleMissingParameters();
        

        
        // check if code already exists       
        RecurringChargeTemplate chargeTemplate = recurringChargeTemplateService.findByCode(postData.getCode());
        if (chargeTemplate == null) {
            throw new EntityDoesNotExistsException(RecurringChargeTemplate.class, postData.getCode());
        }

        InvoiceSubCategory invoiceSubCategory = invoiceSubCategoryService.findByCode(postData.getInvoiceSubCategory());
        if (invoiceSubCategory == null) {
            throw new EntityDoesNotExistsException(InvoiceSubCategory.class, postData.getInvoiceSubCategory());
        }

        Calendar calendar = calendarService.findByCode(postData.getCalendar());
        if (calendar == null) {
            throw new EntityDoesNotExistsException(Calendar.class, postData.getCalendar());
        }
        chargeTemplate.setCode(StringUtils.isBlank(postData.getUpdatedCode())?postData.getCode():postData.getUpdatedCode());
        chargeTemplate.setDescription(postData.getDescription());
        chargeTemplate.setDisabled(postData.isDisabled());
        chargeTemplate.setAmountEditable(postData.getAmountEditable());
        chargeTemplate.setDurationTermInMonth(postData.getDurationTermInMonth());
        chargeTemplate.setSubscriptionProrata(postData.getSubscriptionProrata());
        chargeTemplate.setTerminationProrata(postData.getTerminationProrata());
        chargeTemplate.setApplyInAdvance(postData.getApplyInAdvance());
        chargeTemplate.setShareLevel(LevelEnum.getValue(postData.getShareLevel()));
        chargeTemplate.setInvoiceSubCategory(invoiceSubCategory);
        chargeTemplate.setCalendar(calendar);
        chargeTemplate.setUnitMultiplicator(postData.getUnitMultiplicator());
        chargeTemplate.setRatingUnitDescription(postData.getRatingUnitDescription());
        chargeTemplate.setUnitNbDecimal(postData.getUnitNbDecimal());
        chargeTemplate.setInputUnitDescription(postData.getInputUnitDescription());
        chargeTemplate.setFilterExpression(postData.getFilterExpression());
        if (postData.getRoundingModeDtoEnum() != null) {
            chargeTemplate.setRoundingMode(postData.getRoundingModeDtoEnum());
        } else {
            chargeTemplate.setRoundingMode(RoundingModeEnum.NEAREST);
        }
        
        if(postData.getRevenueRecognitionRuleCode()!=null){
        	RevenueRecognitionRule revenueRecognitionRule = revenueRecognitionRuleService.findByCode(postData.getRevenueRecognitionRuleCode());
        	chargeTemplate.setRevenueRecognitionRule(revenueRecognitionRule);
        }

        List<TradingLanguage> tradingLanguages = tradingLanguageService.list();
        if (!tradingLanguages.isEmpty()) {
            if (postData.getLanguageDescriptions() != null) {
                for (LanguageDescriptionDto ld : postData.getLanguageDescriptions()) {
                    boolean match = false;

                    for (TradingLanguage tl : tradingLanguages) {
                        if (tl.getLanguageCode().equals(ld.getLanguageCode())) {
                            match = true;
                            break;
                        }
                    }

                    if (!match) {
                        throw new MeveoApiException(MeveoApiErrorCodeEnum.GENERIC_API_EXCEPTION, "Language " + ld.getLanguageCode() + " is not supported by the provider.");
                    }
                }

                // create cat messages
                for (LanguageDescriptionDto ld : postData.getLanguageDescriptions()) {
                    CatMessages catMsg = catMessagesService.getCatMessages(chargeTemplate.getCode(),RecurringChargeTemplate.class.getSimpleName(), ld.getLanguageCode());

                    if (catMsg != null) {
                        catMsg.setDescription(ld.getDescription());
                        catMessagesService.update(catMsg);
                    } else {
                        CatMessages catMessages = new CatMessages(RecurringChargeTemplate.class.getSimpleName() , chargeTemplate.getCode(), ld.getLanguageCode(),
                            ld.getDescription());
                        catMessagesService.create(catMessages);
                    }
                }
            }
        }

        if (postData.getTriggeredEdrs() != null) {
            List<TriggeredEDRTemplate> edrTemplates = new ArrayList<TriggeredEDRTemplate>();

            for (TriggeredEdrTemplateDto triggeredEdrTemplateDto : postData.getTriggeredEdrs().getTriggeredEdr()) {
                TriggeredEDRTemplate triggeredEdrTemplate = triggeredEDRTemplateService.findByCode(triggeredEdrTemplateDto.getCode());
                if (triggeredEdrTemplate == null) {
                    throw new EntityDoesNotExistsException(TriggeredEDRTemplate.class, triggeredEdrTemplateDto.getCode());
                }

                edrTemplates.add(triggeredEdrTemplate);
            }

            chargeTemplate.setEdrTemplates(edrTemplates);
        }

        // populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), chargeTemplate, false);
        } catch (MissingParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        chargeTemplate = recurringChargeTemplateService.update(chargeTemplate);
        
        return chargeTemplate;
    }

    /* (non-Javadoc)
     * @see org.meveo.api.ApiService#find(java.lang.String)
     */
    @Override
    public RecurringChargeTemplateDto find(String code) throws EntityDoesNotExistsException, MissingParameterException, InvalidParameterException, MeveoApiException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("recurringChargeTemplateCode");
            handleMissingParameters();
        }


        // check if code already exists
        RecurringChargeTemplate chargeTemplate = recurringChargeTemplateService.findByCode(code, Arrays.asList("invoiceSubCategory", "calendar"));
        if (chargeTemplate == null) {
            throw new EntityDoesNotExistsException(RecurringChargeTemplate.class, code);
        }

        RecurringChargeTemplateDto result = new RecurringChargeTemplateDto(chargeTemplate, entityToDtoConverter.getCustomFieldsDTO(chargeTemplate));

        List<LanguageDescriptionDto> languageDescriptions = new ArrayList<LanguageDescriptionDto>();
        for (CatMessages msg : catMessagesService.getCatMessagesList(RecurringChargeTemplate.class.getSimpleName() , chargeTemplate.getCode())) {
            languageDescriptions.add(new LanguageDescriptionDto(msg.getLanguageCode(), msg.getDescription()));
        }

        result.setLanguageDescriptions(languageDescriptions);

        return result;
    }
    
    /* (non-Javadoc)
     * @see org.meveo.api.ApiService#findIgnoreNotFound(java.lang.String)
     */
    @Override
    public RecurringChargeTemplateDto findIgnoreNotFound(String code) throws MissingParameterException, InvalidParameterException, MeveoApiException {
        try {
            return find(code);
        } catch (EntityDoesNotExistsException e) {
            return null;
        }
    }

    public void remove(String code) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("recurringChargeTemplateCode");
            handleMissingParameters();
        }

        // check if code already exists
        RecurringChargeTemplate chargeTemplate = recurringChargeTemplateService.findByCode(code);
        if (chargeTemplate == null) {
            throw new EntityDoesNotExistsException(RecurringChargeTemplate.class, code);
        }

        recurringChargeTemplateService.remove(chargeTemplate);
    }

    public RecurringChargeTemplate createOrUpdate(RecurringChargeTemplateDto postData) throws MeveoApiException, BusinessException {    	
        if (recurringChargeTemplateService.findByCode(postData.getCode()) == null) {
            return create(postData);
        } else {
            return update(postData);
        }
    }
}
