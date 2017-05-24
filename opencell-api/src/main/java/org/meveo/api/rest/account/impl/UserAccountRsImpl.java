package org.meveo.api.rest.account.impl;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.account.UserAccountApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.account.ApplyProductRequestDto;
import org.meveo.api.dto.account.UserAccountDto;
import org.meveo.api.dto.billing.CounterInstanceDto;
import org.meveo.api.dto.response.account.GetUserAccountResponseDto;
import org.meveo.api.dto.response.account.UserAccountsResponseDto;
import org.meveo.api.dto.response.billing.GetCountersInstancesResponseDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.account.UserAccountRs;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.model.billing.CounterInstance;
import org.meveo.model.shared.DateUtils;

@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class UserAccountRsImpl extends BaseRs implements UserAccountRs {

    @Inject
    private UserAccountApi userAccountApi;

    @Override
    public ActionStatus create(UserAccountDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userAccountApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus update(UserAccountDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userAccountApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetUserAccountResponseDto find(String userAccountCode) {
        GetUserAccountResponseDto result = new GetUserAccountResponseDto();

        try {
            result.setUserAccount(userAccountApi.find(userAccountCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus remove(String userAccountCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userAccountApi.remove(userAccountCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public UserAccountsResponseDto listByBillingAccount(String billingAccountCode) {
        UserAccountsResponseDto result = new UserAccountsResponseDto();

        try {
            result.setUserAccounts(userAccountApi.listByBillingAccount(billingAccountCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdate(UserAccountDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userAccountApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetCountersInstancesResponseDto filterUserAccountCountersByPeriod(String userAccountCode, String date) {
        GetCountersInstancesResponseDto result = new GetCountersInstancesResponseDto();

        try {
            List<CounterInstance> counters = userAccountApi.filterCountersByPeriod(userAccountCode, DateUtils.parseDateWithPattern(date, "yyyy-MM-dd"));
            for (CounterInstance ci : counters) {
                result.getCountersInstances().getCounterInstance().add(new CounterInstanceDto(ci));
            }
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus applyProduct(ApplyProductRequestDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userAccountApi.applyProduct(postData);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }
}
