package org.meveo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.cpq.PriceDTO;
import org.meveo.api.dto.payment.MandatInfoDto;
import org.meveo.api.dto.payment.PaymentResponseDto;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.billing.Country;
import org.meveo.model.cpq.AttributeValidationType;
import org.meveo.model.cpq.CpqQuote;
import org.meveo.model.cpq.QuoteAttribute;
import org.meveo.model.cpq.commercial.PriceLevelEnum;
import org.meveo.model.cpq.enums.AttributeTypeEnum;
import org.meveo.model.payments.CardPaymentMethod;
import org.meveo.model.payments.CreditCardTypeEnum;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.DDPaymentMethod;
import org.meveo.model.payments.MandatStateEnum;
import org.meveo.model.payments.PaymentStatusEnum;
import org.meveo.model.quote.QuotePrice;
import org.meveo.model.shared.Address;
import org.meveo.model.shared.ContactInformation;
import org.meveo.model.shared.DateUtils;
import org.meveo.model.shared.Name;
import org.meveo.model.shared.Title;
import org.meveo.service.base.ValueExpressionWrapper;
import org.meveo.service.cpq.AttributeValueService;
import org.meveo.service.payments.impl.IngenicoGatewayPayment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingenico.connect.gateway.sdk.java.ApiException;
import com.ingenico.connect.gateway.sdk.java.Client;
import com.ingenico.connect.gateway.sdk.java.CommunicatorConfiguration;
import com.ingenico.connect.gateway.sdk.java.Factory;
import com.ingenico.connect.gateway.sdk.java.Marshaller;
import com.ingenico.connect.gateway.sdk.java.defaultimpl.DefaultMarshaller;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.AmountOfMoney;
import com.ingenico.connect.gateway.sdk.java.domain.definitions.BankAccountIban;
import com.ingenico.connect.gateway.sdk.java.domain.errors.definitions.APIError;
import com.ingenico.connect.gateway.sdk.java.domain.mandates.CreateMandateRequest;
import com.ingenico.connect.gateway.sdk.java.domain.mandates.CreateMandateResponse;
import com.ingenico.connect.gateway.sdk.java.domain.mandates.GetMandateResponse;
import com.ingenico.connect.gateway.sdk.java.domain.mandates.definitions.MandateAddress;
import com.ingenico.connect.gateway.sdk.java.domain.mandates.definitions.MandateContactDetails;
import com.ingenico.connect.gateway.sdk.java.domain.mandates.definitions.MandateCustomer;
import com.ingenico.connect.gateway.sdk.java.domain.mandates.definitions.MandatePersonalInformation;
import com.ingenico.connect.gateway.sdk.java.domain.mandates.definitions.MandatePersonalName;
import com.ingenico.connect.gateway.sdk.java.domain.mandates.definitions.MandateResponse;
import com.ingenico.connect.gateway.sdk.java.domain.payment.CreatePaymentRequest;
import com.ingenico.connect.gateway.sdk.java.domain.payment.CreatePaymentResponse;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Customer;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Order;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.Payment;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.PaymentStatusOutput;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.SepaDirectDebitPaymentMethodSpecificInput;
import com.ingenico.connect.gateway.sdk.java.domain.payment.definitions.SepaDirectDebitPaymentProduct771SpecificInput;

public class MyTest {

