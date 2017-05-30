package org.meveo.api.catalog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

import org.meveo.api.BaseApi;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.catalog.OfferProductTemplate;
import org.meveo.model.catalog.OfferServiceTemplate;
import org.meveo.model.catalog.OfferTemplate;
import org.meveo.model.catalog.PricePlanMatrix;
import org.meveo.model.catalog.ProductChargeTemplate;
import org.meveo.model.catalog.ProductTemplate;
import org.meveo.model.catalog.ServiceChargeTemplateRecurring;
import org.meveo.model.catalog.ServiceChargeTemplateSubscription;
import org.meveo.model.catalog.ServiceTemplate;
import org.meveo.service.catalog.impl.OfferTemplateService;
import org.meveo.service.catalog.impl.PricePlanMatrixService;
import org.tmf.dsmapi.catalog.resource.category.Category;
import org.tmf.dsmapi.catalog.resource.product.Price;
import org.tmf.dsmapi.catalog.resource.product.ProductOffering;
import org.tmf.dsmapi.catalog.resource.product.ProductOfferingPrice;
import org.tmf.dsmapi.catalog.resource.product.ProductOfferingPriceType;
import org.tmf.dsmapi.catalog.resource.product.ProductSpecification;

@Stateless
public class CatalogApi extends BaseApi {

	@Inject
	private OfferTemplateService offerTemplateService;

	@Inject
	private PricePlanMatrixService pricePlanMatrixService;

	public ProductOffering findProductOffering(String code, UriInfo uriInfo, Category category) throws EntityDoesNotExistsException {
		OfferTemplate offerTemplate = offerTemplateService.findByCode(code);
		if (offerTemplate == null) {
			throw new EntityDoesNotExistsException(OfferTemplate.class, code);
		}
		List<ProductOfferingPrice> offerPrices = getOfferPrices(offerTemplate);
		return new ProductOffering(offerTemplate, uriInfo, category, offerPrices);
	}

	public List<ProductOffering> findProductOfferings(UriInfo uriInfo, Category category) {
		List<ProductOffering> productOfferings = new ArrayList<ProductOffering>();
		List<OfferTemplate> offerTemplates = offerTemplateService.list(true);

		for (OfferTemplate offerTemplate : offerTemplates) {
			List<ProductOfferingPrice> offerPrices = getOfferPrices(offerTemplate);
			ProductOffering productOffering = new ProductOffering(offerTemplate, uriInfo, category, offerPrices);
			productOffering.setProductOfferingPrice(getOfferPrices(offerTemplate));
			productOfferings.add(productOffering);
		}
		return productOfferings;
	}

	private List<ProductOfferingPrice> getOfferPrices(OfferTemplate offerTemplate) {
		List<ProductOfferingPrice> offerPrices = new ArrayList<>();
		for (OfferServiceTemplate offerServiceTemplate : offerTemplate.getOfferServiceTemplates()) {
			if (!offerServiceTemplate.isMandatory()) {
				continue;
			}
			ServiceTemplate serviceTemplate = offerServiceTemplate.getServiceTemplate();
			offerPrices.addAll(getProductOfferingPricesFromSubscriptionCharges(offerTemplate, serviceTemplate));
			offerPrices.addAll(getProductOfferingPricesFromRecurringCharges(offerTemplate, serviceTemplate));
			offerPrices.addAll(getProductOfferingPricesFromOfferProducts(offerTemplate, serviceTemplate));
		}

		return offerPrices;
	}

