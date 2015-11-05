/*
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.admin.action.billing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.DiscriminatorValue;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.international.status.builder.BundleKey;
import org.jboss.solder.servlet.http.RequestParam;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.InvoiceJasperNotFoundException;
import org.meveo.admin.exception.InvoiceXmlNotFoundException;
import org.meveo.admin.job.PDFParametersConstruction;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.Auditable;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.BillingRun;
import org.meveo.model.billing.CategoryInvoiceAgregate;
import org.meveo.model.billing.Invoice;
import org.meveo.model.billing.InvoiceAgregate;
import org.meveo.model.billing.InvoiceCategory;
import org.meveo.model.billing.InvoiceCategoryDTO;
import org.meveo.model.billing.InvoiceSubCategory;
import org.meveo.model.billing.InvoiceSubCategoryDTO;
import org.meveo.model.billing.InvoiceTypeEnum;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.model.billing.SubCategoryInvoiceAgregate;
import org.meveo.model.billing.Tax;
import org.meveo.model.billing.TaxInvoiceAgregate;
import org.meveo.model.shared.DateUtils;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.billing.impl.BillingAccountService;
import org.meveo.service.billing.impl.InvoiceAgregateService;
import org.meveo.service.billing.impl.InvoiceService;
import org.meveo.service.billing.impl.RatedTransactionService;
import org.meveo.service.billing.impl.XMLInvoiceCreator;
import org.meveo.service.payments.impl.CustomerAccountService;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.LazyDataModel;

/**
 * Standard backing bean for {@link Invoice} (extends {@link BaseBean} that
 * provides almost all common methods to handle entities filtering/sorting in
 * datatable, their create, edit, view, delete operations). It works with Manaty
 * custom JSF components.
 */
@Named
@ViewScoped
public class InvoiceBean extends BaseBean<Invoice> {

	private static final long serialVersionUID = 1L;

	private ParamBean paramBean = ParamBean.getInstance();

	/**
	 * Injected
	 * 
	 * @{link Invoice} service. Extends {@link PersistenceService}.
	 */
	@Inject
	private InvoiceService invoiceService;

	@Inject
	BillingAccountService billingAccountService;

	@Inject
	CustomerAccountService customerAccountService;

	@Inject
	RatedTransactionService ratedTransactionService;

	@Inject
	InvoiceAgregateService invoiceAgregateService;

	@Inject
	XMLInvoiceCreator xmlInvoiceCreator;

	@Inject
	private PDFParametersConstruction pDFParametersConstruction;

	@Inject
	@RequestParam()
	private Instance<Long> adjustedInvoiceIdParam;

	@Inject
	@RequestParam()
	private Instance<Boolean> detailedParam;

	private Boolean detailedInvoiceAdjustment;

	private List<SubCategoryInvoiceAgregate> uiSubCategoryInvoiceAgregates;
	private List<RatedTransaction> uiRatedTransactions;

