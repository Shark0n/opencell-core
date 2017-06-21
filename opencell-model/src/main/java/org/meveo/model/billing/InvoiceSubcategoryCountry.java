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
package org.meveo.model.billing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.EnableEntity;
import org.meveo.model.ExportIdentifier;

/**
 * InvoiceSubcategoryCountry entity.
 */
@Entity
@ExportIdentifier({ "invoiceSubCategory.code", "tradingCountry.country.countryCode", "tax.code"})
@Table(name = "billing_inv_sub_cat_country")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {@Parameter(name = "sequence_name", value = "billing_inv_sub_cat_country_seq"), })
public class InvoiceSubcategoryCountry extends EnableEntity {
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invoice_sub_category_id")
	private InvoiceSubCategory invoiceSubCategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "selling_country_id")
	private TradingCountry sellingCountry;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trading_country_id")
	private TradingCountry tradingCountry;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tax_id")
	private Tax tax;

	@Column(name = "filter_el", length = 2000)
	@Size(max = 2000)
	private String filterEL;

	@Column(name = "tax_code_el", length = 2000)
	@Size(max = 2000)
	private String taxCodeEL;

	public InvoiceSubCategory getInvoiceSubCategory() {
		return invoiceSubCategory;

	}

	public void setInvoiceSubCategory(InvoiceSubCategory invoiceSubCategory) {
		this.invoiceSubCategory = invoiceSubCategory;
	}

	public TradingCountry getSellingCountry() {
		return sellingCountry;
	}

	public void setSellingCountry(TradingCountry sellingCountry) {
		this.sellingCountry = sellingCountry;
	}

	public TradingCountry getTradingCountry() {
		return tradingCountry;
	}

	public void setTradingCountry(TradingCountry tradingCountry) {
		this.tradingCountry = tradingCountry;
	}

	public Tax getTax() {
		return tax;
	}

	public void setTax(Tax tax) {
		this.tax = tax;
	}
	
	public String getTaxCodeEL() {
		return taxCodeEL;
	}

	public void setTaxCodeEL(String taxCodeEL) {
		this.taxCodeEL = taxCodeEL;
	}

	public String getFilterEL() {
		return filterEL;
	}

	public void setFilterEL(String filterEL) {
		this.filterEL = filterEL;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof InvoiceSubcategoryCountry)) {
            return false;
        }
        
        InvoiceSubcategoryCountry other = (InvoiceSubcategoryCountry) obj;
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())){
            return false;
        }
        return true;
    }
}