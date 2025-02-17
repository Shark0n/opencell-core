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

package org.meveo.admin.job.importexport;

import java.io.File;

import org.meveo.commons.utils.JAXBUtils;
import org.meveo.model.jaxb.account.Address;
import org.meveo.model.jaxb.account.BankCoordinates;
import org.meveo.model.jaxb.account.BillingAccount;
import org.meveo.model.jaxb.account.BillingAccounts;
import org.meveo.model.jaxb.account.Name;
import org.meveo.model.jaxb.account.UserAccount;
import org.meveo.model.jaxb.account.UserAccounts;
import org.meveo.model.jaxb.customer.Customer;
import org.meveo.model.jaxb.customer.CustomerAccount;
import org.meveo.model.jaxb.customer.CustomerAccounts;
import org.meveo.model.jaxb.customer.Customers;
import org.meveo.model.jaxb.customer.Seller;
import org.meveo.model.jaxb.customer.Sellers;
import org.meveo.model.jaxb.subscription.ServiceInstance;
import org.meveo.model.jaxb.subscription.Services;
import org.meveo.model.jaxb.subscription.Status;
import org.meveo.model.jaxb.subscription.Subscription;
import org.meveo.model.jaxb.subscription.Subscriptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateImportXml {

	/**
	 * @param args
	 */
	/************************** configuration properties ***********************************/
	private static int count = 9999;
	private static int startIndex = 9001;
	private static String customersFile = "/tmp/CUSTOMERS_ED.xml";
	private static String accountsFile = "/tmp/ACCOUNTS_ED.xml";
	private static String subscriptionsFile = "/tmp/SUBSCRIPTIONS_5bis.xml";
	private static String providerCode = "DEMO";
	private static String customerBrand = "DEMO";
	private static String customerCategory = "Business";
	private static String serviceCode = "EXCH20101";
	private static String creditCategory = "VIP";
	private static String offerCode = "EXCH20101";
	private static String billingCycle = "CYC_INV_MT";

	/***********************************************************************/
	
	private static final Logger log = LoggerFactory.getLogger(GenerateImportXml.class);

	/**
	 * @param args argumrents
	 */
	public static void main(String[] args) {
		try {
			Sellers sellers = new Sellers();
			Seller seller = new Seller();
			seller.setCode("SELLER_FR");
			seller.setDescription("french seller");
			seller.setTradingCountryCode("FR");
			seller.setTradingCurrencyCode("EUR");
			seller.setTradingLanguageCode("FRA");
			sellers.getSeller().add(seller);
			Customer customer = new Customer();
			Customers customers = new Customers();
			customers.getCustomer().add(customer);
			seller.setCustomers(customers);
			customer.setCode("CUST");
			customer.setCustomerCategory(customerCategory);
			customer.setCustomerBrand(customerBrand);
			customer.setDesCustomer("customer");
			CustomerAccounts customerAccounts = new CustomerAccounts();
			customer.setCustomerAccounts(customerAccounts);

			BillingAccounts billingAccounts = new BillingAccounts();

			ServiceInstance serviceInstance = new ServiceInstance();
			serviceInstance.setCode(serviceCode);
			serviceInstance.setSubscriptionDate("2014-02-20");
			Status status = new Status();
			status.setDate("2014-02-20");
			status.setValue("ACTIVE");
			serviceInstance.setStatus(status);
			serviceInstance.setQuantity("1");

			Subscriptions subscriptions = new Subscriptions();

			for (int i = startIndex; i <= count; i++) {
				// xml of customers
				CustomerAccount ca = new CustomerAccount();
				ca.setCode("CA" + i);
				ca.setCreditCategory(creditCategory);
				ca.setExternalRef1("ORCC25" + i);
				ca.setTradingCurrencyCode("EUR");
				ca.setTradingLanguageCode("FRA");
				Name name = new Name();
				name.setFirstName("firstName" + i);
				name.setLastName("lastName" + i);
				name.setTitle("SA");
				ca.setName(name);
				Address address = new Address();
				address.setAddress1("Porte Gauche");
				address.setCity("La Defense");
				address.setCountry("France");
				ca.setAddress(address);
				ca.setPaymentMethod("CHECK");
				customerAccounts.getCustomerAccount().add(ca);

				// xml of billingAccounts

				BillingAccount ba = new BillingAccount();
				ba.setCustomerAccountId("CA" + i);
				ba.setCode("BA" + i);
				ba.setPaymentMethod("DIRECTDEBIT");
				ba.setBillingCycle(billingCycle);
				ba.setSubscriptionDate("2014-02-20");
				ba.setExternalRef1("ORCC25" + i);
				ba.setTradingCountryCode("FR");
				ba.setTradingLanguageCode("FRA");

				org.meveo.model.jaxb.account.Name nameBa = new org.meveo.model.jaxb.account.Name();
				nameBa.setFirstName("firstName" + i);
				nameBa.setLastName("lastName" + i);
				nameBa.setTitle("SA");
				ba.setName(nameBa);
				org.meveo.model.jaxb.account.Address addressBa = new org.meveo.model.jaxb.account.Address();
				addressBa.setAddress1("Porte Gauche");
				addressBa.setZipCode("92000");
				addressBa.setCity("la Defense");
				addressBa.setCountry("France");
				ba.setAddress(addressBa);
				ba.setElectronicBilling("0");
				BankCoordinates bankCoordinate = new BankCoordinates(); 
				bankCoordinate.setKey("");
				ba.setBankCoordinates(bankCoordinate);
				billingAccounts.getBillingAccount().add(ba);

				UserAccounts usersAccounts = new UserAccounts();
				UserAccount userAccount = new UserAccount();
				userAccount.setCode("UA" + i);
				userAccount.setSubscriptionDate("2014-02-20");
				userAccount.setExternalRef1("25001-0" + i);

				org.meveo.model.jaxb.account.Name nameUA = new org.meveo.model.jaxb.account.Name();
				nameUA.setTitle("M");
				nameUA.setFirstName("firstName" + i);
				nameUA.setLastName("lastName" + i);
				userAccount.setName(nameUA);

				org.meveo.model.jaxb.account.Address addressUA = new org.meveo.model.jaxb.account.Address();
				addressUA.setAddress1("Porte Gauche");
				addressUA.setZipCode("92000" + i);
				addressUA.setCity("la Defense");
				addressUA.setCountry("France");
				userAccount.setAddress(addressUA);

				ba.setUserAccounts(usersAccounts);
				usersAccounts.getUserAccount().add(userAccount);

				// XML subscriptions
				Subscription sub = new Subscription();
				sub.setCode("SUB" + i);
				sub.setUserAccountId("UA" + i);
				sub.setOfferCode(offerCode);
				sub.setSubscriptionDate("2014-02-20");
				Status statuSub = new Status();
				statuSub.setDate("2014-02-20");
				statuSub.setValue("ACTIVE");
				sub.setStatus(statuSub);
				Services services = new Services();
				services.getServiceInstance().add(serviceInstance);
				sub.setServices(services);
				subscriptions.getSubscription().add(sub);

			}

			JAXBUtils.marshaller(sellers, new File(customersFile));
			JAXBUtils.marshaller(billingAccounts, new File(accountsFile));
			JAXBUtils.marshaller(subscriptions, new File(subscriptionsFile));

			log.info("Import effectué avec succes");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
