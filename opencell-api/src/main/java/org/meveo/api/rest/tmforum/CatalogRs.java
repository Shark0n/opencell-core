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

package org.meveo.api.rest.tmforum;

import org.meveo.api.dto.catalog.*;
import org.meveo.api.dto.response.ProductChargeTemplatesResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.serialize.RestDateParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Date;

/**
 * TMForum Product catalog API specification implementation. Note: only READ type methods are implemented.
 * 
 * @author Andrius Karpavicius
 */
@Path("/catalogManagement")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface CatalogRs extends IBaseRs {

    /**
     * Get a list of categories
     * 
     * @param info Http request context
     * @return A list of categories
     */
    @GET
    @Path("/category")
    public Response findCategories(@Context UriInfo info);

    /**
     * Get a single category by its code
     * 
     * @param code Category code
     * @param info Http request context
     * @return Single category information
     */
    @GET
    @Path("/category/{code}")
    public Response getCategory(@PathParam("code") String code, @Context UriInfo info);

    /**
     * Get a list of product offerings optionally filtering by some criteria
     * 
     * @param validFrom valid From option criteria date
     * @param validTo valid To option criteria date
     * @param info Http request context
     * @return A list of product offerings matching search criteria
     */
    @GET
    @Path("/productOffering")
    public Response findProductOfferings(@QueryParam("validFrom") @RestDateParam Date validFrom, @QueryParam("validTo") @RestDateParam Date validTo, @Context UriInfo info);

    /**
     * Get details of a single Product template and validity dates. If no validity dates are provided, an Product template valid on a current date will be returned.
     * 
     * @param id Product offering code
     * @param validFrom Product template validity range - from date
     * @param validTo Product template validity range - to date
     * @param info Http request context
     * @return Single product offering
     */
    @GET
    @Path("/productOffering/{id}")
    public Response getProductOffering(@PathParam("id") String id, @QueryParam("validFrom") @RestDateParam Date validFrom, @QueryParam("validTo") @RestDateParam Date validTo,
            @Context UriInfo info);

    /**
     * Get a list of product specifications optionally filtering by some criteria
     * 
     * @param info Http request context
     * @return A list of product specifications matching search criteria
     */
    @GET
    @Path("/productSpecification")
    public Response findProductSpecifications(@Context UriInfo info);

    /**
     * Get details of a single product
     * 
     * @param id Product code
     * @param validFrom Product template validity range - from date
     * @param validTo Product template validity range - to date
     * @param info Http request context
     * @return A single product specification
     */
    @GET
    @Path("/productSpecification/{id}")
    public Response getProductSpecification(@PathParam("id") String id, @QueryParam("validFrom") @RestDateParam Date validFrom, @QueryParam("validTo") @RestDateParam Date validTo,
            @Context UriInfo info);

    /**
     * Create offer from BOM definition
     * 
     * @param postData BOM offer information
     * @return Response of the create offer BOM
     */
    @POST
    @Path("/createOfferFromBOM")
    public Response createOfferFromBOM(BomOfferDto postData);

    /**
     * Create service from BSM definition
     * 
     * @param postData BSM service information
     * @return Response of the create Service BSM
     */
    @POST
    @Path("/createServiceFromBSM")
    public Response createServiceFromBSM(BsmServiceDto postData);
  
    /**
     * Create product from BPM definition
     * 
     * @param postData BPM service information
     * @return Response of the create Service BPM
     */
    @POST
    @Path("/createProductFromBPM")
    public Response createProductFromBPM(BpmProductDto postData);

    /**
     * Get a single productTemplate by its code and validity dates. If no validity dates are provided, a product template valid on a current date will be deleted.
     * 
     * @param code productTemplate code
     * @param validFrom Product template validity range - from date
     * @param validTo Procuct template validity range - to date
     * @return Single productTemplate information
     */
    @GET
    @Path("/productTemplate/{code}")
    public Response getProductTemplate(@PathParam("code") String code, @QueryParam("validFrom") @RestDateParam Date validFrom, @QueryParam("validTo") @RestDateParam Date validTo);

    /**
     * Create product template
     * 
     * @param postData product template information
     * @return Response of the create Product Template
     */
    @POST
    @Path("/productTemplate")
    public Response createProductTemplate(ProductTemplateDto postData);

    /**
     * Create or update product template
     * 
     * @param postData product template information
     * @return Response of the create Product Template
     */
    @POST
    @Path("/productTemplate/createOrUpdate")
    public Response createOrUpdateProductTemplate(ProductTemplateDto postData);

    /**
     * Update product template
     * 
     * @param postData product template information
     * @return Response of the update Product Template
     */
    @PUT
    @Path("/productTemplate")
    public Response updateProductTemplate(ProductTemplateDto postData);

    /**
     * Delete a single productTemplate by its code and validity dates. If no validity dates are provided, a product template valid on a current date will be deleted.
     * 
     * @param code productTemplate code
     * @param validFrom Product template validity range - from date
     * @param validTo Procuct template validity range - to date
     * @return Response of the remove action
     */
    @DELETE
    @Path("/productTemplate/{code}")
    public Response removeProductTemplate(@PathParam("code") String code, @QueryParam("validFrom") @RestDateParam Date validFrom,
            @QueryParam("validTo") @RestDateParam Date validTo);

    /**
     * List all product templates optionally filtering by code and validity dates. If neither date is provided, validity dates will not be considered. If only validFrom is
     * provided, a search will return products valid on a given date. If only validTo date is provided, a search will return products valid from today to a given date.
     * 
     * @param code Product template code for optional filtering
     * @param validFrom Validity range from date.
     * @param validTo Validity range to date.
     * @return A list of product templates
     */
    @GET
    @Path("/productTemplate/list")
    public Response listProductTemplate(@QueryParam("code") String code, @QueryParam("validFrom") @RestDateParam Date validFrom,
            @QueryParam("validTo") @RestDateParam Date validTo);

    /**
     * Enable a Product template with a given code
     * 
     * @param code Product template code
     * @param validFrom Product template validity range - from date
     * @param validTo Product template validity range - to date
     * @return Request processing status
     */
    @POST
    @Path("/productTemplate/{code}/enable")
    Response enableProductTemplate(@PathParam("code") String code, @QueryParam("validFrom") @RestDateParam Date validFrom, @QueryParam("validTo") @RestDateParam Date validTo);

    /**
     * Disable a Product template with a given code
     * 
     * @param code Product template code
     * @param validFrom Product template validity range - from date
     * @param validTo Product template validity range - to date
     * @return Request processing status
     */
    @POST
    @Path("/productTemplate/{code}/disable")
    Response disableProductTemplate(@PathParam("code") String code, @QueryParam("validFrom") @RestDateParam Date validFrom, @QueryParam("validTo") @RestDateParam Date validTo);

    /**
     * Get a single productChargeTemplate by its code
     * 
     * @param code productChargeTemplate code
     * @return Single productChargeTemplate information
     */
    @GET
    @Path("/productChargeTemplate/{code}")
    public Response getProductChargeTemplate(@PathParam("code") String code);

    /**
     * Create product charge template
     * 
     * @param postData product charge template information
     * @return Response of the create Product Charge Template
     */
    @POST
    @Path("/productChargeTemplate")
    public Response createProductChargeTemplate(ProductChargeTemplateDto postData);

    /**
     * Create or update product charge template
     * 
     * @param postData product charge template information
     * @return Response of the create or update Product Charge Template
     */
    @POST
    @Path("/productChargeTemplate/createOrUpdate")
    public Response createOrUpdateProductChargeTemplate(ProductChargeTemplateDto postData);

    /**
     * Update product charge template
     * 
     * @param postData product charge template information
     * @return Response of the update Product Charge Template
     */
    @PUT
    @Path("/productChargeTemplate")
    public Response updateProductChargeTemplate(ProductChargeTemplateDto postData);

    /**
     * Delete a single productChargeTemplate by its code
     * 
     * @param code productChargeTemplate code
     * @return Response of the delete action
     */
    @DELETE
    @Path("/productChargeTemplate/{code}")
    public Response removeProductChargeTemplate(@PathParam("code") String code);

    /**
     * List all productChargeTemplates
     * 
     * @return List of charge template
     */
    @GET
    @Path("/productChargeTemplate/list")
    public Response listProductChargeTemplate();

    /**
     * Gets a productChargeTemplate list.
     *
     * @return Return productChargeTemplate list
     */
    @GET
    @Path("/productChargeTemplate/listGetAll")
    ProductChargeTemplatesResponseDto listGetAllPCTemplates();

    /**
     * Enable a Product charge template with a given code
     * 
     * @param code Product charge template code
     * @return Request processing status
     */
    @POST
    @Path("/productChargeTemplate/{code}/enable")
    Response enableProductChargeTemplate(@PathParam("code") String code);

    /**
     * Disable a Product charge template with a given code
     * 
     * @param code Product charge template code
     * @return Request processing status
     */
    @POST
    @Path("/productChargeTemplate/{code}/disable")
    Response disableProductChargeTemplate(@PathParam("code") String code);
}