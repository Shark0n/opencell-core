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
package org.meveo.admin.parse.xls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.exception.BusinessException;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.shared.Name;
import org.meveo.service.payments.impl.CustomerAccountService;
//import org.richfaces.event.FileUploadEvent;
//import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;

/**
 * Action for importing Customer Accounts.
 * 
 * @author Gediminas Ubartas
 * @since 2010.11.09
 */
@Named
@ConversationScoped
public class CustomerAccountImportAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	protected Logger log;

	@Inject
	private CustomerAccountService customerAccountService;

	private XLSFile xls;

	private List<String[]> importCustomerAccountData;

	private String filename;

	private Integer customerAccountsTotal;

	private Integer imported;

	private Integer failed;
	

    @Inject
    private Messages messages;

	/*
	 * TODO: @Create
	 * 
	 * @Begin(join = true)
	 */
	public void init() {
	}

	/**
	 * Importing uploaded files to database.
	 * 
	 * @return Return view.
	 * 
	 * @throws BusinessException
	 */
	public String doImport() throws BusinessException {
		if (xls == null) {
		    messages.error(new BundleKey("messages", "customerAccount.import.noFileUploaded"));
			return null;
		}
		List<CustomerAccount> failedImports = customerAccountService
				.importCustomerAccounts(convertData());

		failed = failedImports != null ? failedImports.size() : 0;
		imported = customerAccountsTotal - failed;

		// TODO show failed imports somewhere (probably write to other file)
		messages.info(new BundleKey("messages", "customerAccount.import.success"),customerAccountsTotal, imported, failed);
		return "customerAccounts";
	}

	/**
	 * Converts parsed data to required entity
	 * 
	 * @return List of Customer Accounts
	 */
	// TODO Fix mapping to Customer Account
	public List<CustomerAccount> convertData() {
		List<CustomerAccount> list = new ArrayList<CustomerAccount>();
		for (String[] customerAccountData : importCustomerAccountData) {
			CustomerAccount customerAccount = new CustomerAccount();
			customerAccount.setName(new Name(null, customerAccountData[0],null));
		}
		return list;
	}

	// TODO why synchronized??
//	public synchronized void uploadListener(FileUploadEvent  event) throws Exception {
//
//		UploadedFile item = event.getUploadedFile();
//
//		log.debug("#{currentUser.username} > Start processing uploaded XLS file (name='{}') ..",
//				item);
//
//		// file name validation
//		if (!validateFileNameAndExtention(item)) {
//			return;
//		}
//
//		filename = item.getName();
//		log.debug("#{currentUser.username} > Start parsing uploaded file ..");
//
//		try {
//// TODO			xls = new XLSFile(item.getData());
//			xls.parse();
//			importCustomerAccountData = xls.getContexts();
//			customerAccountsTotal = xls.getContexts().size();
//			imported = 0;
//			failed = 0;
//		} catch (Exception e) {
//			log.error("#{currentUser.username} > Error while parsing uploaded file", e);
//			throw e;
//		}

//		log.debug("#{currentUser.username} > Uploaded file parsed successfully");
//
//		log.debug("#{currentUser.username} > End processing uploaded XML file (name='{}')",
//				item.getName());
//	}

//	private boolean validateFileNameAndExtention(UploadedFile item) {
//		log.debug("#{currentUser.username} > Start uploaded file name and extention validation ..");
//		boolean valid = true;
//
//		if (item != null && item.getName() != null) {
//			int dot = item.getName().lastIndexOf(".");
//			String fileExt = item.getName().substring(dot + 1);
//			if (!fileExt.toUpperCase().equals("XLS") && !fileExt.toUpperCase().equals("TXT")) {
//				log.debug("#{currentUser.username} > File name validation failed!");
//				messages.error(new BundleKey("messages", "import.badFileExtension"));
//				valid = false;
//			}
//		} else {
//			log.debug("#{currentUser.username} > File name validation failed!");
//			messages.error(new BundleKey("messages", "import.fileNotFound"));
//			valid = false;
//		}
//
//		return valid;
//	}

	public String getFilename() {
		return filename;
	}

	public Integer getCustomerAccountsTotal() {
		return customerAccountsTotal;
	}

}
