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

package org.meveo.api.security.Interceptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.AccessDeniedException;
import org.meveo.api.security.filter.SecureMethodResultFilter;
import org.meveo.api.security.filter.SecureMethodResultFilterFactory;
import org.meveo.api.security.parameter.SecureMethodParameter;
import org.meveo.api.security.parameter.SecureMethodParameterHandler;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.admin.SecuredEntity;
import org.meveo.model.admin.User;
import org.meveo.model.security.Role;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.admin.impl.RoleService;
import org.meveo.service.admin.impl.UserService;
import org.meveo.service.security.SecuredBusinessEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will handle the processing of {@link SecuredBusinessEntityMethod} annotated methods.
 * 
 * @author Tony Alejandro
 * @author Wassim Drira
 * @author mohamed stitane
 * @lastModifiedVersion 5.0
 */
public class SecuredBusinessEntityMethodInterceptor implements Serializable {

    private static final long serialVersionUID = 4656634337151866255L;

    private static final Logger log = LoggerFactory.getLogger(SecuredBusinessEntityMethodInterceptor.class);

    @Inject
    private SecuredBusinessEntityService securedBusinessEntityService;

    @Inject
    private SecureMethodResultFilterFactory filterFactory;

    @Inject
    private SecureMethodParameterHandler parameterHandler;

    @Inject
    @CurrentUser
    private MeveoUser currentUser;

    @Inject
    private UserService userService;

    @Inject
    private RoleService roleService;

    /** paramBean Factory allows to get application scope paramBean or provider specific paramBean */
    @Inject
    private ParamBeanFactory paramBeanFactory;

    /**
     * This is called before a method that makes use of the {@link SecuredBusinessEntityMethodInterceptor} is called. It contains logic on retrieving the attributes of the
     * {@link SecuredBusinessEntityMethod} annotation placed on the method and then validate the parameters described in the {@link SecureMethodParameter} validation attributes and
     * then filters the result using the {@link SecureMethodResultFilter} filter attribute.
     * 
     * @param context  The invocation context
     * @return  The filtered result object
     * @throws Exception exception
     */
    @AroundInvoke
    public Object checkForSecuredEntities(InvocationContext context) throws Exception {

        SecuredBusinessEntityMethod annotation = context.getMethod().getAnnotation(SecuredBusinessEntityMethod.class);
        if (annotation == null) {
            return context.proceed();
        }

        // log.error("AKK checking secured entities currentUser is {}", currentUser);

        // check if secured entities should be checked.
        String secureSetting = paramBeanFactory.getInstance().getProperty("secured.entities.enabled", "false");
        boolean secureEntitesEnabled = Boolean.parseBoolean(secureSetting);

        // if not, immediately return.
        if (!secureEntitesEnabled) {
            return context.proceed();
        }

        Map<Class<?>, Set<SecuredEntity>> allSecuredEntitiesMap = getAllSecuredEntities(currentUser);
        boolean hasRestrictions = !allSecuredEntitiesMap.isEmpty();

		if (!hasRestrictions) {
			return context.proceed();
		}

        Class<?> objectClass = context.getMethod().getDeclaringClass();
        String objectName = objectClass.getSimpleName();
        String methodName = context.getMethod().getName();

        log.debug("Checking method {}.{} for secured BusinessEntities", objectName, methodName);

        Object[] values = context.getParameters();

        List<SecuredEntity> securedEntities = allSecuredEntitiesMap.values().stream().flatMap(Set::stream).collect(Collectors.toList());
        addSecuredEntitiesToFilters(securedEntities, values);

        SecureMethodParameter[] parametersForValidation = annotation.validate();
        for (SecureMethodParameter parameter : parametersForValidation) {
            BusinessEntity entity = parameterHandler.getParameterValue(parameter, values, BusinessEntity.class);
            if (entity == null) {
                // TODO what to do if entity was not resolved because parameter value was null e.g. doing a search by a restricted field and dont provide any field value - that
                // means that instead of filtering search criteria, results should be filtered instead

            } else {
                if (!securedBusinessEntityService.isEntityAllowed(entity, allSecuredEntitiesMap, false)) {
                    throw new AccessDeniedException("Access to entity details is not allowed.");
                }
            }
        }

        log.debug("Allowing method {}.{} to be invoked.", objectName, methodName);
        Object result = context.proceed();

        SecureMethodResultFilter filter = filterFactory.getFilter(annotation.resultFilter());
        log.debug("Method {}.{} results will be filtered using {} filter.", objectName, methodName, filter);
        result = filter.filterResult(context.getMethod(), result, currentUser, allSecuredEntitiesMap);
        return result;

    }