	public static void main(String[] args) throws BusinessException, JsonProcessingException {
		//IngenicoGatewayPayment ingenico=new IngenicoGatewayPayment();
		CpqQuote quote=new CpqQuote();
		quote.setId(1L);
		quote.setQuoteDate(new Date());
		QuoteAttribute quoteAttr=new QuoteAttribute();
		quoteAttr.setDateValue(DateUtils.parseDateWithPattern("01/08/2022", "dd/MM/yyyy"));
		quoteAttr.setId(1L);
		org.meveo.model.cpq.Attribute attr=new org.meveo.model.cpq.Attribute();
		attr.setAttributeType(AttributeTypeEnum.DATE);
		attr.setValidationType(AttributeValidationType.EL);
		attr.setValidationPattern("#{quoteAttribute.dateValue > quote.quoteDate}");
		quoteAttr.setAttribute(attr);
		
		AttributeValueService.validateValue(quoteAttr, quote, null, null, null);
		//boolean a=ValueExpressionWrapper.evaluateExpression("#{date < quote.quoteDate}", Boolean.class, DateUtils.parseDateWithPattern("01/08/2022", "dd/MM/yyyy"),quote);
		//System.out.println(a);
		
		/*********
		 * 
		 * {
createMandate body={
  "alias" : null,
  "customer" : {
    "bankAccountIban" : {
      "accountHolderName" : null,
      "iban" : "FR7630001007941234567890185"
    },
    "companyName" : "LES MOUSQUETAIRES",
    "contactDetails" : {
      "emailAddress" : null
    },
    "mandateAddress" : {
      "city" : "PARIS 15",
      "countryCode" : "FR",
      "houseNumber" : null,
      "street" : "24 RUE AUGUSTE CHABRIERES",
      "zip" : "75015"
    },
    "personalInformation" : {
      "name" : {
        "firstName" : "-",
        "surname" : "LES MOUSQUETAIRES"
      },
      "title" : "Mr"
    }
  },
  "customerReference" : "4179360464",
  "language" : null,
  "recurrenceType" : "RECURRING",
  "signatureType" : "UNSIGNED",
  "uniqueMandateReference" : "BPIAB0000002926FD010",
  "returnUrl" : null
}
 }
}
		 * ***/
//		CustomerAccount customerAccount=new CustomerAccount();
//		
//		ContactInformation ci=new ContactInformation();
//		ci.setEmail("rachid.aityaazza@opencellsoft.com");
//		Address address=new Address();
//		address.setAddress1("56 rue Kleber");
//		address.setCity("Paris");
//		Country country=new Country();
//		country.setDescription("France");
//		country.setCountryCode("FR");
//		address.setCountry(country);
//		address.setZipCode("92300");
//		
//		Name name=new Name();
//		name.setFirstName("-");
//		name.setLastName("ISSUE-SEPA-REJ-AC01");
//		Title title=new Title();
//		title.setDescription("Mr");
//		name.setTitle(title);
//		
//		customerAccount.setContactInformation(ci);
//		customerAccount.setAddress(address);
//		customerAccount.setName(name);
//		customerAccount.setExternalRef1("cust2");
//		customerAccount.setCode("4003868175");
//		String rum="BPIAB0000000002312FD673";
//		createMandate(customerAccount, "FR7630001007941234567890185",rum);
//		checkMandat("BPIAB0000002479FD02", null);
//		doPayment(null, rum, 3000L, customerAccount, null, null, null,null,null, "FR", null);
		MyTest test=new MyTest();
		//test.calculatePrices();
	}
	
