package org.tmf.dsmapi.catalog.resource.product;

import java.io.Serializable;

import org.tmf.dsmapi.catalog.resource.TimeRange;
import org.tmf.dsmapi.commons.Utilities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author bahman.barzideh
 * 
 *         { "name": "Monthly Price", "description": "monthlyprice", "validFor": { "startDateTime": "2013-04-19T16:42:23-04:00", "endDateTime": "2013-06-19T00:00:00-04:00" },
 *         "priceType": "recurring", "unitOfMeasure": "", "price": { "taxIncludedAmount": "12.00", "dutyFreeAmount": "10.00", "taxRate": "20.00", "currencyCode": "EUR",
 *         "percentage": 0 }, "recurringChargePeriod": "monthly", "productOfferPriceAlteration": { "name": "Shipping Discount", "description": "One time shipping discount",
 *         "validFor": { "startDateTime": "2013-04-19T16:42:23.0Z" }, "priceType": "One Time discount", "unitOfMeasure": "", "price": { "percentage": 100 },
 *         "recurringChargePeriod": "", "priceCondition": "apply if total amount of the  order is greater than 300.00" } }
 * 
 */
@JsonInclude(value = Include.NON_NULL)
public class ProductOfferingPrice implements Serializable {
    private final static long serialVersionUID = 1L;

    @JsonProperty(value = "name")
    private String priceName;

    @JsonProperty(value = "description")
    private String priceDescription;

    @JsonProperty(value = "validFor")
    private TimeRange priceValidFor;

    private ProductOfferingPriceType priceType;

    private String unitOfMeasure;

    private Price price;

    private String recurringChargePeriod;

    private ProductOfferPriceAlteration productOfferPriceAlteration;

    public ProductOfferingPrice() {
    }

    public String getPriceName() {
        return priceName;
    }

    public void setPriceName(String priceName) {
        this.priceName = priceName;
    }

    public String getPriceDescription() {
        return priceDescription;
    }

    public void setPriceDescription(String priceDescription) {
        this.priceDescription = priceDescription;
    }

    public TimeRange getPriceValidFor() {
        return priceValidFor;
    }

    public void setPriceValidFor(TimeRange priceValidFor) {
        this.priceValidFor = priceValidFor;
    }

    public ProductOfferingPriceType getPriceType() {
        return priceType;
    }

    public void setPriceType(ProductOfferingPriceType priceType) {
        this.priceType = priceType;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public String getRecurringChargePeriod() {
        return recurringChargePeriod;
    }

    public void setRecurringChargePeriod(String recurringChargePeriod) {
        this.recurringChargePeriod = recurringChargePeriod;
    }

    public ProductOfferPriceAlteration getProductOfferPriceAlteration() {
        return productOfferPriceAlteration;
    }

    public void setProductOfferPriceAlteration(ProductOfferPriceAlteration productOfferPriceAlteration) {
        this.productOfferPriceAlteration = productOfferPriceAlteration;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 37 * hash + (this.priceName != null ? this.priceName.hashCode() : 0);
        hash = 37 * hash + (this.priceDescription != null ? this.priceDescription.hashCode() : 0);
        hash = 37 * hash + (this.priceValidFor != null ? this.priceValidFor.hashCode() : 0);
        hash = 37 * hash + (this.priceType != null ? this.priceType.hashCode() : 0);
        hash = 37 * hash + (this.unitOfMeasure != null ? this.unitOfMeasure.hashCode() : 0);
        hash = 37 * hash + (this.price != null ? this.price.hashCode() : 0);
        hash = 37 * hash + (this.recurringChargePeriod != null ? this.recurringChargePeriod.hashCode() : 0);
        hash = 37 * hash + (this.productOfferPriceAlteration != null ? this.productOfferPriceAlteration.hashCode() : 0);

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final ProductOfferingPrice other = (ProductOfferingPrice) object;
        if (Utilities.areEqual(this.priceName, other.priceName) == false) {
            return false;
        }

        if (Utilities.areEqual(this.priceDescription, other.priceDescription) == false) {
            return false;
        }

        if (Utilities.areEqual(this.priceValidFor, other.priceValidFor) == false) {
            return false;
        }

        if (this.priceType != other.priceType) {
            return false;
        }

        if (Utilities.areEqual(this.unitOfMeasure, other.unitOfMeasure) == false) {
            return false;
        }

        if (Utilities.areEqual(this.price, other.price) == false) {
            return false;
        }

        if (Utilities.areEqual(this.recurringChargePeriod, other.recurringChargePeriod) == false) {
            return false;
        }

        if (Utilities.areEqual(this.productOfferPriceAlteration, other.productOfferPriceAlteration) == false) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "ProductOfferingPrice{" + "priceName=" + priceName + ", priceDescription=" + priceDescription + ", priceValidFor=" + priceValidFor + ", priceType=" + priceType
                + ", unitOfMeasure=" + unitOfMeasure + ", price=" + price + ", recurringChargePeriod=" + recurringChargePeriod + ", productOfferPriceAlteration="
                + productOfferPriceAlteration + '}';
    }
}