    /**
     * Get all accessible entities for the current user, both associated directly to the user
     * or to its associated roles.
     * Those accessible entities are then grouped by types into Map
     *
     * @param currentUser MeveoUser current user
     * @return current user's accessible entities
     */
    private Map<Class<?>, Set<SecuredEntity>> getAllSecuredEntities(MeveoUser currentUser) {
        List<SecuredEntity> allSecuredEntities = new ArrayList<>();
        User user = userService.findByUsername(currentUser.getUserName());
        allSecuredEntities.addAll(user.getSecuredEntities());

        List<Role> rolesWithSecuredEntities = roleService.getEntityManager().createNamedQuery("Role.getRolesWithSecuredEntities", Role.class)
        		.setParameter("currentUserRoles", currentUser.getRoles())
        		.getResultList();
        allSecuredEntities.addAll(rolesWithSecuredEntities.stream().map(Role::getSecuredEntities).flatMap(List::stream).collect(Collectors.toList()));


        // group secured entites by types into Map
        Map<Class<?>, Set<SecuredEntity>> securedEntitiesMap = new HashMap<>();
        Set<SecuredEntity> securedEntitySet = null;
        try {
            for (SecuredEntity securedEntity : allSecuredEntities) {
                Class<?> securedBusinessEntityClass = Class.forName(securedEntity.getEntityClass());
                if (securedEntitiesMap.get(securedBusinessEntityClass) == null) {
                    securedEntitySet = new HashSet<>();
                    securedEntitiesMap.put(securedBusinessEntityClass, securedEntitySet);
                }
                securedEntitiesMap.get(securedBusinessEntityClass).add(securedEntity);
            }
        } catch (ClassNotFoundException e) {
            // do nothing
        }
        return securedEntitiesMap;
    }
    /**
     * Adding a secured entities code to the filters for paging
     *
     * @param securedEntities all secured entities
     * @param values          the context parameter
     */
    private void addSecuredEntitiesToFilters(List<SecuredEntity> securedEntities, Object[] values) {
        log.debug("Adding a secured entities code to the filters for paging");
        for (Object obj : values) {
            if (obj instanceof PagingAndFiltering) {
                PagingAndFiltering pagingAndFiltering = (PagingAndFiltering) obj;
                updateFilters(securedEntities, pagingAndFiltering);
                break;
            }
        }
    }

    /**
     * Adding a secured entities code to the filters
     *
     * @param securedEntities    a secured entities
     * @param pagingAndFiltering a paging and filtering object
     */
    private void updateFilters(List<SecuredEntity> securedEntities, PagingAndFiltering pagingAndFiltering) {
        if (isNotNull(pagingAndFiltering)) {
            Map<String, Object> filters = Optional.ofNullable(pagingAndFiltering.getFilters()).orElse(new HashMap<>());
            for (SecuredEntity securedEntity : securedEntities) {
                final String entityClass = securedEntity.getEntityClass();
                //extract the field name from entity class, I supposed that the field name is the same as the Class name.
                final String fieldName = entityClass.substring(entityClass.lastIndexOf('.') + 1).toLowerCase();
                log.debug("Code = {} for entity = {}", securedEntity.getCode(), fieldName);
                final String keyInList = "inList " + fieldName + ".code";
                if (filters.containsKey(fieldName)) {
                    final Object initialValue = filters.get(fieldName);
                    filters.put(keyInList, StringUtils.concat(initialValue, ",", securedEntity.getCode()));
                    filters.remove(fieldName);
                } else if (filters.containsKey(keyInList)) {
                    final Object initialList = filters.get(keyInList);
                    filters.replace(keyInList, StringUtils.concat(initialList, ",", securedEntity.getCode()));
                } else {
                    filters.put(fieldName, securedEntity.getCode());
                }
            }
            pagingAndFiltering.setFilters(filters);
        }
    }

    /**
     * check if the object is null
     *
     * @param pagingAndFiltering a paging and filtering object
     * @return true or false
     */
    private boolean isNotNull(PagingAndFiltering pagingAndFiltering) {
        return pagingAndFiltering != null && pagingAndFiltering.getLimit() != null && pagingAndFiltering.getOffset() != null;
    }
}
