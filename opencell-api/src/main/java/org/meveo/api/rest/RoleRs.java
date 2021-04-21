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

package org.meveo.api.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.RoleDto;
import org.meveo.api.dto.RolesDto;
import org.meveo.api.dto.response.GetRoleResponse;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.PagingAndFiltering.SortOrder;

/**
 * REST API for managing {@link Role}.
 * @author Edward P. Legaspi
 * @lastModifiedVersion 6.0
 */
@Path("/role")
@Tag(name = "Role", description = "@%Role")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface RoleRs extends IBaseRs {

    /**
     * Create role.
     * 
     * @param postData posted data containing role dto.
     * @return action status
     */
    @POST
    @Path("/")
	@Operation(
			summary=" Create role.  ",
			description=" Create role.  ",
			operationId="    POST_Role_ ",
			responses= {
				@ApiResponse(description=" action status ",
						content=@Content(
									schema=@Schema(
											implementation= ActionStatus.class
											)
								)
				)}
	)
    ActionStatus create(RoleDto postData);

    /**
     * Update role.
     * 
     * @param postData posted data
     * @return action status.
     */
    @PUT
    @Path("/")
	@Operation(
			summary=" Update role.  ",
			description=" Update role.  ",
			operationId="    PUT_Role_ ",
			responses= {
				@ApiResponse(description=" action status. ",
						content=@Content(
									schema=@Schema(
											implementation= ActionStatus.class
											)
								)
				)}
	)
    ActionStatus update(RoleDto postData);

    /**
     * Remove role.
     *
     * @param roleName Role name
     * @return action status.
     */
    @DELETE
    @Path("/{roleName}")
	@Operation(
			summary=" Remove role. ",
			description=" Remove role. ",
			operationId="    DELETE_Role_{roleName}",
			responses= {
				@ApiResponse(description=" action status. ",
						content=@Content(
									schema=@Schema(
											implementation= ActionStatus.class
											)
								)
				)}
	)
    ActionStatus remove(@PathParam("roleName") String roleName);

    /**
     * Search role.
     * 
     * @param roleName Role name
     * @param includeSecuredEntities if true returns the secured entities
     * @return found role
     */
    @GET
    @Path("/")
	@Operation(
			summary=" Search role.  ",
			description=" Search role.  ",
			operationId="    GET_Role_ ",
			responses= {
				@ApiResponse(description=" found role ",
						content=@Content(
									schema=@Schema(
											implementation= GetRoleResponse.class
											)
								)
				)}
	)
    GetRoleResponse find(@QueryParam("roleName") String roleName, @QueryParam("includeSecuredEntities") boolean includeSecuredEntities);

    /**
     * Create or update role.
     * 
     * @param postData posted data
     * @return action status
     */
    @POST
    @Path("/createOrUpdate")
	@Operation(
			summary=" Create or update role.  ",
			description=" Create or update role.  ",
			operationId="    POST_Role_createOrUpdate ",
			responses= {
				@ApiResponse(description=" action status ",
						content=@Content(
									schema=@Schema(
											implementation= ActionStatus.class
											)
								)
				)}
	)
    ActionStatus createOrUpdate(RoleDto postData);

    /**
     * List roles matching a given criteria.
     * 
     * @param query Search criteria. Query is composed of the following: filterKey1:filterValue1|filterKey2:filterValue2
     * @param fields Data retrieval options/fieldnames separated by a comma. Specify "permissions" in fields to include the permissions. Specify "roles" to include child roles.
     * @param offset Pagination - from record number
     * @param limit Pagination - number of records to retrieve
     * @param sortBy Sorting - field to sort by - a field from a main entity being searched. See Data model for a list of fields.
     * @param sortOrder Sorting - sort order.
     * @return A list of roles
     */
    @GET
    @Path("/list")
	@Operation(
			summary=" List roles matching a given criteria.  ",
			description=" List roles matching a given criteria.  ",
			operationId="    GET_Role_list",
			responses= {
				@ApiResponse(description=" A list of roles ",
						content=@Content(
									schema=@Schema(
											implementation= RolesDto.class
											)
								)
				)}
	)
    RolesDto listGet(@QueryParam("query") String query, @QueryParam("fields") String fields, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit,
            @DefaultValue("name") @QueryParam("sortBy") String sortBy, @DefaultValue("ASCENDING") @QueryParam("sortOrder") SortOrder sortOrder);

    /**
     * List roles matching a given criteria.
     * 
     * @param pagingAndFiltering Pagination and filtering criteria. Specify "permissions" in fields to include the permissions. Specify "roles" to include child roles.
     * @return A list of roles
     */
    @POST
    @Path("/list")
	@Operation(
			summary=" List roles matching a given criteria.  ",
			description=" List roles matching a given criteria.  ",
			operationId="    POST_Role_list ",
			responses= {
				@ApiResponse(description=" A list of roles ",
						content=@Content(
									schema=@Schema(
											implementation= RolesDto.class
											)
								)
				)}
	)
    RolesDto listPost(PagingAndFiltering pagingAndFiltering);
    
    /**
     * List external roles.
     * @return list of external roles
     */
    @GET
    @Path("/external")
	@Operation(
			summary=" List external roles. ",
			description=" List external roles. ",
			operationId="    GET_Role_external ",
			responses= {
				@ApiResponse(description=" list of external roles ",
						content=@Content(
									schema=@Schema(
											implementation= RolesDto.class
											)
								)
				)}
	)
    RolesDto listExternalRoles();

}
