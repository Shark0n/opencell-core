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
package org.meveo.service.payments.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.NoAllOperationUnmatchedException;
import org.meveo.admin.exception.UnbalanceAmountException;
import org.meveo.api.dto.payment.DoPaymentResponseDto;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.payments.CardToken;
import org.meveo.model.payments.CreditCardTypeEnum;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.MatchingTypeEnum;
import org.meveo.model.payments.OCCTemplate;
import org.meveo.model.payments.Payment;
import org.meveo.model.payments.PaymentMethodEnum;
import org.meveo.service.base.PersistenceService;

/**
 * Payment service implementation.
 */
@Stateless
public class PaymentService extends PersistenceService<Payment> {

	@Inject
	private CardTokenService cardTokenService;

	@Inject
	private OCCTemplateService oCCTemplateService;

	@Inject
	private MatchingCodeService matchingCodeService;

	public DoPaymentResponseDto doPaymentCardToken(CustomerAccount customerAccount, Long ctsAmount, List<Long> aoIdsToPay,boolean createAO,boolean matchingAO) throws BusinessException, NoAllOperationUnmatchedException, UnbalanceAmountException {
		if(customerAccount.getPaymentTokens() == null || customerAccount.getPaymentTokens().isEmpty()){
			throw new BusinessException("There no payment token for customerAccount:"+customerAccount.getCode());
		}
		GatewayPaymentInterface  gatewayPaymentInterface = GatewayPaymentFactory.getInstance(GatewayPaymentNamesEnum.valueOf(ParamBean.getInstance().getProperty("meveo.gatewayPayment", "INGENICO")));		
		if(!PaymentMethodEnum.CARD.name().equals(customerAccount.getPaymentMethod().name())){
			throw new BusinessException("Unsupported payment method:"+customerAccount.getPaymentMethod());
		}		

		DoPaymentResponseDto doPaymentResponseDto =  gatewayPaymentInterface.doPaymentToken(cardTokenService.getPreferedToken(customerAccount), ctsAmount);
		//TODO succes payment status
		if(true /*doPaymentResponseDto.getPaymentStatus()*/){
			Long aoPaymentId = null;
			if(createAO){
				try{
					aoPaymentId = createPaymentAO(customerAccount, ctsAmount, doPaymentResponseDto.getPaymentID());
					doPaymentResponseDto.setAoCreated(true);
				}catch (Exception e) {
					log.warn("Cant create Account operation payment :"+e.getMessage());
				}
				if(matchingAO){
					try{
						createMatching(customerAccount, ctsAmount, aoPaymentId, aoIdsToPay);
						doPaymentResponseDto.setMatchingCreated(true);
					}catch (Exception e) {
						log.warn("Cant create matching :"+e.getMessage());
					}
				}
			}
		}

		return doPaymentResponseDto;
	}

