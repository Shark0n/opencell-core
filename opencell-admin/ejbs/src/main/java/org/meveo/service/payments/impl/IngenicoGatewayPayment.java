package org.meveo.service.payments.impl;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.payment.DoPaymentResponseDto;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.payments.CreditCardTypeEnum;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.PaymentToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.connect.gateway.sdk.java.ApiException;
import com.ingenico.connect.gateway.sdk.java.Client;
import com.ingenico.connect.gateway.sdk.java.CommunicatorConfiguration;
import com.ingenico.connect.gateway.sdk.java.DeclinedPaymentException;
import com.ingenico.connect.gateway.sdk.java.Factory;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.Address;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.AmountOfMoney;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.Card;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.CardWithoutCvv;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.CompanyInformation;
import com.ingenico.connect.gateway.sdk.java.domain.payment.CreatePaymentRequest;
import com.ingenico.connect.gateway.sdk.java.domain.payment.CreatePaymentResponse;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.CardPaymentMethodSpecificInput;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Customer;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Order;
import com.ingenico.connect.gateway.sdk.java.domain.token.CreateTokenRequest;
import com.ingenico.connect.gateway.sdk.java.domain.token.CreateTokenResponse;
import com.ingenico.connect.gateway.sdk.java.domain.token.definitions.CustomerToken;
import com.ingenico.connect.gateway.sdk.java.domain.token.definitions.PersonalInformationToken;
import com.ingenico.connect.gateway.sdk.java.domain.token.definitions.PersonalNameToken;
import com.ingenico.connect.gateway.sdk.java.domain.token.definitions.TokenCard;
import com.ingenico.connect.gateway.sdk.java.domain.token.definitions.TokenCardData;

public class IngenicoGatewayPayment implements GatewayPaymentInterface {
	
	protected Logger log = LoggerFactory.getLogger(IngenicoGatewayPayment.class);

	private static Client client = null;
	private String merchantId = ParamBean.getInstance().getProperty("ingenico.merchantId", null);

	private static void connect() {
		CommunicatorConfiguration communicatorConfiguration = new CommunicatorConfiguration(
				ParamBean.getInstance().getProperties());
		communicatorConfiguration.setApiKeyId(ParamBean.getInstance().getProperty("ingenico.ApiKeyId", null));
		communicatorConfiguration.setSecretApiKey(ParamBean.getInstance().getProperty("ingenico.SecretApiKey", null));
		client = Factory.createClient(communicatorConfiguration);
	}

	private static Client getClient() {
		if (client == null) {
			connect();
		}
		return client;
	}

	@Override
	public String createCardToken(CustomerAccount customerAccount, String alias, String cardNumber,
			String cardHolderName, String expirayDate, String issueNumber, int productPaymentId, String countryCode)throws BusinessException {
		try {		
			CompanyInformation companyInformation = new CompanyInformation();
			companyInformation.setName(customerAccount.getCode());

			PersonalNameToken name = new PersonalNameToken();
			if (customerAccount.getName() != null) {
				name.setFirstName(customerAccount.getName().getFirstName());
				name.setSurname(customerAccount.getName().getLastName());
				name.setSurnamePrefix(customerAccount.getName().getTitle() == null ? "" : customerAccount.getName().getTitle().getCode());
			}

			PersonalInformationToken personalInformation = new PersonalInformationToken();
			personalInformation.setName(name);

			CustomerToken customerToken = new CustomerToken();
			customerToken.setBillingAddress(getBillingAddress(customerAccount, countryCode));
			customerToken.setCompanyInformation(companyInformation);
			customerToken.setMerchantCustomerId(customerAccount.getCode());
			customerToken.setPersonalInformation(personalInformation);

			CardWithoutCvv cardWithoutCvv = new CardWithoutCvv();
			cardWithoutCvv.setCardholderName(cardHolderName);
			cardWithoutCvv.setCardNumber(cardNumber);
			cardWithoutCvv.setExpiryDate(expirayDate);
			cardWithoutCvv.setIssueNumber(issueNumber);

			TokenCardData tokenCardData = new TokenCardData();
			// Date of the first transaction (for ATOS),Format: YYYYMMDD
			tokenCardData.setFirstTransactionDate("");
			// Reference of the provider (of the first transaction) - used to
			// store the ATOS Transaction Certificate
			tokenCardData.setProviderReference("");
			tokenCardData.setCardWithoutCvv(cardWithoutCvv);

			TokenCard tokenCard = new TokenCard();
			tokenCard.setAlias(alias);
			tokenCard.setCustomer(customerToken);
			tokenCard.setData(tokenCardData);

			CreateTokenRequest body = new CreateTokenRequest();
			body.setCard(tokenCard);
			body.setPaymentProductId(productPaymentId);
			
			CreateTokenResponse response = getClient().merchant(merchantId).tokens().create(body);			
			if (!response.getIsNewToken()) {				
				throw new BusinessException("A token already exist for card:"+StringUtils.hideCardNumber(cardNumber));
			}
			return response.getToken();
		} catch (ApiException ev) {			
			throw new BusinessException(ev.getResponseBody());

		} catch (Exception e) {			
			throw new BusinessException(e.getMessage());
		}

	}

