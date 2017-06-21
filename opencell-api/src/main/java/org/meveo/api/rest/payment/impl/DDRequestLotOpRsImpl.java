package org.meveo.api.rest.payment.impl;

import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.payment.DDRequestLotOpDto;
import org.meveo.api.dto.response.payment.DDRequestLotOpsResponseDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.payment.DDRequestLotOpApi;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.payment.DDRequestLotOpRs;
import org.meveo.model.payments.DDRequestOpStatusEnum;

@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class DDRequestLotOpRsImpl extends BaseRs implements DDRequestLotOpRs {

    @Inject
    private DDRequestLotOpApi ddrequestLotOpApi;

	@Override
	public ActionStatus create(DDRequestLotOpDto dto) {
      ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

      try {
          ddrequestLotOpApi.create(dto);
      } catch (MeveoApiException e) {
          result.setStatus(ActionStatusEnum.FAIL);
          result.setMessage(e.getMessage());
          log.error("error occurred while creating account operation ", e);
      } catch (Exception e) {
          result.setStatus(ActionStatusEnum.FAIL);
          result.setMessage(e.getMessage());
          log.error("error generated while creating account operation ", e);
      }

      return result;	}

	@Override
	public DDRequestLotOpsResponseDto list(Date fromDueDate, Date toDueDate, DDRequestOpStatusEnum status) {
      DDRequestLotOpsResponseDto result = new DDRequestLotOpsResponseDto();

      try {
          result.setDdrequestLotOps(ddrequestLotOpApi.listDDRequestLotOps(fromDueDate, toDueDate, status));
//      } catch (MeveoApiException e) {
//          result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
//          result.getActionStatus().setMessage(e.getMessage());
//          log.error("error occurred while getting list account operation ", e);
      } catch (Exception e) {
          result.getActionStatus().setStatus(ActionStatusEnum.FAIL);
          result.getActionStatus().setMessage(e.getMessage());
          log.error("error generated while getting list account operation ", e);
      }

      return result;
  }
}