	private List<ProductOfferingPrice> getProductOfferingPricesFromSubscriptionCharges(OfferTemplate offerTemplate, ServiceTemplate serviceTemplate) {
		List<ProductOfferingPrice> offerPrices = new ArrayList<>();
		if (serviceTemplate.getServiceSubscriptionCharges() != null) {
			Price price = new Price();
			price.setDutyFreeAmount(new BigDecimal(0));
			price.setTaxIncludedAmount(new BigDecimal(0));

			String chargeCode = null;
			for (ServiceChargeTemplateSubscription serviceChargeTemplateSubscription : serviceTemplate.getServiceSubscriptionCharges()) {
				
				if (serviceChargeTemplateSubscription.getChargeTemplate().getInvoiceSubCategory().getInvoiceSubcategoryCountries() != null
						&& serviceChargeTemplateSubscription.getChargeTemplate().getInvoiceSubCategory().getInvoiceSubcategoryCountries().get(0).getTax() != null) {
					price.setTaxRate(serviceChargeTemplateSubscription.getChargeTemplate().getInvoiceSubCategory().getInvoiceSubcategoryCountries().get(0).getTax().getPercent());
				}

                chargeCode = serviceChargeTemplateSubscription.getChargeTemplate().getCode();

                List<PricePlanMatrix> pricePlans = pricePlanMatrixService.findByOfferTemplateAndEventCode(offerTemplate.getCode(), chargeCode);
                if (pricePlans == null || pricePlans.isEmpty()) {
                    pricePlans = pricePlanMatrixService.findByOfferTemplateAndEventCode(null, chargeCode);
                }
                if (pricePlans != null && !pricePlans.isEmpty()) {
                    price.setDutyFreeAmount(price.getDutyFreeAmount().add(pricePlans.get(0).getAmountWithoutTax()));
                    if (!appProvider.isEntreprise()) {
                        price.setTaxIncludedAmount(price.getTaxIncludedAmount().add(pricePlans.get(0).getAmountWithTax()));
                    }
                }
            }

			if (appProvider.isEntreprise()) {
				if (price.getDutyFreeAmount() != null && price.getTaxRate() != null) {
					BigDecimal taxRate = price.getTaxRate().divide(new BigDecimal(100)).add(new BigDecimal(1));
					price.setTaxIncludedAmount(price.getDutyFreeAmount().multiply(taxRate));
				} else if (price.getDutyFreeAmount() != null && price.getTaxRate() == null) {
					price.setTaxIncludedAmount(price.getDutyFreeAmount());
				} else {
					price.setTaxIncludedAmount(new BigDecimal(0));
				}
			}

			ProductOfferingPrice offerPrice = new ProductOfferingPrice();
			offerPrice.setPriceName(serviceTemplate.getCode());
			offerPrice.setPriceType(ProductOfferingPriceType.ONE_TIME);
			offerPrice.setPrice(price);
			offerPrice.setPriceDescription(serviceTemplate.getDescriptionOrCode());
			offerPrices.add(offerPrice);
		}

		return offerPrices;
	}

	private List<ProductOfferingPrice> getProductOfferingPricesFromRecurringCharges(OfferTemplate offerTemplate, ServiceTemplate serviceTemplate) {
		List<ProductOfferingPrice> offerPrices = new ArrayList<>();
		if (serviceTemplate.getServiceRecurringCharges() != null) {
			ProductOfferingPrice offerPrice = null;
			Price price = null;
			String chargeCode = null;
			for (ServiceChargeTemplateRecurring serviceChargeTemplateRecurring : serviceTemplate.getServiceRecurringCharges()) {

				price = new Price();
				price.setDutyFreeAmount(new BigDecimal(0));
				price.setTaxIncludedAmount(new BigDecimal(0));

                chargeCode = serviceChargeTemplateRecurring.getChargeTemplate().getCode();
                
				if (serviceChargeTemplateRecurring.getChargeTemplate().getInvoiceSubCategory().getInvoiceSubcategoryCountries() != null
						&& serviceChargeTemplateRecurring.getChargeTemplate().getInvoiceSubCategory().getInvoiceSubcategoryCountries().get(0).getTax() != null) {
					price.setTaxRate(serviceChargeTemplateRecurring.getChargeTemplate().getInvoiceSubCategory().getInvoiceSubcategoryCountries().get(0).getTax().getPercent());
				}

                List<PricePlanMatrix> pricePlans = pricePlanMatrixService.findByOfferTemplateAndEventCode(offerTemplate.getCode(), chargeCode);
                if (pricePlans == null || pricePlans.isEmpty()) {
                    pricePlans = pricePlanMatrixService.findByOfferTemplateAndEventCode(null, chargeCode);
                }
                if (pricePlans != null && !pricePlans.isEmpty()) {
                    price.setDutyFreeAmount(price.getDutyFreeAmount().add(pricePlans.get(0).getAmountWithoutTax()));
                    if (!appProvider.isEntreprise()) {
                        price.setTaxIncludedAmount(
                            price.getTaxIncludedAmount().add(pricePlans.get(0).getAmountWithTax() != null ? pricePlans.get(0).getAmountWithTax() : BigDecimal.ZERO));
                    }
                }

				if (appProvider.isEntreprise()) {
					if (price.getDutyFreeAmount() != null && price.getTaxRate() != null) {
						BigDecimal taxRate = price.getTaxRate().divide(new BigDecimal(100)).add(new BigDecimal(1));
						price.setTaxIncludedAmount(price.getDutyFreeAmount().multiply(taxRate));
					} else if (price.getDutyFreeAmount() != null && price.getTaxRate() == null) {
						price.setTaxIncludedAmount(price.getDutyFreeAmount());
					} else {
						price.setTaxIncludedAmount(new BigDecimal(0));
					}
				}

				String calendarCode = serviceChargeTemplateRecurring.getChargeTemplate().getCalendar().getCode();
				String priceName = StringUtils.isBlank(calendarCode) ? serviceTemplate.getCode() : serviceTemplate.getCode() + " " + calendarCode;

				offerPrice = new ProductOfferingPrice();
				offerPrice.setPriceName(priceName);
				offerPrice.setPriceType(ProductOfferingPriceType.RECURRING);
				offerPrice.setPrice(price);
				offerPrice.setPriceDescription(serviceTemplate.getDescriptionOrCode());
				offerPrices.add(offerPrice);
			}
		}
		return offerPrices;
	}

