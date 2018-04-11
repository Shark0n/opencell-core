package org.meveo.api.dto.catalog;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.CustomFieldsDto;
import org.meveo.api.dto.EnableBusinessDto;
import org.meveo.api.dto.LanguageDescriptionDto;
import org.meveo.model.catalog.PricePlanMatrix;

/**
 * DTO for {@link PricePlanMatrix}.
 * 
 * @author Edward P. Legaspi
 * @lastModifiedVersion 5.0
 */
@XmlRootElement(name = "PricePlan")
@XmlAccessorType(XmlAccessType.FIELD)
public class PricePlanMatrixDto extends EnableBusinessDto {

    private static final long serialVersionUID = -9089693491690592072L;

    @XmlElement(required = true)
    private String eventCode;

    private String seller;
    private String country;
    private String currency;
    private BigDecimal minQuantity;
    private BigDecimal maxQuantity;
    private String offerTemplate;
    private OfferTemplateDto offerTemplateVersion;

    private Date startSubscriptionDate;
    private Date endSubscriptionDate;
    private Date startRatingDate;
    private Date endRatingDate;

    private Long minSubscriptionAgeInMonth;
    private Long maxSubscriptionAgeInMonth;

    private BigDecimal amountWithoutTax;
    private BigDecimal amountWithTax;
    private String amountWithoutTaxEL;
    private String amountWithTaxEL;

    private String minimumAmountWithoutTaxEl;
    private String minimumAmountWithTaxEl;

    private int priority;

    private String criteria1;
    private String criteria2;
    private String criteria3;
    private String criteriaEL;

    private String validityCalendarCode;

    private String scriptInstance;

    private CustomFieldsDto customFields;

    private List<LanguageDescriptionDto> languageDescriptions;

    private String woDescriptionEL;

    /**
     * If this EL is not null, evaluate and set in WalletOperation amounts during amount calculation in RatingService.
     */
    private String ratingEL;

    public PricePlanMatrixDto() {

    }

