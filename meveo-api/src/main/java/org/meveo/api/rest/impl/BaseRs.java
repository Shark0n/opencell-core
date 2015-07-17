package org.meveo.api.rest.impl;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.rest.security.RSUser;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.admin.User;
import org.meveo.util.MeveoParamBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Edward P. Legaspi
 **/
public abstract class BaseRs implements IBaseRs {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	@MeveoParamBean
	protected ParamBean paramBean;

	@Inject
	@RSUser
	private User currentUser;

	@Context
	protected HttpServletRequest httpServletRequest;

	// one way to get HttpServletResponse
	@Context
	protected HttpServletResponse httpServletResponse;

	protected final String RESPONSE_DELIMITER = " - ";

	@GET
	@Path("/version")
	public ActionStatus index() {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "MEVEO API Rest Web Service V1.0");

		return result;
	}

	@GET
	@Path("/user")
	public ActionStatus user() {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "WS User is=" + getCurrentUser().toString());

		return result;
	}

	public User getCurrentUser() {
		log.debug("Injected REST user is={}", currentUser);
		return currentUser;
	}

}