    public static void createMandate(CustomerAccount customerAccount,String iban,String mandateReference) throws BusinessException {
    	try {
    		BankAccountIban bankAccountIban=new BankAccountIban(); 
    		bankAccountIban.setIban(iban);
 
    		MandateContactDetails contactDetails=new MandateContactDetails();
    		if(customerAccount.getContactInformation() != null ) {
    			//contactDetails.setEmailAddress(customerAccount.getContactInformation().getEmail()); 
    		}
    		
    		MandateAddress address=new MandateAddress();
    		if (customerAccount.getAddress() != null) {
    		address.setCity(customerAccount.getAddress().getCity());
    		address.setCountryCode(customerAccount.getAddress().getCountry() == null ? null : customerAccount.getAddress().getCountry().getCountryCode());
    		address.setStreet(customerAccount.getAddress().getAddress1());
    		address.setZip(customerAccount.getAddress().getZipCode());
    		}
    		MandatePersonalName name = new MandatePersonalName();
    		MandatePersonalInformation personalInformation =new MandatePersonalInformation();
    		if (customerAccount.getName() != null) {
    			name.setFirstName("-");
    			name.setSurname(customerAccount.getName().getLastName()); 
    			personalInformation.setTitle(customerAccount.getName().getTitle() == null ? "" : customerAccount.getName().getTitle().getDescription());
    		}  
    		personalInformation.setName(name);
    		MandateCustomer customer=new MandateCustomer();
    		customer.setBankAccountIban(bankAccountIban);
    		customer.setContactDetails(contactDetails);
    		customer.setMandateAddress(address);
    		customer.setPersonalInformation(personalInformation);
    		customer.setCompanyName(customerAccount.getName().getLastName());
    		
    		CreateMandateRequest body = new CreateMandateRequest();
    		body.setUniqueMandateReference(mandateReference);
    		body.setCustomer(customer);
    		body.setCustomerReference(customerAccount.getExternalRef1()); 
    		body.setRecurrenceType("RECURRING");
    		body.setSignatureType("UNSIGNED");
    		getClient(false);
    	      
            ObjectMapper mapper = new ObjectMapper(); 
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
    		System.out.println("createMandate body="+jsonString);
    		CreateMandateResponse response = client.merchant("bpifrance").mandates().create(body); 
    		System.out.println(response.getMandate().getStatus());
    		
    	} catch (ApiException ev) {
    		ev.printStackTrace();

    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    }
    /** The client. */
    private  static Client client = null;
    
    private static Marshaller marshaller = null;
    private static void getClient(boolean prod) {
    	Properties pros=new Properties();
    	pros.setProperty("connect.api.authorizationType", "V1HMAC");
    	pros.setProperty("connect.api.connectTimeout", "5000");
    	pros.setProperty("connect.api.endpoint.host", prod?"eu.api-ingenico.com":"eu.preprod.api-ingenico.com");
    	pros.setProperty("connect.api.endpoint.scheme", "https");
    	pros.setProperty("connect.api.integrator", "");
    	pros.setProperty("connect.api.socketTimeout", "300000"); 
        CommunicatorConfiguration communicatorConfiguration = new CommunicatorConfiguration(pros);
        communicatorConfiguration.setApiKeyId(prod?"f7ef4cbafd46c392":"7a6495ada4604559");
        communicatorConfiguration.setSecretApiKey(prod?"52prQNFnm0vz23KBfkpmcDa258jAFCyG1LiAuGAnz9E=":"G5HkI4/lsx3rnMx2mMjtQMBDuxAvugUTXL2+Po6h0Xs=");
         client = Factory.createClient(communicatorConfiguration);
         marshaller = DefaultMarshaller.INSTANCE;
    }
    
   
    public static MandatInfoDto checkMandat(String mandatReference, String mandateId) throws BusinessException {
    	getClient(true);
    	MandatInfoDto mandatInfoDto=new MandatInfoDto();
    	GetMandateResponse response = client.merchant("bpifrance").mandates().get(mandatReference); 
    	MandateResponse mandatResponse=response.getMandate();
    	if(mandatResponse!=null) { 
    		if("WAITING_FOR_REFERENCE".equals(mandatResponse.getStatus())) {
    			mandatInfoDto.setState(MandatStateEnum.waitingForReference); 
    		}else {
    			mandatInfoDto.setState(MandatStateEnum.valueOf(mandatResponse.getStatus().toLowerCase()));
    		}
    		mandatInfoDto.setReference(mandatResponse.getUniqueMandateReference());
    	}  
System.out.println(mandatInfoDto.getState());
    	return mandatInfoDto;

    } 
    
    private static PaymentResponseDto doPayment(String tokenId,String mandateidentification,Long ctsAmount, CustomerAccount customerAccount, String cardNumber,
            String ownerName, String cvv, String expirayDate, CreditCardTypeEnum cardType, String countryCode, Map<String, Object> additionalParams) throws BusinessException, JsonProcessingException {
		PaymentResponseDto doPaymentResponseDto = new PaymentResponseDto();
		doPaymentResponseDto.setPaymentStatus(PaymentStatusEnum.NOT_PROCESSED);
    	try {
    		getClient(false);
            CreatePaymentRequest body = buildPaymentRequest( tokenId, mandateidentification,ctsAmount, customerAccount, cardNumber, ownerName, cvv, expirayDate, cardType);
            
            ObjectMapper mapper = new ObjectMapper(); 
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            System.out.println("Body :"+jsonString);
            CreatePaymentResponse response = client.merchant("bpifrance").payments().create(body);
            
            if (response != null) {
            	System.out.println("doPayment RESPONSE :"+marshaller.marshal(response));
              
                doPaymentResponseDto.setPaymentID(response.getPayment().getId());
                doPaymentResponseDto.setPaymentStatus(mappingStaus(response.getPayment().getStatus()));
                if (response.getCreationOutput() != null) {
                    doPaymentResponseDto.setTransactionId(response.getCreationOutput().getExternalReference());
                    doPaymentResponseDto.setTokenId(response.getCreationOutput().getToken());
                    doPaymentResponseDto.setNewToken(response.getCreationOutput().getIsNewToken());
                }
                Payment payment = response.getPayment();
                if (payment != null && response.getPayment().getStatusOutput().getErrors() != null) {
                    PaymentStatusOutput statusOutput = payment.getStatusOutput();
                    if (statusOutput != null) {
                        List<APIError> errors = statusOutput.getErrors();
                        if (CollectionUtils.isNotEmpty(errors)) {
                            doPaymentResponseDto.setErrorMessage(errors.toString());
                            doPaymentResponseDto.setErrorCode(errors.get(0).getId()); 
                        }
                    }
                }
                return doPaymentResponseDto;
            } else {
                throw new BusinessException("Gateway response is null");
            }
    	} catch (ApiException e) {
    		e.printStackTrace();
			doPaymentResponseDto.setPaymentStatus(PaymentStatusEnum.ERROR);
			doPaymentResponseDto.setErrorMessage(e.getResponseBody());
			if (CollectionUtils.isNotEmpty(e.getErrors())) {
				doPaymentResponseDto.setErrorCode(e.getErrors().get(0).getId());
			}
		}
		return doPaymentResponseDto;
	}
    
    private static com.ingenico.connect.gateway.sdk.java.domain.definitions.Address getBillingAddress(CustomerAccount customerAccount) {
    	com.ingenico.connect.gateway.sdk.java.domain.definitions.Address billingAddress = new com.ingenico.connect.gateway.sdk.java.domain.definitions.Address();
        if (customerAccount.getAddress() != null) {
            billingAddress.setAdditionalInfo(customerAccount.getAddress().getAddress3());
            billingAddress.setCity(customerAccount.getAddress().getCity());
            billingAddress.setCountryCode(customerAccount.getAddress().getCountry() == null ? null : customerAccount.getAddress().getCountry().getCountryCode());
            billingAddress.setHouseNumber("");
            billingAddress.setState(customerAccount.getAddress().getState());
            billingAddress.setStreet(customerAccount.getAddress().getAddress1());
            billingAddress.setZip(customerAccount.getAddress().getZipCode());
        }
        return billingAddress;
    }
    private static CreatePaymentRequest buildPaymentRequest(String tokenId,String mandateidentification, Long ctsAmount, CustomerAccount customerAccount,
            String cardNumber, String ownerName, String cvv, String expirayDate, CreditCardTypeEnum cardType) {
        AmountOfMoney amountOfMoney = new AmountOfMoney();
        amountOfMoney.setAmount(ctsAmount);
        amountOfMoney.setCurrencyCode("EUR");

        Customer customer = new Customer();
        customer.setBillingAddress(getBillingAddress(customerAccount));

        Order order = new Order();
        order.setAmountOfMoney(amountOfMoney);
        order.setCustomer(customer);

        CreatePaymentRequest body = new CreatePaymentRequest();
       
            body.setSepaDirectDebitPaymentMethodSpecificInput(getSepaInput(tokenId, mandateidentification));
        
      

        body.setOrder(order);
        return body;
    }
    private static SepaDirectDebitPaymentMethodSpecificInput getSepaInput(String tokenId,String mandateidentification) {
        SepaDirectDebitPaymentMethodSpecificInput sepaPmInput = new SepaDirectDebitPaymentMethodSpecificInput();
        sepaPmInput.setPaymentProductId(771);
        sepaPmInput.setToken(tokenId);
        SepaDirectDebitPaymentProduct771SpecificInput sepaDirectDebitPaymentProduct771SpecificInput = new SepaDirectDebitPaymentProduct771SpecificInput();
        sepaDirectDebitPaymentProduct771SpecificInput.setMandateReference(mandateidentification);
        sepaPmInput.setPaymentProduct771SpecificInput(sepaDirectDebitPaymentProduct771SpecificInput);
        return sepaPmInput;
    }

    private static PaymentStatusEnum mappingStaus(String ingenicoStatus) {
        if (ingenicoStatus == null) {
            return PaymentStatusEnum.ERROR;
        }
        if ("CREATED".equals(ingenicoStatus) || "PAID".equals(ingenicoStatus) || "REFUNDED".equals(ingenicoStatus) || "CAPTURED".equals(ingenicoStatus)) {
            return PaymentStatusEnum.ACCEPTED;
        }
        if (ingenicoStatus.startsWith("PENDING")) {
            return PaymentStatusEnum.PENDING;
        }
        if (ingenicoStatus.equals("ACCOUNT_VERIFIED")) {
            return PaymentStatusEnum.PENDING;
        }
        if (ingenicoStatus.equals("AUTHORIZATION_REQUESTED")) {
            return PaymentStatusEnum.PENDING;
        }
        if (ingenicoStatus.equals("CAPTURE_REQUESTED")) {
            return PaymentStatusEnum.PENDING;
        }
        if (ingenicoStatus.equals("REJECTED_CAPTURE")) {
            return PaymentStatusEnum.REJECTED;
        }
        if (ingenicoStatus.equals("REVERSED")) {
            return PaymentStatusEnum.ACCEPTED;
        }
        if (ingenicoStatus.equals("CHARGEBACKED")) {
            return PaymentStatusEnum.ACCEPTED;
        }
        if (ingenicoStatus.equals("REFUND_REQUESTED")) {
            return PaymentStatusEnum.PENDING;
        }
        if (ingenicoStatus.equals("PAYOUT_REQUESTED")) {
            return PaymentStatusEnum.PENDING;
        }
        
        return PaymentStatusEnum.REJECTED;
    }
    private void calculatePrices() {
    	BigDecimal quoteTotalAmount=BigDecimal.ZERO;
        List<PriceDTO> pricesDTO =new ArrayList<>();
    	Map<Long, String> pricesPerType=new HashMap<Long, String>();
    	pricesPerType.put(100L, "L");
    	   pricesPerType
           .keySet()
           .stream()
           .map(key -> reducePrice(key))
           .filter(Optional::isPresent)
           .map(price -> {
               QuotePrice quotePrice = price.get();
               System.out.println(quotePrice.getAmountWithoutTax());
               pricesDTO.add(new PriceDTO(quotePrice));
               quoteTotalAmount.add(quotePrice.getAmountWithoutTax());
               return pricesDTO;
           }).collect(Collectors.toList());
    	   BigDecimal sum = pricesDTO.stream().map(o->o.getAmountWithoutTax()).reduce(BigDecimal.ZERO, BigDecimal::add);
    	   System.out.println("amount="+sum);
    }
    
    private Optional<QuotePrice> reducePrice(Long key) {
    	System.out.println("reducePrice");
    	QuotePrice qp=new QuotePrice();
    	qp.setAmountWithoutTax(new BigDecimal(key));
    	return Optional.of(qp);
    }
}
