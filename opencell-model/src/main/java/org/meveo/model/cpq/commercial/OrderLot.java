package org.meveo.model.cpq.commercial;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.BusinessCFEntity;
import org.meveo.model.CustomFieldEntity;
import org.meveo.model.quote.QuoteLot;

/** 
 * @author Tarik F.
 * @version 11.0
 */
@Entity
@Table(name = "cpq_order_lot", uniqueConstraints = @UniqueConstraint(columnNames = { "code", "order_id" }))
@CustomFieldEntity(cftCodePrefix = "OrderLot")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "cpq_order_lot_seq")})
public class OrderLot extends BusinessCFEntity{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	@NotNull
	private CommercialOrder order;
	
	@Column(name = "name", length = 50)
	@Size(max = 50)
	private String name;
	
	
    /**
   	 * quote product attached to this orderOffer
   	 */
       
   	@OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "quote_lot_id")
   	private QuoteLot quoteLot;

	/**
	 * @return the order
	 */
	public CommercialOrder getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(CommercialOrder order) {
		this.order = order;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public QuoteLot getQuoteLot() {
		return quoteLot;
	}

	public void setQuoteLot(QuoteLot quoteLot) {
		this.quoteLot = quoteLot;
	}
	
	
	
	
}
