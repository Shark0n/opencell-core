package org.meveo.apiv2.article.service;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

import org.meveo.apiv2.ordering.services.ApiService;
import org.meveo.model.article.AccountingArticle;
import org.meveo.model.article.ArticleMapping;
import org.meveo.model.article.ArticleMappingLine;
import org.meveo.model.article.AttributeMapping;
import org.meveo.model.catalog.ChargeTemplate;
import org.meveo.model.catalog.OfferTemplate;
import org.meveo.model.cpq.Attribute;
import org.meveo.model.cpq.Product;
import org.meveo.service.billing.impl.article.AccountingArticleService;
import org.meveo.service.billing.impl.article.ArticleMappingLineService;
import org.meveo.service.billing.impl.article.ArticleMappingService;
import org.meveo.service.catalog.impl.ChargeTemplateService;
import org.meveo.service.catalog.impl.OfferTemplateService;
import org.meveo.service.cpq.AttributeService;
import org.meveo.service.cpq.ProductService;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArticleMappingLineApiService implements ApiService<ArticleMappingLine> {

    @Inject
    private AccountingArticleService accountingArticleApiService;
    @Inject
    private ArticleMappingService articleMappingApiService;
    @Inject
    private ArticleMappingLineService articleMappingLineService;
    @Inject
    private OfferTemplateService offerTemplateService;
    @Inject
    private ChargeTemplateService<ChargeTemplate> chargeTemplateService;
    @Inject
    private AttributeService attributeService;
    @Inject
    private ProductService productService;

    private List<String> fields =
            asList("accountingArticle", "articleMapping", "offerTemplate", "product", "chargeTemplate");

    @Override
    public List<ArticleMappingLine> list(Long offset, Long limit, String sort, String orderBy, String filter) {
        return null;
    }

    @Override
    public Long getCount(String filter) {
        return null;
    }

    @Override
    public Optional<ArticleMappingLine> findById(Long id) {
        return ofNullable(articleMappingLineService.findById(id, fields, true));
    }

    @Override
    public ArticleMappingLine create(ArticleMappingLine articleMappingLine) {
        AccountingArticle accountingArticle = accountingArticleApiService.findById(articleMappingLine.getAccountingArticle().getId());
        if(accountingArticle == null)
            throw new BadRequestException("No accounting article found with id: " + articleMappingLine.getAccountingArticle().getId());
        if(articleMappingLine.getArticleMapping()!=null) {
	        ArticleMapping articleMapping = articleMappingApiService.findById(articleMappingLine.getArticleMapping().getId());
	        if(articleMapping == null)
	            throw new BadRequestException("No article mapping found with id: " + articleMappingLine.getArticleMapping().getId());
	        articleMappingLine.setArticleMapping(articleMapping);
        }

        if(articleMappingLine.getAttributesMapping() != null && !articleMappingLine.getAttributesMapping().isEmpty()){
            List<AttributeMapping> attributesMapping = articleMappingLine.getAttributesMapping()
                    .stream()
                    .map(am -> {
                        Attribute attribute = attributeService.findById(am.getAttribute().getId());
                        if (attribute == null)
                            throw new BadRequestException("No attribute found with Id: " + am.getAttribute().getId());
                        AttributeMapping attributeMapping = new AttributeMapping(attribute, am.getAttributeValue());
                        attributeMapping.setArticleMappingLine(articleMappingLine);
                        return attributeMapping;
                    })
                    .collect(Collectors.toList());
            articleMappingLine.setAttributesMapping(attributesMapping);
        }
        populateArtcileMappingLine(articleMappingLine);
        articleMappingLine.setAccountingArticle(accountingArticle);
        articleMappingLineService.create(articleMappingLine);
        return articleMappingLine;
    }

    @Override
    public Optional<ArticleMappingLine> update(Long id, ArticleMappingLine articleMappingLine) {
        ArticleMappingLine articleMappingLineUpdated = articleMappingLineService.findById(id, true);
        if(articleMappingLineUpdated == null) return Optional.empty();
        if(articleMappingLine.getAccountingArticle().getId() != null) {
	        AccountingArticle accountingArticle = accountingArticleApiService.findById(articleMappingLine.getAccountingArticle().getId());
	        if(accountingArticle == null)
	            throw new BadRequestException("No accounting article found with id: " + articleMappingLine.getAccountingArticle().getId());
	        articleMappingLineUpdated.setAccountingArticle(accountingArticle);
        }
        if(articleMappingLine.getArticleMapping()!=null && articleMappingLine.getArticleMapping().getId() != null) {
	        ArticleMapping articleMapping = articleMappingApiService.findById(articleMappingLine.getArticleMapping().getId());
	        if(articleMapping == null)
	            throw new BadRequestException("No article mapping found with id: " + articleMappingLine.getArticleMapping().getId());
	        articleMappingLineUpdated.setArticleMapping(articleMapping);
        } else {
        	articleMappingLineUpdated.setArticleMapping(null);
        }
        
        if(articleMappingLine.getOfferTemplate() != null){
            OfferTemplate offerTemplate = offerTemplateService.findById(articleMappingLine.getOfferTemplate().getId());
            if(offerTemplate == null)
                throw new BadRequestException("No offer template found with id: " + articleMappingLine.getOfferTemplate().getId());
            articleMappingLineUpdated.setOfferTemplate(offerTemplate);
        }
        if(articleMappingLine.getProduct() != null){
            Product product = productService.findById(articleMappingLine.getProduct().getId());
            if(product == null)
                throw new BadRequestException("No product template found with id: " + articleMappingLine.getProduct().getId());
            articleMappingLineUpdated.setProduct(product);
        }
        if(articleMappingLine.getChargeTemplate() != null){
            ChargeTemplate chargeTemplate = chargeTemplateService.findById(articleMappingLine.getChargeTemplate().getId());
            if(chargeTemplate == null)
                throw new BadRequestException("No charge template found with id: " + articleMappingLine.getChargeTemplate().getId());
            articleMappingLineUpdated.setChargeTemplate(chargeTemplate);
        }
        
        articleMappingLineUpdated.setParameter1(articleMappingLine.getParameter1());
        articleMappingLineUpdated.setParameter2(articleMappingLine.getParameter2());
        articleMappingLineUpdated.setParameter3(articleMappingLine.getParameter3());
        
//        articleMappingLineUpdated.getAttributesMapping().forEach(am -> AttributeMappingService.remove(am));
        articleMappingLineUpdated.getAttributesMapping().clear();
        if(articleMappingLine.getAttributesMapping() != null && !articleMappingLine.getAttributesMapping().isEmpty()){
            List<AttributeMapping> attributesMapping = articleMappingLine.getAttributesMapping()
                    .stream()
                    .map(am -> {
                        Attribute attribute = attributeService.findById(am.getAttribute().getId());
                        if (attribute == null)
                            throw new BadRequestException("No attribute found with Id: " + am.getAttribute().getId());
                        AttributeMapping attributeMapping = new AttributeMapping(attribute, am.getAttributeValue());
                        attributeMapping.setArticleMappingLine(articleMappingLineUpdated);
                        return attributeMapping;
                    })
                    .collect(Collectors.toList());
            articleMappingLineUpdated.getAttributesMapping().addAll(attributesMapping);
        }
        articleMappingLineService.update(articleMappingLineUpdated);
        return Optional.of(articleMappingLineUpdated);
    }
    
    private void populateArtcileMappingLine(ArticleMappingLine articleMappingLine) {
    	 if(articleMappingLine.getOfferTemplate() != null){
             OfferTemplate offerTemplate = offerTemplateService.findById(articleMappingLine.getOfferTemplate().getId());
             if(offerTemplate == null)
                 throw new BadRequestException("No offer template found with id: " + articleMappingLine.getOfferTemplate().getId());
             articleMappingLine.setOfferTemplate(offerTemplate);
         }
         if(articleMappingLine.getProduct() != null){
             Product product = productService.findById(articleMappingLine.getProduct().getId());
             if(product == null)
                 throw new BadRequestException("No product template found with id: " + articleMappingLine.getProduct().getId());
             articleMappingLine.setProduct(product);
         }
         if(articleMappingLine.getChargeTemplate() != null){
             ChargeTemplate chargeTemplate = chargeTemplateService.findById(articleMappingLine.getChargeTemplate().getId());
             if(chargeTemplate == null)
                 throw new BadRequestException("No charge template found with id: " + articleMappingLine.getChargeTemplate().getId());
             articleMappingLine.setChargeTemplate(chargeTemplate);
         }
    }
    
   
    

    @Override
    public Optional<ArticleMappingLine> patch(Long id, ArticleMappingLine baseEntity) {
        return Optional.empty();
    }

    @Override
    public Optional<ArticleMappingLine> delete(Long id) {
    	Optional<ArticleMappingLine> articleMappingLine = findById(id);
    	if(articleMappingLine.isPresent()) {
    		ArticleMappingLine current = articleMappingLine.get();
    		articleMappingLineService.remove(current);
    		return Optional.of(current);
    	}
         return Optional.empty();
    }

    @Override
    public Optional<ArticleMappingLine> findByCode(String code) {
        return ofNullable(articleMappingLineService.findByCode(code, fields));
    }
}
