package org.meveo.api.ws.impl;

import java.util.List;

import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.jws.WebService;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.finance.RevenueRecognitionRuleDto;
import org.meveo.api.dto.response.payment.RevenueRecognitionRuleDtoResponse;
import org.meveo.api.dto.response.payment.RevenueRecognitionRuleDtosResponse;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.payment.RevenueRecognitionRuleApi;
import org.meveo.api.ws.FinanceWs;

@WebService(serviceName = "FinanceWs", endpointInterface = "org.meveo.api.ws.FinanceWs")
@Interceptors({ WsRestApiInterceptor.class })
public class FinanceWSImpl extends BaseWs implements FinanceWs {

    @Inject
    RevenueRecognitionRuleApi rrrApi;

    @Override
    public ActionStatus createRevenueRecognitionRule(RevenueRecognitionRuleDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            rrrApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus updateRevenueRecognitionRule(RevenueRecognitionRuleDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            rrrApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus deleteRevenueRecognitionRule(String code) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            rrrApi.remove(code);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public RevenueRecognitionRuleDtosResponse listRevenueRecognitionRules() {
        RevenueRecognitionRuleDtosResponse result = new RevenueRecognitionRuleDtosResponse();
        result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);
        result.getActionStatus().setMessage("");
        try {
            List<RevenueRecognitionRuleDto> dtos = rrrApi.list();
            result.setRevenueRecognitionRules(dtos);
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public RevenueRecognitionRuleDtoResponse getRevenueRecognitionRule(String code) {
        RevenueRecognitionRuleDtoResponse result = new RevenueRecognitionRuleDtoResponse();
        result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);

        try {
            result.setRevenueRecognitionRuleDto(rrrApi.find(code));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdateRevenueRecognitionRule(RevenueRecognitionRuleDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            rrrApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }
}