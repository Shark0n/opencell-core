package org.meveo.api.rest.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.RoleApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.RoleDto;
import org.meveo.api.dto.response.GetRoleResponse;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.RoleRs;

@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class RoleRsImpl extends BaseRs implements RoleRs {

    @Inject
    private RoleApi roleApi;

    @Override
    public ActionStatus create(RoleDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            roleApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus update(RoleDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            roleApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus remove(String roleName, String provider) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            roleApi.remove(roleName, provider);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetRoleResponse find(String roleName, String provider) {
        GetRoleResponse result = new GetRoleResponse();
        try {
            result.setRoleDto(roleApi.find(roleName, provider));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdate(RoleDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            roleApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

}
