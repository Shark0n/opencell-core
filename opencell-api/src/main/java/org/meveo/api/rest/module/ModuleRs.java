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

package org.meveo.api.rest.module;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.response.module.MeveoModuleDtoResponse;
import org.meveo.api.dto.response.module.MeveoModuleDtosResponse;
import org.meveo.api.rest.IBaseRs;

@Path("/module")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface ModuleRs extends IBaseRs {

    /**
     * Create a new meveo module
     * 
     * @param moduleDto The meveo module's data
     * @return Request processing status
     */
    @POST
    @Path("/")
    public ActionStatus create(MeveoModuleDto moduleDto);

    /**
     * Update an existing Meveo module
     * 
     * @param moduleDto The Meveo module's data
     * @return Request processing status
     */
    @PUT
    @Path("/")
    public ActionStatus update(MeveoModuleDto moduleDto);

    /**
     * Create new or update an existing Meveo module
     * 
     * @param moduleDto The Meveo module's data
     * @return Request processing status
     */
    @POST
    @Path("/createOrUpdate")
    public ActionStatus createOrUpdate(MeveoModuleDto moduleDto);

    /**
     * Remove an existing module with a given code 
     * 
     * @param code The module's code
     * @return Request processing status
     */
    @DELETE
    @Path("/{code}")
    public ActionStatus delete(@PathParam("code") String code);

    /**
     * List all Meveo's modules
     * 
     * @return A list of Meveo's modules
     */
    @GET
    @Path("/list")
    public MeveoModuleDtosResponse list();

    /**
     * Install Meveo module
     * 
     * @param moduleDto the Meveo's module
     * @return Request processing status
     */
    @PUT
    @Path("/install")
    public ActionStatus install(MeveoModuleDto moduleDto);

    /**
     * Find a Meveo's module with a given code 
     * 
     * @param code The Meveo module's code
     * @return Meveo module DTO Response.
     */
    @GET
    @Path("/")
    public MeveoModuleDtoResponse get(@QueryParam("code") String code);

    /**
     * uninstall a Meveo's module with a given code
     * 
     * @param code The Meveo module's code
     * @return Request processing status
     */
    @GET
    @Path("/uninstall")
    public ActionStatus uninstall(@QueryParam("code") String code);

    /**
     * Enable a Meveo's module with a given code
     * 
     * @param code The Meveo module's code
     * @return Request processing status
     */
    @GET
    @Path("/enable")
    public ActionStatus enableGet(@QueryParam("code") String code);

    /**
     * Disable a Meveo's module with a given code
     * 
     * @param code The Meveo module's code
     * @return Request processing status
     */
    @GET
    @Path("/disable")
    public ActionStatus disableGet(@QueryParam("code") String code);

    /**
     * Enable a Opencell module with a given code
     * 
     * @param code Opencell module code
     * @return Request processing status
     */
    @POST
    @Path("/{code}/enable")
    ActionStatus enable(@PathParam("code") String code);

    /**
     * Disable a Opencell module with a given code
     * 
     * @param code Opencell module code
     * @return Request processing status
     */
    @POST
    @Path("/{code}/disable")
    ActionStatus disable(@PathParam("code") String code);

}