	private List<ProductOfferingPrice> getProductOfferingPricesFromOfferProducts(OfferTemplate offerTemplate, ServiceTemplate serviceTemplate) {
		List<ProductOfferingPrice> productOfferingPrices = new ArrayList<>();
		List<OfferProductTemplate> offerProductTemplates = offerTemplate.getOfferProductTemplates();

		if (offerProductTemplates == null || offerProductTemplates.isEmpty()) {
			return productOfferingPrices;
		}

		for (OfferProductTemplate offerProductTemplate : offerProductTemplates) {

			// load all prices from the offer template
			OfferTemplate productOfferTemplate = offerProductTemplate.getOfferTemplate();
			productOfferingPrices.addAll(getOfferPrices(productOfferTemplate));
			
			// load the prices form the product template
			ProductTemplate productTemplate = offerProductTemplate.getProductTemplate();
			List<ProductChargeTemplate> productChargeTemplates = productTemplate.getProductChargeTemplates();
			
			Price price = new Price();
			price.setDutyFreeAmount(new BigDecimal(0));
			price.setTaxIncludedAmount(new BigDecimal(0));

			String chargeCode = null;
			for(ProductChargeTemplate productChargeTemplate : productChargeTemplates){
				chargeCode = productChargeTemplate.getCode();
				if (productChargeTemplate.getInvoiceSubCategory().getInvoiceSubcategoryCountries() != null
						&& productChargeTemplate.getInvoiceSubCategory().getInvoiceSubcategoryCountries().get(0).getTax() != null) {
					price.setTaxRate(productChargeTemplate.getInvoiceSubCategory().getInvoiceSubcategoryCountries().get(0).getTax().getPercent());
				}
				
				List<PricePlanMatrix> pricePlans = pricePlanMatrixService.findByOfferTemplateAndEventCode(offerTemplate.getCode(), chargeCode);
                if (pricePlans == null || pricePlans.isEmpty()) {
                    pricePlans = pricePlanMatrixService.findByOfferTemplateAndEventCode(null, chargeCode);
                }
                if (pricePlans != null && !pricePlans.isEmpty()) {
                    price.setDutyFreeAmount(price.getDutyFreeAmount().add(pricePlans.get(0).getAmountWithoutTax()));
                    if (!appProvider.isEntreprise()) {
                        price.setTaxIncludedAmount(price.getTaxIncludedAmount().add(pricePlans.get(0).getAmountWithTax()));
                    }
                }

				if (appProvider.isEntreprise()) {
					if (price.getDutyFreeAmount() != null && price.getTaxRate() != null) {
						BigDecimal taxRate = price.getTaxRate().divide(new BigDecimal(100)).add(new BigDecimal(1));
						price.setTaxIncludedAmount(price.getDutyFreeAmount().multiply(taxRate));
					} else if (price.getDutyFreeAmount() != null && price.getTaxRate() == null) {
						price.setTaxIncludedAmount(price.getDutyFreeAmount());
					} else {
						price.setTaxIncludedAmount(new BigDecimal(0));
					}
				}
			}

			ProductOfferingPrice productOfferingPrice = new ProductOfferingPrice();
			productOfferingPrice.setPriceName(serviceTemplate.getCode());
			productOfferingPrice.setPriceType(ProductOfferingPriceType.ONE_TIME);
			productOfferingPrice.setPrice(price);
			productOfferingPrice.setPriceDescription(serviceTemplate.getDescriptionOrCode());
		}

		return productOfferingPrices;
	}

	public ProductSpecification findProductSpecification(String code, UriInfo uriInfo) throws EntityDoesNotExistsException {
		OfferTemplate offerTemplate = offerTemplateService.findByCode(code);
		if (offerTemplate != null) {
			return ProductSpecification.parseFromOfferTemplate(offerTemplate, uriInfo);
		} else {
			throw new EntityDoesNotExistsException(OfferTemplate.class, code);
		}
	}

	public List<ProductSpecification> findProductSpecifications(UriInfo uriInfo) {
		List<OfferTemplate> offerTemplates = offerTemplateService.list(true);
		return ProductSpecification.parseFromOfferTemplates(offerTemplates, uriInfo);
	}

}
