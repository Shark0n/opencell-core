package org.meveo.api.dto.catalog;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.CustomFieldsDto;
import org.meveo.model.catalog.RecurringChargeTemplate;

/**
 * @author Edward P. Legaspi
 **/
@XmlRootElement(name = "RecurringChargeTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
public class RecurringChargeTemplateDto extends ChargeTemplateDto {

	private static final long serialVersionUID = 1652193975405244532L;

	@XmlElement(required = true)
	private String calendar;

	private Integer durationTermInMonth;
	private Boolean subscriptionProrata;
	private Boolean terminationProrata;
	private Boolean applyInAdvance=false;
	private Integer shareLevel;
	
	@Size(max = 2000)
    private String filterExpression = null;

	public RecurringChargeTemplateDto() {

	}

	public RecurringChargeTemplateDto(RecurringChargeTemplate e, CustomFieldsDto customFieldInstances) {
		super(e, customFieldInstances);
		durationTermInMonth = e.getDurationTermInMonth();
		subscriptionProrata = e.getSubscriptionProrata();
		terminationProrata = e.getTerminationProrata();
		applyInAdvance = e.getApplyInAdvance();
		setFilterExpression(e.getFilterExpression());
		if (e.getShareLevel() != null) {
			shareLevel = e.getShareLevel().getId();
		}
		if (e.getCalendar()!=null){
		    calendar = e.getCalendar().getCode();
		}
	}

	public Integer getDurationTermInMonth() {
		return durationTermInMonth;
	}

	public void setDurationTermInMonth(Integer durationTermInMonth) {
		this.durationTermInMonth = durationTermInMonth;
	}

	public Boolean getSubscriptionProrata() {
		return subscriptionProrata == null ? false : subscriptionProrata;
	}

	public void setSubscriptionProrata(Boolean subscriptionProrata) {
		this.subscriptionProrata = subscriptionProrata;
	}

	public Boolean getTerminationProrata() {
		return terminationProrata == null ? false : terminationProrata;
	}

	public void setTerminationProrata(Boolean terminationProrata) {
		this.terminationProrata = terminationProrata;
	}

	public Boolean getApplyInAdvance() {
		return applyInAdvance;
	}

	public void setApplyInAdvance(Boolean applyInAdvance) {
		this.applyInAdvance = applyInAdvance;
	}

	public Integer getShareLevel() {
		return shareLevel;
	}

	public void setShareLevel(Integer shareLevel) {
		this.shareLevel = shareLevel;
	}

	public String getCalendar() {
		return calendar;
	}

	public void setCalendar(String calendar) {
		this.calendar = calendar;
	}

	@Override
	public String toString() {
		return "RecurringChargeTemplateDto [calendar=" + calendar + ", durationTermInMonth=" + durationTermInMonth
				+ ", subscriptionProrata=" + subscriptionProrata + ", terminationProrata=" + terminationProrata
				+ ", applyInAdvance=" + applyInAdvance + ", shareLevel=" + shareLevel + ", getCode()=" + getCode()
				+ ", getDescription()=" + getDescription() + ", getLanguageDescriptions()=" + getLanguageDescriptions()
				+ ", toString()=" + super.toString() + ", getAmountEditable()=" + getAmountEditable()
				+ ", getInvoiceSubCategory()=" + getInvoiceSubCategory() + ", isDisabled()=" + isDisabled()
				+ ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + "]";
	}

    public String getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

}
