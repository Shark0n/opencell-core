package org.meveo.service.catalog.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.DatePeriod;
import org.meveo.model.catalog.Channel;
import org.meveo.model.catalog.DigitalResource;
import org.meveo.model.catalog.OfferTemplateCategory;
import org.meveo.model.catalog.ProductTemplate;
import org.meveo.model.catalog.WalletTemplate;
import org.meveo.model.crm.BusinessAccountModel;
import org.meveo.service.base.MultilanguageEntityService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;

@Stateless
public class ProductTemplateService extends MultilanguageEntityService<ProductTemplate> {

	@Inject
	private CustomFieldInstanceService customFieldInstanceService;

	public long countProductTemplateActive(boolean status) {
		long result = 0;

		Query query;

		if (status) {
			query = getEntityManager().createNamedQuery("ProductTemplate.countActive");
		} else {
			query = getEntityManager().createNamedQuery("ProductTemplate.countDisabled");
		}

		result = (long) query.getSingleResult();
		return result;
	}

	public long countProductTemplateExpiring() {	
        Long result = 0L;
        Query query = getEntityManager().createNamedQuery("ProductTemplate.countExpiring");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        query.setParameter("nowMinus1Day", c.getTime());

        try {
            result = (long) query.getSingleResult();
        } catch (NoResultException e) {

        }
        return result;		
	}


    /**
     * Create a shallow duplicate of an Product template (main Product template information and custom fields). A new Product template will have a code with suffix "- Copy"
     * 
     * @param product Product template to duplicate
     * @return A persisted duplicated Bundle template
     * @throws BusinessException
     */
    public synchronized void duplicate(ProductTemplate product) throws BusinessException {
        duplicate(product, true);
    }
    
    /**
     * Create a new version of an Product template. It is a shallow copy of an Product template (main Product template information and custom fields) with identical code and validity start date matching latest version's validity end
     * date or current date.
     * 
     * @param product Product template to create new version for
     * @return A not-persisted copy of Product template
     * @throws BusinessException
     */
    public synchronized ProductTemplate instantiateNewVersion(ProductTemplate product) throws BusinessException {

        // Find the latest version of an offer for duplication and to calculate a validity start date for a new offer
        ProductTemplate latestVersion = findTheLatestVersion(product.getCode());
        String code = latestVersion.getCode();
        Date endDate = latestVersion.getValidity().getTo();

        product = duplicate(latestVersion, false);

        product.setCode(code);

        Date from = endDate != null ? endDate : new Date();
        product.setValidity(new DatePeriod(from, null));

        return product;
    }
    
    /**
     * Create a duplicate of a given Product template. It is a shallow copy of an Product template (main Product template information and custom fields)
     * 
     * @param product Product template to duplicate
     * @param persist Shall new entity be persisted
     * @return A copy of Product template
     * @throws BusinessException
     */
	private synchronized ProductTemplate duplicate(ProductTemplate product, boolean persist) throws BusinessException {

	    product = refreshOrRetrieve(product);

		// Lazy load related values first
		product.getWalletTemplates().size();
		product.getBusinessAccountModels().size();
		product.getAttachments().size();
		product.getChannels().size();
		product.getOfferTemplateCategories().size();

		String code = findDuplicateCode(product);

		// Detach and clear ids of entity and related entities
		detach(product);
		product.setId(null);
		String sourceAppliesToEntity = product.clearUuid();

		List<BusinessAccountModel> businessAccountModels = product.getBusinessAccountModels();
		product.setBusinessAccountModels(new ArrayList<BusinessAccountModel>());

		List<DigitalResource> attachments = product.getAttachments();
		product.setAttachments(new ArrayList<DigitalResource>());

		List<Channel> channels = product.getChannels();
		product.setChannels(new ArrayList<Channel>());

		List<OfferTemplateCategory> offerTemplateCategories = product.getOfferTemplateCategories();
		product.setOfferTemplateCategories(new ArrayList<OfferTemplateCategory>());

		List<WalletTemplate> walletTemplates = product.getWalletTemplates();
		product.setWalletTemplates(new ArrayList<WalletTemplate>());

		product.setCode(code);

		if (businessAccountModels != null) {
			for (BusinessAccountModel bam : businessAccountModels) {
				product.getBusinessAccountModels().add(bam);
			}
		}

		if (attachments != null) {
			for (DigitalResource attachment : attachments) {
				product.addAttachment(attachment);
			}
		}

		if (channels != null) {
			for (Channel channel : channels) {
				product.getChannels().add(channel);
			}
		}

		if (offerTemplateCategories != null) {
			for (OfferTemplateCategory offerTemplateCategory : offerTemplateCategories) {
				product.getOfferTemplateCategories().add(offerTemplateCategory);
			}
		}

		if (walletTemplates != null) {
			for (WalletTemplate wt : walletTemplates) {
				product.getWalletTemplates().add(wt);
			}
		}

		customFieldInstanceService.duplicateCfValues(sourceAppliesToEntity, product);

		if (persist){
		    create(product);        
		}

		return product;
	}

    /**
     * Find the latest version of Product template matching a given code
     * 
     * @param code Code to match
     * @return Product template with the highest validity start date
     */
    private ProductTemplate findTheLatestVersion(String code) {

        ProductTemplate latestVersion = (ProductTemplate) getEntityManager().createNamedQuery("ProductOffering.findLatestVersion").setParameter("code", code).setMaxResults(1)
            .getSingleResult();
        return latestVersion;
    }
}