	/**
	 * 
	 * @param customerAccount
	 * @param ctsAmount
	 * @param invoice
	 * @param cardNumber
	 * @param ownerName
	 * @param cvv
	 * @param expirayDate format :MMyy
	 * @return
	 * @throws BusinessException
	 * @throws UnbalanceAmountException 
	 * @throws NoAllOperationUnmatchedException 
	 */
	public DoPaymentResponseDto doPaymentCard(CustomerAccount customerAccount, Long ctsAmount,String cardNumber,String ownerName, String cvv,String expirayDate,
			CreditCardTypeEnum cardType, List<Long> aoIdsToPay,boolean createAO,boolean matchingAO ) throws BusinessException, NoAllOperationUnmatchedException, UnbalanceAmountException {
		GatewayPaymentInterface  gatewayPaymentInterface = GatewayPaymentFactory.getInstance(GatewayPaymentNamesEnum.valueOf(ParamBean.getInstance().getProperty("meveo.gatewayPayment", "INGENICO")));		
		if(!PaymentMethodEnum.CARD.name().equals(customerAccount.getPaymentMethod().name())){
			throw new BusinessException("Unsupported payment method:"+customerAccount.getPaymentMethod());
		}				
		DoPaymentResponseDto doPaymentResponseDto =  gatewayPaymentInterface.doPaymentCard(customerAccount, ctsAmount, cardNumber, ownerName,  cvv, expirayDate,cardType);		
		//TODO succes payment status
		if(true /*doPaymentResponseDto.getPaymentStatus()*/){
			CardToken cardToken = new CardToken(); 
			cardToken.setAlias("Card_"+ownerName);
			cardToken.setCardNumber(cardNumber);
			cardToken.setCardType(cardType);
			cardToken.setCustomerAccount(customerAccount);
			cardToken.setDefault(true);		
			cardToken.setHiddenCardNumber(StringUtils.hideCardNumber(cardNumber));
			cardToken.setMonthExpiration(new Integer(expirayDate.substring(0, 2)));
			cardToken.setYearExpiration(new Integer(expirayDate.substring(2, 4)));
			cardToken.setOwner(ownerName);
			cardToken.setTokenId(doPaymentResponseDto.getTokenId());
			cardTokenService.create(cardToken);	
			Long aoPaymentId = null;
			if(createAO){
				try{
					aoPaymentId = createPaymentAO(customerAccount, ctsAmount, doPaymentResponseDto.getPaymentID());
					doPaymentResponseDto.setAoCreated(true);
				}catch (Exception e) {
					log.warn("Cant create Account operation payment :"+e.getMessage());
				}
				if(matchingAO){
					try{
						createMatching(customerAccount, ctsAmount, aoPaymentId, aoIdsToPay);
						doPaymentResponseDto.setMatchingCreated(true);
					}catch (Exception e) {
						log.warn("Cant create matching :"+e.getMessage());
					}
				}
			}			
		}
		return doPaymentResponseDto;
	}

/**
 * 
 * @param customerAccount
 * @param ctsAmount
 * @param paymentID
 * @return the AO id created
 * @throws BusinessException
 */
	public Long createPaymentAO(CustomerAccount customerAccount,Long ctsAmount,String paymentReference) throws BusinessException {
		OCCTemplate occTemplate = oCCTemplateService.findByCode(ParamBean.getInstance().getProperty("occ.payment.card", "RG_TIP"));
		if (occTemplate == null) {
			throw new BusinessException("Cannot find OCC Template with code=" + (ParamBean.getInstance().getProperty("occ.payment.card", "RG_TIP")));
		}
		Payment payment = new Payment();
		payment.setPaymentMethod(customerAccount.getPaymentMethod());
		payment.setAmount((new BigDecimal(ctsAmount).divide(new BigDecimal(100))));
		payment.setUnMatchingAmount(payment.getAmount());
		payment.setMatchingAmount(BigDecimal.ZERO);
		payment.setAccountCode(occTemplate.getAccountCode());
		payment.setOccCode(occTemplate.getCode());
		payment.setOccDescription(occTemplate.getDescription());
		payment.setTransactionCategory(occTemplate.getOccCategory());
		payment.setAccountCodeClientSide(occTemplate.getAccountCodeClientSide());
		payment.setCustomerAccount(customerAccount);
		payment.setReference(paymentReference);				
		payment.setTransactionDate(new Date());
		payment.setMatchingStatus(MatchingStatusEnum.O);				
		create(payment);
		return payment.getId();

	}
	
	
/**
 * Create matching for aoPaymentId and aoIdsToPay
 * @param customerAccount
 * @param ctsAmount
 * @param aoPaymentId
 * @param aoIdsToPay
 * @throws BusinessException
 * @throws NoAllOperationUnmatchedException
 * @throws UnbalanceAmountException
 */
	public void createMatching(CustomerAccount customerAccount,Long ctsAmount,Long aoPaymentId,List<Long> aoIdsToPay) throws BusinessException, NoAllOperationUnmatchedException, UnbalanceAmountException {
		List<Long> listReferenceToMatch = aoIdsToPay;
		if(listReferenceToMatch != null){							
			listReferenceToMatch.add(aoPaymentId);
			matchingCodeService.matchOperations(null, customerAccount.getCode(), listReferenceToMatch, null, MatchingTypeEnum.A);
		}			
	}
}
