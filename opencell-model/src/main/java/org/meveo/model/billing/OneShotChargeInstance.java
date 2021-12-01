/*
 * (C) Copyright 2015-2020 Opencell SAS (https://opencellsoft.com/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW. EXCEPT WHEN
 * OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM "AS
 * IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO
 * THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * For more information on the GNU Affero General Public License, please consult
 * <https://www.gnu.org/licenses/agpl-3.0.en.html>.
 */
package org.meveo.model.billing;

import org.meveo.model.catalog.OneShotChargeTemplate;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * One shot charge as part of subscribed service
 * 
 * @author Andrius Karpavicius
 */
@Entity
@DiscriminatorValue("O")
public class OneShotChargeInstance extends ChargeInstance {

    private static final long serialVersionUID = 1L;

    /**
     * Quantity subscribed
     */
    @Column(name = "quantity", precision = NB_PRECISION, scale = NB_DECIMALS, nullable = false)
    @NotNull
    protected BigDecimal quantity = BigDecimal.ONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id")
    private CounterInstance counter;

    public OneShotChargeInstance() {

    }

    /**
     * Constructor
     *
     * @param amountWithoutTax Amount without tax
     * @param amountWithTax Amount with tax
     * @param chargeTemplate Charge template
     * @param serviceInstance Service instance to associate with
     * @param status Status
     */
    protected OneShotChargeInstance(BigDecimal amountWithoutTax, BigDecimal amountWithTax, OneShotChargeTemplate chargeTemplate, ServiceInstance serviceInstance, InstanceStatusEnum status) {

        super(amountWithoutTax, amountWithTax, chargeTemplate, serviceInstance, status);

        this.quantity = serviceInstance.getQuantity() == null ? BigDecimal.ONE : serviceInstance.getQuantity();
    }

    /**
     * Constructor
     * 
     * @param description Charge description (to override a value from a charge template). Optional
     * @param chargeDate Charge date
     * @param amountWithoutTax Amount without tax
     * @param amountWithTax Amount with tax
     * @param quantity Quantity
     * @param orderNumber Order number
     * @param subscription Subscription
     * @param chargeTemplate Charge template
     */
    public OneShotChargeInstance(String description, Date chargeDate, BigDecimal amountWithoutTax, BigDecimal amountWithTax, BigDecimal quantity, String orderNumber, Subscription subscription,
            OneShotChargeTemplate chargeTemplate) {

        super(amountWithoutTax, amountWithTax, chargeTemplate, subscription, InstanceStatusEnum.ACTIVE);

        this.chargeDate = chargeDate;
        this.quantity = quantity == null ? BigDecimal.ONE : quantity;
        this.orderNumber = orderNumber;
        if (description != null) {
            this.description = description;
        }
    }

    /**
     * Constructor
     * 
     * @param description Charge description (to override a value from a charge template). Optional
     * @param chargeDate Charge date
     * @param amountWithoutTax Amount without tax
     * @param amountWithTax Amount with tax
     * @param quantity Quantity
     * @param orderNumber Order number
     * @param serviceInstance Service instance
     * @param chargeTemplate Charge template
     */
    public OneShotChargeInstance(String description, Date chargeDate, BigDecimal amountWithoutTax, BigDecimal amountWithTax, BigDecimal quantity, String orderNumber, ServiceInstance serviceInstance,
            OneShotChargeTemplate chargeTemplate) {

        super(amountWithoutTax, amountWithTax, chargeTemplate, serviceInstance, InstanceStatusEnum.ACTIVE);

        this.chargeDate = chargeDate;
        this.quantity = quantity == null ? BigDecimal.ONE : quantity;
        this.orderNumber = orderNumber;
        if (description != null) {
            this.description = description;
        }
    }

    /**
     * @return Quantity subscribed
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * @param quantity Quantity subscribed
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets a counter instance
     *
     * @return a counter instance
     */
    public CounterInstance getCounter() {
        return counter;
    }

    /**
     * Sets a counter instance.
     *
     * @param counter a counter instance
     */
    public void setCounter(CounterInstance counter) {
        this.counter = counter;
    }
}