	/**
	 * Constructor. Invokes super constructor and provides class type of this
	 * bean for {@link BaseBean}.
	 */
	public InvoiceBean() {
		super(Invoice.class);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public Invoice initEntity() {
		Invoice invoice = super.initEntity();

		if (adjustedInvoiceIdParam != null && adjustedInvoiceIdParam.get() != null) {
			if (detailedParam != null && invoice.isTransient()) {
				if (isDetailed()) {
					invoice.setDetailedInvoice(true);
				} else {
					invoice.setDetailedInvoice(false);
				}
			}
		}

		if (invoice.isTransient() && adjustedInvoiceIdParam != null && adjustedInvoiceIdParam.get() != null) {
			if (invoice.getAdjustedInvoice() == null) {
				Invoice adjustedInvoice = invoiceService.findById(adjustedInvoiceIdParam.get());
				invoice.setAdjustedInvoice(adjustedInvoice);
				invoice.setBillingAccount(adjustedInvoice.getBillingAccount());
				invoice.setBillingRun(adjustedInvoice.getBillingRun());
				invoice.setDueDate(adjustedInvoice.getDueDate());
				invoice.setInvoiceDate(adjustedInvoice.getInvoiceDate());
				invoice.setPaymentMethod(adjustedInvoice.getPaymentMethod());
				invoice.setInvoiceNumber(invoiceService.getInvoiceAdjustmentNumber(invoice, getCurrentUser()));
				invoice.setInvoiceTypeEnum(InvoiceTypeEnum.CREDIT_NOTE_ADJUST);

				// duplicate rated transaction for detailed
				// invoice adjustment
				if (isDetailed()) {
					uiRatedTransactions = new ArrayList<>();

					for (RatedTransaction ratedTransaction : ratedTransactionService.listByInvoice(adjustedInvoice)) {
						RatedTransaction newRatedTransaction = new RatedTransaction(ratedTransaction);

						newRatedTransaction.setInvoiceSubCategory(ratedTransaction.getInvoiceSubCategory());
						newRatedTransaction.setInvoice(invoice);
						newRatedTransaction.setAdjustedRatedTx(ratedTransaction);

						uiRatedTransactions.add(newRatedTransaction);
					}
				} else {
					Auditable auditable = new Auditable();
					auditable.setCreator(getCurrentUser());
					auditable.setCreated(new Date());
					uiSubCategoryInvoiceAgregates = new ArrayList<SubCategoryInvoiceAgregate>();
					for (InvoiceAgregate invoiceAgregate : adjustedInvoice.getInvoiceAgregates()) {
						if (invoiceAgregate instanceof CategoryInvoiceAgregate) {
							CategoryInvoiceAgregate categoryInvoiceAgregate = (CategoryInvoiceAgregate) invoiceAgregate;
							CategoryInvoiceAgregate newCategoryInvoiceAgregate = new CategoryInvoiceAgregate(
									categoryInvoiceAgregate);

							newCategoryInvoiceAgregate.setSubCategoryInvoiceAgregates(null);
							newCategoryInvoiceAgregate.setInvoice(invoice);
							newCategoryInvoiceAgregate.setAuditable(auditable);

							if (categoryInvoiceAgregate.getSubCategoryInvoiceAgregates() != null) {
								for (SubCategoryInvoiceAgregate subCategoryInvoiceAgregate : categoryInvoiceAgregate
										.getSubCategoryInvoiceAgregates()) {
									SubCategoryInvoiceAgregate newSubCategoryInvoiceAgregate = new SubCategoryInvoiceAgregate(
											subCategoryInvoiceAgregate);
									
									newSubCategoryInvoiceAgregate.setInvoice(invoice);
									newSubCategoryInvoiceAgregate.setAuditable(auditable);
									newSubCategoryInvoiceAgregate.setOldAmountWithoutTax(subCategoryInvoiceAgregate
											.getAmountWithoutTax());
									newSubCategoryInvoiceAgregate.setAmountWithTax(new BigDecimal(0));
									newSubCategoryInvoiceAgregate.setAmountTax(new BigDecimal(0));
									newSubCategoryInvoiceAgregate
											.setCategoryInvoiceAgregate(newCategoryInvoiceAgregate);

									newCategoryInvoiceAgregate
											.addSubCategoryInvoiceAggregate(newSubCategoryInvoiceAgregate);
									
									if (subCategoryInvoiceAgregate.getSubCategoryTaxes() != null) {
										for (Tax tax : subCategoryInvoiceAgregate.getSubCategoryTaxes()) {
											newSubCategoryInvoiceAgregate.addSubCategoryTax(tax);
										}
									}

									uiSubCategoryInvoiceAgregates.add(newSubCategoryInvoiceAgregate);									
								}
							}
						} else if (invoiceAgregate instanceof TaxInvoiceAgregate) {
							TaxInvoiceAgregate taxInvoiceAgregate = (TaxInvoiceAgregate) invoiceAgregate;
							TaxInvoiceAgregate newTaxInvoiceAgregate = new TaxInvoiceAgregate(taxInvoiceAgregate);

							newTaxInvoiceAgregate.setInvoice(invoice);
							newTaxInvoiceAgregate.setAuditable(auditable);
							newTaxInvoiceAgregate.setTax(taxInvoiceAgregate.getTax());
						}
					}
				}
			}
		} else if (adjustedInvoiceIdParam != null && adjustedInvoiceIdParam.get() != null) {
			if (!isDetailed()) {
				// load subCategoryInvoiceAggregates
				uiSubCategoryInvoiceAgregates = (List<SubCategoryInvoiceAgregate>) invoiceAgregateService
						.listByInvoiceAndType(invoice,
								SubCategoryInvoiceAgregate.class.getAnnotation(DiscriminatorValue.class).value());
			} else {
				// detailed
				uiRatedTransactions = ratedTransactionService.listByInvoice(entity);
			}
		} else {
			getPersistenceService().refresh(invoice);
		}

		return invoice;
	}

	/**
	 * Method, that is invoked in billing account screen. This method returns
	 * invoices associated with current Billing Account.
	 * 
	 */
	public LazyDataModel<Invoice> getBillingAccountInvoices(BillingAccount ba) {
		if (ba.getCode() == null) {
			log.warn("No billingAccount code");
		} else {
			filters.put("billingAccount", ba);
			filters.put("invoiceTypeEnum", InvoiceTypeEnum.COMMERCIAL);
			return getLazyDataModel();
		}

		return null;
	}

	/**
	 * @see org.meveo.admin.action.BaseBean#getPersistenceService()
	 */
	@Override
	protected IPersistenceService<Invoice> getPersistenceService() {
		return invoiceService;
	}

	public List<InvoiceCategoryDTO> getInvoiceCategories() {

		LinkedHashMap<String, InvoiceCategoryDTO> headerCategories = new LinkedHashMap<String, InvoiceCategoryDTO>();
		List<CategoryInvoiceAgregate> categoryInvoiceAgregates = new ArrayList<CategoryInvoiceAgregate>();
		for (InvoiceAgregate invoiceAgregate : entity.getInvoiceAgregates()) {
			if (invoiceAgregate instanceof CategoryInvoiceAgregate) {
				CategoryInvoiceAgregate categoryInvoiceAgregate = (CategoryInvoiceAgregate) invoiceAgregate;
				categoryInvoiceAgregates.add(categoryInvoiceAgregate);
			}
		}
		Collections.sort(categoryInvoiceAgregates, new Comparator<CategoryInvoiceAgregate>() {
			public int compare(CategoryInvoiceAgregate c0, CategoryInvoiceAgregate c1) {
				if (c0.getInvoiceCategory() != null && c1.getInvoiceCategory() != null
						&& c0.getInvoiceCategory().getSortIndex() != null
						&& c1.getInvoiceCategory().getSortIndex() != null) {
					return c0.getInvoiceCategory().getSortIndex().compareTo(c1.getInvoiceCategory().getSortIndex());
				}
				return 0;
			}
		});

		for (CategoryInvoiceAgregate categoryInvoiceAgregate : categoryInvoiceAgregates) {
			InvoiceCategory invoiceCategory = categoryInvoiceAgregate.getInvoiceCategory();
			InvoiceCategoryDTO headerCat = null;
			if (headerCategories.containsKey(invoiceCategory.getCode())) {
				headerCat = headerCategories.get(invoiceCategory.getCode());
				headerCat.addAmountWithoutTax(categoryInvoiceAgregate.getAmountWithoutTax());
				headerCat.addAmountWithTax(categoryInvoiceAgregate.getAmountWithTax());
			} else {
				headerCat = new InvoiceCategoryDTO();
				headerCat.setDescription(invoiceCategory.getDescription());
				headerCat.setCode(invoiceCategory.getCode());
				headerCat.setAmountWithoutTax(categoryInvoiceAgregate.getAmountWithoutTax());
				headerCat.setAmountWithTax(categoryInvoiceAgregate.getAmountWithTax());
				headerCategories.put(invoiceCategory.getCode(), headerCat);
			}

			Set<SubCategoryInvoiceAgregate> subCategoryInvoiceAgregates = categoryInvoiceAgregate
					.getSubCategoryInvoiceAgregates();
			LinkedHashMap<String, InvoiceSubCategoryDTO> headerSubCategories = headerCat.getInvoiceSubCategoryDTOMap();
			for (SubCategoryInvoiceAgregate subCatInvoiceAgregate : subCategoryInvoiceAgregates) {
				InvoiceSubCategory invoiceSubCategory = subCatInvoiceAgregate.getInvoiceSubCategory();
				InvoiceSubCategoryDTO headerSubCat = null;
				if (headerSubCategories.containsKey(invoiceSubCategory.getCode())) {
					headerSubCat = headerSubCategories.get(invoiceSubCategory.getCode());
					headerSubCat.addAmountWithoutTax(subCatInvoiceAgregate.getAmountWithoutTax());
					headerSubCat.addAmountWithTax(subCatInvoiceAgregate.getAmountWithTax());
				} else {
					headerSubCat = new InvoiceSubCategoryDTO();
					headerSubCat.setDescription(invoiceSubCategory.getDescription());
					headerSubCat.setCode(invoiceSubCategory.getCode());
					headerSubCat.setAmountWithoutTax(subCatInvoiceAgregate.getAmountWithoutTax());
					headerSubCat.setAmountWithTax(subCatInvoiceAgregate.getAmountWithTax());
					headerSubCat.setRatedTransactions(ratedTransactionService.getListByInvoiceAndSubCategory(entity,
							invoiceSubCategory));
					headerSubCategories.put(invoiceSubCategory.getCode(), headerSubCat);
				}
			}
		}
		return new ArrayList<InvoiceCategoryDTO>(headerCategories.values());
	}

	public String getNetToPay() throws BusinessException {
		BigDecimal balance = customerAccountService.customerAccountBalanceDue(null, entity.getBillingAccount()
				.getCustomerAccount().getCode(), entity.getDueDate(), entity.getProvider());

		if (balance == null) {
			throw new BusinessException("account balance calculation failed");
		}

		BigDecimal netToPay = BigDecimal.ZERO;
		if (entity.getProvider().isEntreprise()) {
			netToPay = entity.getAmountWithTax();
		} else {
			netToPay = entity.getAmountWithTax().add(balance);
		}
		return netToPay.setScale(2, RoundingMode.HALF_UP).toString();
	}

	public void deleteInvoicePdf() {
		try {
			entity.setPdf(null);
			invoiceService.update(entity);
			messages.info(new BundleKey("messages", "delete.successful"));
		} catch (Exception e) {
			log.error("failed to generate PDF ", e);
		}
	}

	public void generatePdf() {
		try {
			Map<String, Object> parameters = pDFParametersConstruction.constructParameters(entity);
			invoiceService.producePdf(parameters, getCurrentUser());
			messages.info(new BundleKey("messages", "invoice.pdfGeneration"));
		} catch (InvoiceXmlNotFoundException e) {
			messages.error(new BundleKey("messages", "invoice.xmlNotFound"));
		} catch (InvoiceJasperNotFoundException e) {
			messages.error(new BundleKey("messages", "invoice.jasperNotFound"));
		} catch (Exception e) {
			log.error("failed to generate PDF ", e);
		}
	}

	public List<SubCategoryInvoiceAgregate> getDiscountAggregates() {
		return invoiceAgregateService.findDiscountAggregates(entity);
	}

	public File getXmlInvoiceDir() {
		ParamBean param = ParamBean.getInstance();
		String invoicesDir = param.getProperty("providers.rootDir", "/tmp/meveo");
		File billingRundir = new File(invoicesDir
				+ File.separator
				+ getCurrentProvider().getCode()
				+ File.separator
				+ "invoices"
				+ File.separator
				+ "xml"
				+ File.separator
				+ (getEntity().getBillingRun() == null ? DateUtils.formatDateWithPattern(getEntity().getAuditable()
						.getCreated(), paramBean.getProperty("meveo.dateTimeFormat.string", "ddMMyyyy_HHmmss"))
						: getEntity().getBillingRun().getId()));
		return billingRundir;
	}

	public void generateXMLInvoice() throws BusinessException {
		try {
			xmlInvoiceCreator.createXMLInvoice(entity.getId(), getXmlInvoiceDir());
			messages.info(new BundleKey("messages", "invoice.xmlGeneration"));
		} catch (Exception e) {
			log.error("failed to generate xml invoice", e);
		}

	}

	public String downloadXMLInvoice() {
		String fileName = (entity.getInvoiceNumber() != null ? entity.getInvoiceNumber() : entity
				.getTemporaryInvoiceNumber()) + ".xml";

		return downloadXMLInvoice(fileName);
	}

	public String downloadXMLInvoiceAdjustment() {
		String fileName = paramBean.getProperty("invoicing.invoiceAdjustment.prefix", "_IA_")
				+ entity.getInvoiceNumber() + ".xml";

		return downloadXMLInvoice(fileName);
	}

	public String downloadXMLInvoice(String fileName) {
		log.info("start to download...");

		File file = new File(getXmlInvoiceDir().getAbsolutePath() + File.separator + fileName);
		try {
			javax.faces.context.FacesContext context = javax.faces.context.FacesContext.getCurrentInstance();
			HttpServletResponse res = (HttpServletResponse) context.getExternalContext().getResponse();
			res.setContentType("application/force-download");
			res.setContentLength((int) file.length());
			res.addHeader("Content-disposition", "attachment;filename=\"" + fileName + "\"");

			OutputStream out = res.getOutputStream();
			InputStream fin = new FileInputStream(file);

			byte[] buf = new byte[1024];
			int sig = 0;
			while ((sig = fin.read(buf, 0, 1024)) != -1) {
				out.write(buf, 0, sig);
			}
			fin.close();
			out.flush();
			out.close();
			context.responseComplete();
			log.info("download over!");
		} catch (Exception e) {
			log.error("Error:#0, when dowload file: #1", e.getMessage(), file.getAbsolutePath());
		}
		log.info("downloaded successfully!");
		return null;
	}

	public void deleteXmlInvoice() {
		try {
			File file = new File(getXmlInvoiceDir().getAbsolutePath() + File.separator
					+ entity.getTemporaryInvoiceNumber() + ".xml");
			if (file.exists()) {
				file.delete();
				messages.info(new BundleKey("messages", "delete.successful"));
			}
		} catch (Exception e) {
			log.error("failed to delete xml invoice ", e);
		}
	}

	public boolean isXmlInvoiceAlreadyGenerated() {
		String fileDir = getXmlInvoiceDir().getAbsolutePath()
				+ File.separator
				+ (getEntity().getInvoiceNumber() != null ? getEntity().getInvoiceNumber() : getEntity()
						.getTemporaryInvoiceNumber()) + ".xml";
		File file = new File(fileDir);
		return file.exists();
	}

	public boolean isXmlInvoiceAdjustmentAlreadyGenerated() {
		String fileDir = getXmlInvoiceDir().getAbsolutePath()
				+ File.separator
				+ paramBean.getProperty("invoicing.invoiceAdjustment.prefix", "_IA_")
				+ (getEntity().getInvoiceNumber() != null ? getEntity().getInvoiceNumber() : getEntity()
						.getTemporaryInvoiceNumber()) + ".xml";
		File file = new File(fileDir);
		return file.exists();
	}

	public void excludeBillingAccounts(BillingRun billingrun) {
		try {
			log.debug("excludeBillingAccounts getSelectedEntities=" + getSelectedEntities().size());
			if (getSelectedEntities() != null && getSelectedEntities().size() > 0) {
				for (Invoice invoice : getSelectedEntities()) {
					invoiceService.deleteInvoice(invoice);
					billingrun.getInvoices().remove(invoice);
				}
				messages.info(new BundleKey("messages", "info.invoicing.billingAccountExcluded"));
			} else {
				messages.error(new BundleKey("messages", "postInvoicingReport.noBillingAccountSelected"));
			}

		} catch (Exception e) {
			log.error("Failed to exclude BillingAccounts!", e);
			messages.error(new BundleKey("messages", "error.execution"));
		}
	}

	public BigDecimal totalInvoiceAdjustmentAmountWithoutTax() {
		BigDecimal total = new BigDecimal(0);
		if (entity != null && uiSubCategoryInvoiceAgregates != null) {
			for (SubCategoryInvoiceAgregate subCategoryInvoiceAgregate : uiSubCategoryInvoiceAgregates) {
				total = total.add(subCategoryInvoiceAgregate.getAmountWithoutTax());
			}
		}

		return total;
	}

	public BigDecimal totalInvoiceAdjustmentAmountTax() {
		BigDecimal total = new BigDecimal(0);
		if (entity != null && uiSubCategoryInvoiceAgregates != null) {
			for (SubCategoryInvoiceAgregate subCategoryInvoiceAgregate : uiSubCategoryInvoiceAgregates) {
				total = total.add(subCategoryInvoiceAgregate.getAmountTax());
			}
		}

		return total;
	}

	public BigDecimal totalInvoiceAdjustmentAmountWithTax() {
		BigDecimal total = new BigDecimal(0);
		if (entity != null && uiSubCategoryInvoiceAgregates != null) {
			for (SubCategoryInvoiceAgregate subCategoryInvoiceAgregate : uiSubCategoryInvoiceAgregates) {
				total = total.add(subCategoryInvoiceAgregate.getAmountWithTax());
			}
		}

		return total;
	}

	/**
	 * Detail invoice adjustments.
	 */

	public BigDecimal totalInvoiceAdjustmentDetailUnitAmountWithoutTax() {
		BigDecimal total = new BigDecimal(0);
		if (entity != null && uiRatedTransactions != null) {
			for (RatedTransaction ratedTransaction : uiRatedTransactions) {
				total = total.add(ratedTransaction.getUnitAmountWithoutTax());
			}
		}

		return total;
	}

	public BigDecimal totalInvoiceAdjustmentDetailUnitAmountWithTax() {
		BigDecimal total = new BigDecimal(0);
		if (entity != null && uiRatedTransactions != null) {
			for (RatedTransaction ratedTransaction : uiRatedTransactions) {
				total = total.add(ratedTransaction.getUnitAmountWithTax());
			}
		}

		return total;
	}

	public BigDecimal totalInvoiceAdjustmentDetailQuantity() {
		BigDecimal total = new BigDecimal(0);
		if (entity != null && uiRatedTransactions != null) {
			for (RatedTransaction ratedTransaction : uiRatedTransactions) {
				total = total.add(ratedTransaction.getQuantity());
			}
		}

		return total;
	}

	public BigDecimal totalInvoiceAdjustmentDetailAmountWithoutTax() {
		BigDecimal total = new BigDecimal(0);
		if (entity != null && uiRatedTransactions != null) {
			for (RatedTransaction ratedTransaction : uiRatedTransactions) {
				total = total.add(ratedTransaction.getAmountWithoutTax());
			}
		}

		return total;
	}

	public BigDecimal totalInvoiceAdjustmentDetailAmountWithTax() {
		BigDecimal total = new BigDecimal(0);
		if (entity != null && uiRatedTransactions != null) {
			for (RatedTransaction ratedTransaction : uiRatedTransactions) {
				total = total.add(ratedTransaction.getAmountWithTax());
			}
		}

		return total;
	}

	public void reComputeInvoiceAdjustment(SubCategoryInvoiceAgregate subCategoryInvoiceAgregate) {
		invoiceService.recomputeSubCategoryAggregate(entity, currentUser);
	}

	public void reComputeDetailedInvoiceAdjustment(RatedTransaction ratedTx) {
		ratedTx.recompute(entity.getProvider().isEntreprise());
	}

	public void testListener() {
		log.debug("testListener");
	}

	public Instance<Long> getAdjustedInvoiceIdParam() {
		return adjustedInvoiceIdParam;
	}

	public void setAdjustedInvoiceIdParam(Instance<Long> adjustedInvoiceIdParam) {
		this.adjustedInvoiceIdParam = adjustedInvoiceIdParam;
	}

	public Long getAdjustedInvoiceId() {
		if (getEntity() != null && getEntity().getAdjustedInvoice() != null) {
			return getEntity().getAdjustedInvoice().getId();
		}

		return adjustedInvoiceIdParam.get();
	}

	public String saveOrUpdateInvoiceAdjustment() throws Exception {
		if (entity.isTransient()) {
			if (isDetailed()) {
				for (RatedTransaction rt : uiRatedTransactions) {
					ratedTransactionService.create(rt, getCurrentUser());
				}
			}
			invoiceService.updateCreditNoteNb(entity, Long.parseLong(entity.getAlias()));
		}

		super.saveOrUpdate(false);

		if (isDetailed()) {
			ratedTransactionService.createInvoiceAndAgregates(entity.getBillingAccount(), entity, new Date(),
					getCurrentUser(), true);
		} else {
			invoiceService.recomputeAggregates(entity, getCurrentUser());
		}

		invoiceService.updateInvoiceAdjustmentCurrentNb(entity);

		// create xml invoice adjustment
		String invoicesDir = paramBean.getProperty("providers.rootDir", "/tmp/meveo");
		File billingRundir = new File(invoicesDir + File.separator + getCurrentProvider().getCode() + File.separator
				+ "invoices" + File.separator + "xml" + File.separator
				+ entity.getAdjustedInvoice().getBillingRun().getId());
		billingRundir.mkdirs();
		xmlInvoiceCreator.createXMLInvoiceAdjustment(entity.getId(), billingRundir);

		// create pdf
		Map<String, Object> parameters = pDFParametersConstruction.constructParameters(entity.getId(),
				currentUser.getProvider());
		invoiceService.produceInvoiceAdjustmentPdf(parameters, currentUser);

		return "/pages/billing/invoices/invoiceDetail.jsf?objectId=" + entity.getAdjustedInvoice().getId() + "&cid="
				+ conversation.getId() + "&faces-redirect=true&includeViewParams=true";
	}

	public List<SubCategoryInvoiceAgregate> getUiSubCategoryInvoiceAgregates() {
		return uiSubCategoryInvoiceAgregates;
	}

	public void setUiSubCategoryInvoiceAgregates(List<SubCategoryInvoiceAgregate> uiSubCategoryInvoiceAgregates) {
		this.uiSubCategoryInvoiceAgregates = uiSubCategoryInvoiceAgregates;
	}

	public boolean isDetailed() {
		if (detailedInvoiceAdjustment == null && detailedParam != null && detailedParam.get() != null) {
			detailedInvoiceAdjustment = detailedParam.get();
		}

		return detailedInvoiceAdjustment;
	}

	public Instance<Boolean> getDetailedParam() {
		return detailedParam;
	}

	public void setDetailedParam(Instance<Boolean> detailedParam) {
		this.detailedParam = detailedParam;
	}

	public List<RatedTransaction> getUiRatedTransactions() {
		return uiRatedTransactions;
	}

	public void setUiRatedTransactions(List<RatedTransaction> uiRatedTransactions) {
		this.uiRatedTransactions = uiRatedTransactions;
	}

	public Boolean getDetailedInvoiceAdjustment() {
		return detailedInvoiceAdjustment;
	}

	public void setDetailedInvoiceAdjustment(Boolean detailedInvoiceAdjustment) {
		this.detailedInvoiceAdjustment = detailedInvoiceAdjustment;
	}

}