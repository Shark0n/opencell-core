package org.meveo.admin.action.payments;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.action.BaseBean;
import org.meveo.model.payments.CreditCategory;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.payments.impl.CreditCategoryService;

/**
 * @author Edward P. Legaspi
 **/
@Named
@ViewScoped
public class CreditCategoryBean extends BaseBean<CreditCategory> {

	private static final long serialVersionUID = 2958011009867008164L;

	@Inject
	private CreditCategoryService creditCategoryService;

	public CreditCategoryBean() {
		super(CreditCategory.class);
	}
	
	@Override
	protected IPersistenceService<CreditCategory> getPersistenceService() {
		return creditCategoryService;
	}

}