	@Override
	public DoPaymentResponseDto doPaymentToken(PaymentToken paymentToken, Long ctsAmount) throws BusinessException {

		return doPayment(paymentToken, ctsAmount, paymentToken.getCustomerAccount(), null, null, null,null, null,null);
	}

	@Override
	public DoPaymentResponseDto doPaymentCard(CustomerAccount customerAccount, Long ctsAmount, String cardNumber,
			String ownerName, String cvv, String expirayDate,CreditCardTypeEnum cardType,String countryCode) throws BusinessException {
		return doPayment(null, ctsAmount, customerAccount, cardNumber, ownerName, cvv, expirayDate,cardType,countryCode);
	}

	private DoPaymentResponseDto doPayment(PaymentToken paymentToken, Long ctsAmount, CustomerAccount customerAccount,String cardNumber, String ownerName, String cvv, 
			String expirayDate,CreditCardTypeEnum cardType,String countryCode) throws BusinessException {
		
		AmountOfMoney amountOfMoney = new AmountOfMoney();
		amountOfMoney.setAmount(ctsAmount);
		amountOfMoney.setCurrencyCode(customerAccount.getTradingCurrency().getCurrencyCode());
		
		Order order = new Order();
		order.setAmountOfMoney(amountOfMoney);

		CardPaymentMethodSpecificInput cardPaymentMethodSpecificInput = new CardPaymentMethodSpecificInput();
		if (paymentToken != null) {
			cardPaymentMethodSpecificInput.setToken(paymentToken.getTokenId());
		} else {
			Card card = new Card();
			card.setCardNumber(cardNumber);
			card.setCardholderName(ownerName);
			card.setCvv(cvv);
			card.setExpiryDate(expirayDate);
			cardPaymentMethodSpecificInput.setCard(card);
			cardPaymentMethodSpecificInput.setPaymentProductId(cardType.getId());
			Customer customer = new Customer();
			customer.setBillingAddress(getBillingAddress(customerAccount, countryCode));
			order.setCustomer(customer);
			
		}
		CreatePaymentRequest body = new CreatePaymentRequest();
		body.setCardPaymentMethodSpecificInput(cardPaymentMethodSpecificInput);
		body.setOrder(order);

		try {
			CreatePaymentResponse response = getClient().merchant(merchantId).payments().create(body);
			DoPaymentResponseDto doPaymentResponseDto = new DoPaymentResponseDto();
			doPaymentResponseDto.setPaymentID(response.getPayment().getId());
			doPaymentResponseDto.setPaymentStatus(response.getPayment().getStatus());
			doPaymentResponseDto.setTransactionId(response.getCreationOutput().getExternalReference());
			doPaymentResponseDto.setTokenId(response.getCreationOutput().getToken());
			doPaymentResponseDto.setNewToken(response.getCreationOutput().getIsNewToken());
			return doPaymentResponseDto;
		} catch (DeclinedPaymentException e) {
			throw new BusinessException(e.getResponseBody());
		} catch (ApiException e) {
			throw new BusinessException(e.getResponseBody());
		}
	}

	@Override
	public void cancelPayment(String paymentID) throws BusinessException {
		try{
			getClient().merchant(merchantId).payments().cancel(paymentID);
		}catch (ApiException e) {
			throw new BusinessException(e.getResponseBody());
		}		
	}
	
	private Address getBillingAddress(CustomerAccount customerAccount, String countryCode){
		Address billingAddress = new Address();
		if (customerAccount.getAddress() != null) {
			billingAddress.setAdditionalInfo(customerAccount.getAddress().getAddress3());
			billingAddress.setCity(customerAccount.getAddress().getCity());
			billingAddress.setCountryCode(countryCode);
			billingAddress.setHouseNumber(customerAccount.getAddress().getAddress1());
			billingAddress.setState(customerAccount.getAddress().getState());
			billingAddress.setStreet(customerAccount.getAddress().getAddress2());
			billingAddress.setZip(customerAccount.getAddress().getZipCode());
		}
		return billingAddress;
	}
}
