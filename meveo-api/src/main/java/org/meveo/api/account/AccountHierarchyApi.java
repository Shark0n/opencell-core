package org.meveo.api.account;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.DuplicateDefaultAccountException;
import org.meveo.api.dto.account.AccountHierarchyDto;
import org.meveo.api.dto.account.CRMAccountHierarchyDto;
import org.meveo.api.dto.account.CustomerHierarchyDto;
import org.meveo.api.dto.account.CustomersDto;
import org.meveo.api.dto.account.FindAccountHierachyRequestDto;
import org.meveo.api.dto.response.account.GetAccountHierarchyResponseDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.model.admin.User;
import org.meveo.service.crm.impl.AccountHierarchyApiService;

/**
 *
 * Creates the customer heirarchy including : - Trading Country - Trading
 * Currency - Trading Language - Customer Brand - Customer Category - Seller -
 * Customer - Customer Account - Billing Account - User Account
 * 
 * Required Parameters :customerId, customerBrandCode,customerCategoryCode,
 * sellerCode ,currencyCode,countryCode,lastName,languageCode,billingCycleCode
 *
 */

@Stateless
public class AccountHierarchyApi {

	@Inject
	private AccountHierarchyApiService accountHierarchyApiService;

	/**
	 * 
	 * @param postData
	 * @param currentUser
	 * @throws MeveoApiException
	 */
	public void create(AccountHierarchyDto postData, User currentUser)
			throws MeveoApiException {
		accountHierarchyApiService.create(postData, currentUser);
	}

	/**
	 * 
	 * @param postData
	 * @param currentUser
	 * @throws MeveoApiException
	 */
	public void update(AccountHierarchyDto postData, User currentUser)
			throws MeveoApiException {
		accountHierarchyApiService.update(postData, currentUser);
	}

	/**
	 * 
	 * @param postData
	 * @param currentUser
	 * @return
	 * @throws MeveoApiException
	 */
	public CustomersDto find(AccountHierarchyDto postData, User currentUser)
			throws MeveoApiException {
		return accountHierarchyApiService.find(postData, currentUser);
	}

	/**
	 * 
	 * @param postData
	 * @param currentUser
	 * @throws MeveoApiException
	 */
	public void customerHierarchyUpdate(CustomerHierarchyDto postData,
			User currentUser) throws MeveoApiException {
		accountHierarchyApiService.customerHierarchyUpdate(postData,
				currentUser);
	}

	/**
	 * 
	 * @param postData
	 * @param currentUser
	 * @return
	 * @throws MeveoApiException
	 */
	public GetAccountHierarchyResponseDto findAccountHierarchy2(
			FindAccountHierachyRequestDto postData, User currentUser)
			throws MeveoApiException {
		return accountHierarchyApiService.findAccountHierarchy2(postData,
				currentUser);
	}

	/**
	 * 
	 * @param postData
	 * @param currentUser
	 * @throws MeveoApiException
	 */
	public void createCRMAccountHierarchy(CRMAccountHierarchyDto postData,
			User currentUser) throws MeveoApiException,
			DuplicateDefaultAccountException {
		accountHierarchyApiService.createCRMAccountHierarchy(postData,
				currentUser);
	}

	/**
	 * 
	 * @param postData
	 * @param currentUser
	 * @throws MeveoApiException
	 * @throws DuplicateDefaultAccountException
	 */
	public void updateCRMAccountHierarchy(CRMAccountHierarchyDto postData,
			User currentUser) throws MeveoApiException,
			DuplicateDefaultAccountException {
		accountHierarchyApiService.updateCRMAccountHierarchy(postData,
				currentUser);
	}

	/**
	 * Create or update Account Hierarchy based on code.
	 * 
	 * @param postData
	 * @param currentUser
	 * @throws MeveoApiException
	 */
	public void createOrUpdate(AccountHierarchyDto postData, User currentUser)
			throws MeveoApiException {
		accountHierarchyApiService.createOrUpdate(postData, currentUser);
	}

	/**
	 * 
	 * @param postData
	 * @param currentUser
	 * @throws MeveoApiException
	 */
	public void createOrUpdateCRMAccountHierarchy(
			CRMAccountHierarchyDto postData, User currentUser)
			throws MeveoApiException, DuplicateDefaultAccountException {
		accountHierarchyApiService.createOrUpdateCRMAccountHierarchy(postData,
				currentUser);
	}
}
