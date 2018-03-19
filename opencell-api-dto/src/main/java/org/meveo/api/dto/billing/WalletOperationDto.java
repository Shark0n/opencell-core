package org.meveo.api.dto.billing;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseDto;
import org.meveo.model.billing.OperationTypeEnum;
import org.meveo.model.billing.WalletOperation;
import org.meveo.model.billing.WalletOperationStatusEnum;

/**
 * @author Edward P. Legaspi
 **/
@XmlRootElement(name = "WalletOperation")
@XmlAccessorType(XmlAccessType.FIELD)
public class WalletOperationDto extends BaseDto {

    private static final long serialVersionUID = -1920217666509809184L;

    @XmlAttribute(required = true)
    private String code;

    @XmlAttribute()
    private String description;

    private String userAccount;

    private String subscription;

    private String walletTemplate;

    private String seller;

    private String chargeInstance;
    private Long chargeInstanceId;

    private String currency;
    private OperationTypeEnum type;
    private WalletOperationStatusEnum status;

    private String ratingUnitDescription;
    private BigDecimal taxPercent;
    private BigDecimal unitAmountWithoutTax;
    private BigDecimal unitAmountWithTax;
    private BigDecimal unitAmountTax;
    private BigDecimal quantity;
    private BigDecimal amountWithoutTax;
    private BigDecimal amountWithTax;
    private BigDecimal amountTax;
    private String parameter1;
    private String parameter2;
    private String parameter3;
    private String parameterExtra;
    private String orderNumber;
    private Date startDate;
    private Date endDate;
    private Date operationDate;
    private Date subscriptionDate;
    private String offerCode;
    private BigDecimal rawAmountWithoutTax;
    private BigDecimal rawAmountWithTax;

    public WalletOperationDto() {

    }

