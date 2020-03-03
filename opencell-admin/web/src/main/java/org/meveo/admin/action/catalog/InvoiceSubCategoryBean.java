/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.admin.action.catalog;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.action.BaseBean;
import org.meveo.admin.action.CustomFieldBean;
import org.meveo.model.billing.InvoiceSubCategory;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.catalog.impl.InvoiceCategoryService;
import org.meveo.service.catalog.impl.InvoiceSubCategoryService;
import org.omnifaces.cdi.Param;

/**
 * Standard backing bean for {@link InvoiceSubCategory} (extends {@link BaseBean} that provides almost all common methods to handle entities filtering/sorting in datatable, their
 * create, edit, view, delete operations). It works with Manaty custom JSF components.
 */
@Named
@ViewScoped
public class InvoiceSubCategoryBean extends CustomFieldBean<InvoiceSubCategory> {
    private static final long serialVersionUID = 1L;

    /**
     * Injected @{link InvoiceSubCategory} service. Extends {@link PersistenceService}.
     */
    @Inject
    private InvoiceSubCategoryService invoiceSubCategoryService;

    /**
     * Inject InvoiceCategory service, that is used to load default category if its id was passed in parameters.
     */
    @Inject
    private InvoiceCategoryService invoiceCategoryService;

    /**
     * InvoiceCategory Id passed as a parameter. Used when creating new InvoiceSubCategory from InvoiceCategory window, so default InvoiceCategory will be set on newly created
     * InvoiceSubCategory.
     */
    @Inject
    @Param
    private Long invoiceCategoryId;

    /**
     * Constructor. Invokes super constructor and provides class type of this bean for {@link BaseBean}.
     */
    public InvoiceSubCategoryBean() {
        super(InvoiceSubCategory.class);
    }

    /**
     * Factory method for entity to edit. If objectId param set load that entity from database, otherwise create new.
     * 
     * @return invoice sub category
     */
    @Override
    public InvoiceSubCategory initEntity() {
        InvoiceSubCategory invoiceCatSub = super.initEntity();

        if (invoiceCategoryId != null) {
            entity.setInvoiceCategory(invoiceCategoryService.findById(invoiceCategoryId));
        }

        return invoiceCatSub;
    }

    public List<InvoiceSubCategory> listAll() {
        getFilters();
        if (filters.containsKey("languageCode")) {
            filters.put("language.languageCode", filters.get("languageCode"));
            filters.remove("languageCode");
        } else if (filters.containsKey("language.languageCode")) {
            filters.remove("language.languageCode");
        }
        return super.listAll();
    }

    /**
     * @see org.meveo.admin.action.BaseBean#getPersistenceService()
     */
    @Override
    protected IPersistenceService<InvoiceSubCategory> getPersistenceService() {
        return invoiceSubCategoryService;
    }

    @Override
    protected String getDefaultSort() {
        return "code";
    }
}