    /**
     * Convert PricePlanMatrix entity to DTO including its custom field values
     * 
     * @param pricePlan Price plan entity
     * @param customFieldInstances Custom field values
     */
    public PricePlanMatrixDto(PricePlanMatrix pricePlan, CustomFieldsDto customFieldInstances) {
        super(pricePlan);

        eventCode = pricePlan.getEventCode();
        if (pricePlan.getSeller() != null) {
            seller = pricePlan.getSeller().getCode();
        }
        if (pricePlan.getTradingCountry() != null) {
            country = pricePlan.getTradingCountry().getCountryCode();
        }
        if (pricePlan.getTradingCurrency() != null) {
            currency = pricePlan.getTradingCurrency().getCurrencyCode();
        }
        if (pricePlan.getOfferTemplate() != null) {
            offerTemplateVersion = new OfferTemplateDto(pricePlan.getOfferTemplate(), null, true);
            offerTemplate = pricePlan.getOfferTemplate().getCode();
        }
        minQuantity = pricePlan.getMinQuantity();
        maxQuantity = pricePlan.getMaxQuantity();
        startSubscriptionDate = pricePlan.getStartRatingDate();
        endSubscriptionDate = pricePlan.getEndSubscriptionDate();
        startRatingDate = pricePlan.getStartRatingDate();
        endRatingDate = pricePlan.getEndRatingDate();
        minSubscriptionAgeInMonth = pricePlan.getMinSubscriptionAgeInMonth();
        maxSubscriptionAgeInMonth = pricePlan.getMaxSubscriptionAgeInMonth();
        amountWithoutTax = pricePlan.getAmountWithoutTax();
        amountWithTax = pricePlan.getAmountWithTax();
        amountWithoutTaxEL = pricePlan.getAmountWithoutTaxEL();
        amountWithTaxEL = pricePlan.getAmountWithTaxEL();
        priority = pricePlan.getPriority();
        criteria1 = pricePlan.getCriteria1Value();
        criteria2 = pricePlan.getCriteria2Value();
        criteria3 = pricePlan.getCriteria3Value();
        if (pricePlan.getValidityCalendar() != null) {
            validityCalendarCode = pricePlan.getValidityCalendar().getCode();
        }

        criteriaEL = pricePlan.getCriteriaEL();
        if (pricePlan.getScriptInstance() != null) {
            scriptInstance = pricePlan.getScriptInstance().getCode();
        }
        customFields = customFieldInstances;
        setLanguageDescriptions(LanguageDescriptionDto.convertMultiLanguageFromMapOfValues(pricePlan.getDescriptionI18n()));
        woDescriptionEL = pricePlan.getWoDescriptionEL();
        ratingEL = pricePlan.getRatingEL();
        minimumAmountWithoutTaxEl = pricePlan.getMinimumAmountWithoutTaxEl();
        minimumAmountWithTaxEl = pricePlan.getMinimumAmountWithTaxEl();
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(BigDecimal minQuantity) {
        this.minQuantity = minQuantity;
    }

    public BigDecimal getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(BigDecimal maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public OfferTemplateDto getOfferTemplateVersion() {
        return offerTemplateVersion;
    }

    public void setOfferTemplateVersion(OfferTemplateDto offerTemplateVersion) {
        this.offerTemplateVersion = offerTemplateVersion;
    }

    public String getOfferTemplate() {
        return offerTemplate;
    }

    public void setOfferTemplate(String offerTemplate) {
        this.offerTemplate = offerTemplate;
    }

    public Date getStartSubscriptionDate() {
        return startSubscriptionDate;
    }

    public void setStartSubscriptionDate(Date startSubscriptionDate) {
        this.startSubscriptionDate = startSubscriptionDate;
    }

    public Date getEndSubscriptionDate() {
        return endSubscriptionDate;
    }

    public void setEndSubscriptionDate(Date endSubscriptionDate) {
        this.endSubscriptionDate = endSubscriptionDate;
    }

    public Date getStartRatingDate() {
        return startRatingDate;
    }

    public void setStartRatingDate(Date startRatingDate) {
        this.startRatingDate = startRatingDate;
    }

    public Date getEndRatingDate() {
        return endRatingDate;
    }

    public void setEndRatingDate(Date endRatingDate) {
        this.endRatingDate = endRatingDate;
    }

    public Long getMinSubscriptionAgeInMonth() {
        return minSubscriptionAgeInMonth;
    }

    public void setMinSubscriptionAgeInMonth(Long minSubscriptionAgeInMonth) {
        this.minSubscriptionAgeInMonth = minSubscriptionAgeInMonth;
    }

    public Long getMaxSubscriptionAgeInMonth() {
        return maxSubscriptionAgeInMonth;
    }

    public void setMaxSubscriptionAgeInMonth(Long maxSubscriptionAgeInMonth) {
        this.maxSubscriptionAgeInMonth = maxSubscriptionAgeInMonth;
    }

    public BigDecimal getAmountWithoutTax() {
        return amountWithoutTax;
    }

    public void setAmountWithoutTax(BigDecimal amountWithoutTax) {
        this.amountWithoutTax = amountWithoutTax;
    }

    public BigDecimal getAmountWithTax() {
        return amountWithTax;
    }

    public void setAmountWithTax(BigDecimal amountWithTax) {
        this.amountWithTax = amountWithTax;
    }

    public String getAmountWithoutTaxEL() {
        return amountWithoutTaxEL;
    }

    public void setAmountWithoutTaxEL(String amountWithoutTaxEL) {
        this.amountWithoutTaxEL = amountWithoutTaxEL;
    }

    public String getAmountWithTaxEL() {
        return amountWithTaxEL;
    }

    public void setAmountWithTaxEL(String amountWithTaxEL) {
        this.amountWithTaxEL = amountWithTaxEL;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCriteria1() {
        return criteria1;
    }

    public void setCriteria1(String criteria1) {
        this.criteria1 = criteria1;
    }

    public String getCriteria2() {
        return criteria2;
    }

    public void setCriteria2(String criteria2) {
        this.criteria2 = criteria2;
    }

    public String getCriteria3() {
        return criteria3;
    }

    public void setCriteria3(String criteria3) {
        this.criteria3 = criteria3;
    }

    @Override
    public String toString() {
        return "PricePlanDto [code=" + code + ", eventCode=" + eventCode + ", description=" + description + ", seller=" + seller + ", country=" + country + ", currency=" + currency
                + ", minQuantity=" + minQuantity + ", maxQuantity=" + maxQuantity + ", offerTemplate=" + offerTemplateVersion + ", startSubscriptionDate=" + startSubscriptionDate
                + ", endSubscriptionDate=" + endSubscriptionDate + ", startRatingDate=" + startRatingDate + ", endRatingDate=" + endRatingDate + ", minSubscriptionAgeInMonth="
                + minSubscriptionAgeInMonth + ", maxSubscriptionAgeInMonth=" + maxSubscriptionAgeInMonth + ", amountWithoutTax=" + amountWithoutTax + ", amountWithTax="
                + amountWithTax + ", amountWithoutTaxEL=" + amountWithoutTaxEL + ", amountWithTaxEL=" + amountWithTaxEL + ", priority=" + priority + ", criteria1=" + criteria1
                + ", criteria2=" + criteria2 + ", criteria3=" + criteria3 + ", validityCalendarCode=" + validityCalendarCode + ", scriptInstance=" + scriptInstance + "]";
    }

    public String getValidityCalendarCode() {
        return validityCalendarCode;
    }

    public void setValidityCalendarCode(String validityCalendarCode) {
        this.validityCalendarCode = validityCalendarCode;
    }

    public String getCriteriaEL() {
        return criteriaEL;
    }

    public void setCriteriaEL(String criteriaEL) {
        this.criteriaEL = criteriaEL;
    }

    public String getScriptInstance() {
        return scriptInstance;
    }

    public void setScriptInstance(String scriptInstance) {
        this.scriptInstance = scriptInstance;
    }

    public CustomFieldsDto getCustomFields() {
        return customFields;
    }

    public void setCustomFields(CustomFieldsDto customFields) {
        this.customFields = customFields;
    }

    public List<LanguageDescriptionDto> getLanguageDescriptions() {
        return languageDescriptions;
    }

    public void setLanguageDescriptions(List<LanguageDescriptionDto> languageDescriptions) {
        this.languageDescriptions = languageDescriptions;
    }

    public String getWoDescriptionEL() {
        return woDescriptionEL;
    }

    public void setWoDescriptionEL(String woDescriptionEL) {
        this.woDescriptionEL = woDescriptionEL;
    }

    public String getRatingEL() {
        return ratingEL;
    }

    public void setRatingEL(String ratingEL) {
        this.ratingEL = ratingEL;
    }

    public String getMinimumAmountWithoutTaxEl() {
        return minimumAmountWithoutTaxEl;
    }

    public void setMinimumAmountWithoutTaxEl(String minimumAmountWithoutTaxEl) {
        this.minimumAmountWithoutTaxEl = minimumAmountWithoutTaxEl;
    }

    public String getMinimumAmountWithTaxEl() {
        return minimumAmountWithTaxEl;
    }

    public void setMinimumAmountWithTaxEl(String minimumAmountWithTaxEl) {
        this.minimumAmountWithTaxEl = minimumAmountWithTaxEl;
    }
}