    public WalletOperationDto(WalletOperation wo) {
        code = wo.getCode();
        description = wo.getDescription();
        seller = wo.getSeller().getCode();

        if (wo.getWallet() != null && wo.getWallet().getWalletTemplate() != null) {
            walletTemplate = wo.getWallet().getWalletTemplate().getCode();
        }

        currency = wo.getCurrency().getCurrencyCode();
        type = wo.getType();
        status = wo.getStatus();
        ratingUnitDescription = wo.getRatingUnitDescription();
        taxPercent = wo.getTaxPercent();
        unitAmountWithoutTax = wo.getUnitAmountWithoutTax();
        unitAmountWithTax = wo.getUnitAmountWithTax();
        unitAmountTax = wo.getUnitAmountTax();
        quantity = wo.getQuantity();
        amountWithoutTax = wo.getAmountWithoutTax();
        amountWithTax = wo.getAmountWithTax();
        amountTax = wo.getAmountTax();
        parameter1 = wo.getParameter1();
        parameter2 = wo.getParameter2();
        parameter3 = wo.getParameter3();
        parameterExtra = wo.getParameterExtra();
        startDate = wo.getStartDate();
        endDate = wo.getEndDate();
        operationDate = wo.getOperationDate();
        subscriptionDate = wo.getSubscriptionDate();
        walletTemplate = wo.getWallet().getCode();
        userAccount = wo.getWallet().getUserAccount().getCode();
        offerCode = wo.getOfferCode();
        chargeInstance = wo.getChargeInstance().getCode();
        chargeInstanceId = wo.getChargeInstance().getId();
        rawAmountWithoutTax = wo.getRawAmountWithoutTax();
        rawAmountWithTax = wo.getRawAmountWithTax();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public OperationTypeEnum getType() {
        return type;
    }

    public void setType(OperationTypeEnum type) {
        this.type = type;
    }

    public WalletOperationStatusEnum getStatus() {
        return status;
    }

    public void setStatus(WalletOperationStatusEnum status) {
        this.status = status;
    }

    public Date getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(Date operationDate) {
        this.operationDate = operationDate;
    }

    public BigDecimal getTaxPercent() {
        return taxPercent;
    }

    public void setTaxPercent(BigDecimal taxPercent) {
        this.taxPercent = taxPercent;
    }

    public BigDecimal getUnitAmountWithoutTax() {
        return unitAmountWithoutTax;
    }

    public void setUnitAmountWithoutTax(BigDecimal unitAmountWithoutTax) {
        this.unitAmountWithoutTax = unitAmountWithoutTax;
    }

    public BigDecimal getUnitAmountWithTax() {
        return unitAmountWithTax;
    }

    public void setUnitAmountWithTax(BigDecimal unitAmountWithTax) {
        this.unitAmountWithTax = unitAmountWithTax;
    }

    public BigDecimal getUnitAmountTax() {
        return unitAmountTax;
    }

    public void setUnitAmountTax(BigDecimal unitAmountTax) {
        this.unitAmountTax = unitAmountTax;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
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

    public BigDecimal getAmountTax() {
        return amountTax;
    }

    public void setAmountTax(BigDecimal amountTax) {
        this.amountTax = amountTax;
    }

    public String getParameter1() {
        return parameter1;
    }

    public void setParameter1(String parameter1) {
        this.parameter1 = parameter1;
    }

    public String getParameter2() {
        return parameter2;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }

    public String getParameter3() {
        return parameter3;
    }

    public void setParameter3(String parameter3) {
        this.parameter3 = parameter3;
    }

    public String getParameterExtra() {
        return parameterExtra;
    }

    public void setParameterExtra(String parameterExtra) {
        this.parameterExtra = parameterExtra;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(Date subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    @Override
    public String toString() {
        return "WalletOperationDto [code=" + code + ", description=" + description + ", userAccount=" + userAccount + ", subscription=" + subscription + ", walletTemplate="
                + walletTemplate + ", seller=" + seller + ", chargeInstance=" + chargeInstance + ", chargeInstanceId=" + chargeInstanceId + ", currency=" + currency + ", type="
                + type + ", status=" + status + ", ratingUnitDescription=" + ratingUnitDescription + ", taxPercent=" + taxPercent + ", unitAmountWithoutTax=" + unitAmountWithoutTax
                + ", unitAmountWithTax=" + unitAmountWithTax + ", unitAmountTax=" + unitAmountTax + ", quantity=" + quantity + ", amountWithoutTax=" + amountWithoutTax
                + ", amountWithTax=" + amountWithTax + ", amountTax=" + amountTax + ", parameter1=" + parameter1 + ", parameter2=" + parameter2 + ", parameter3=" + parameter3
                + ", parameterExtra=" + parameterExtra + ", orderNumber=" + orderNumber + ", startDate=" + startDate + ", endDate=" + endDate + ", operationDate=" + operationDate
                + ", subscriptionDate=" + subscriptionDate + ", offerCode=" + offerCode + "]";
    }

    public String getWalletTemplate() {
        return walletTemplate;
    }

    public void setWalletTemplate(String walletTemplate) {
        this.walletTemplate = walletTemplate;
    }

    public String getChargeInstance() {
        return chargeInstance;
    }

    public void setChargeInstance(String chargeInstance) {
        this.chargeInstance = chargeInstance;
    }

    public Long getChargeInstanceId() {
        return chargeInstanceId;
    }

    public void setChargeInstanceId(Long chargeInstanceId) {
        this.chargeInstanceId = chargeInstanceId;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRatingUnitDescription() {
        return ratingUnitDescription;
    }

    public void setRatingUnitDescription(String ratingUnitDescription) {
        this.ratingUnitDescription = ratingUnitDescription;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public BigDecimal getRawAmountWithoutTax() {
        return rawAmountWithoutTax;
    }

    public void setRawAmountWithoutTax(BigDecimal rawAmountWithoutTax) {
        this.rawAmountWithoutTax = rawAmountWithoutTax;
    }

    public BigDecimal getRawAmountWithTax() {
        return rawAmountWithTax;
    }

    public void setRawAmountWithTax(BigDecimal rawAmountWithTax) {
        this.rawAmountWithTax = rawAmountWithTax;
    }

}
