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
package org.meveo.admin.sepa;

//import java.text.Normalizer;
import java.math.RoundingMode;
import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.GrpHdr;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.GrpHdr.InitgPty;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.Cdtr;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrAcct;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrAcct.Id;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrAgt;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrAgt.FinInstnId;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrSchmeId;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrSchmeId.Id.PrvtId;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrSchmeId.Id.PrvtId.Othr;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrSchmeId.Id.PrvtId.Othr.SchmeNm;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.Dbtr;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DbtrAcct;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DbtrAgt;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DrctDbtTx;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DrctDbtTx.MndtRltdInf;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.InstdAmt;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.PmtId;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.PmtTpInf;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.PmtTpInf.LclInstrm;
import org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.PmtTpInf.SvcLvl;
import org.meveo.admin.util.ArConfig;
import org.meveo.commons.utils.ParamBean;
//import org.meveo.commons.utils.StringUtils;
import org.meveo.model.billing.BankCoordinates;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.DDPaymentMethod;
import org.meveo.model.payments.DDRequestItem;
import org.meveo.model.payments.DDRequestLOT;
import org.meveo.model.payments.PaymentMethod;
import org.meveo.model.payments.RecordedInvoice;
import org.meveo.model.shared.DateUtils;
import org.meveo.util.ApplicationProvider;
import org.slf4j.Logger;

@Stateless
public class SepaFileBuilder {

    @Inject
    private Logger log;

    @Inject
    @ApplicationProvider
    private Provider appProvider;

    public void addHeader(CstmrDrctDbtInitn Message, DDRequestLOT ddRequestLOT) throws Exception {
        GrpHdr groupHeader = new GrpHdr();
        Message.setGrpHdr(groupHeader);
        groupHeader.setMsgId(ArConfig.getDDRequestHeaderReference() + "-" + ddRequestLOT.getId());
        groupHeader.setCreDtTm(DateUtils.dateToXMLGregorianCalendar(new Date()));
        groupHeader.setNbOfTxs(ddRequestLOT.getDdrequestItems().size());
        groupHeader.setCtrlSum(ddRequestLOT.getInvoicesAmount().setScale(2, RoundingMode.HALF_UP));
        InitgPty initgPty = new InitgPty();
        initgPty.setNm(appProvider.getDescription());
        groupHeader.setInitgPty(initgPty);

    }

    public void addPaymentInformation(CstmrDrctDbtInitn Message, DDRequestItem dDRequestItem) throws Exception {

        log.info("addPaymentInformation dDRequestItem id=" + dDRequestItem.getId());

        PmtInf PaymentInformation = new PmtInf();
        Message.getPmtInf().add(PaymentInformation);
        PaymentInformation.setPmtInfId(ArConfig.getDDRequestHeaderReference() + "-" + dDRequestItem.getId());
        PaymentInformation.setPmtMtd(ParamBean.getInstance().getProperty("sepa.PmtMtd", "TRF"));
        PaymentInformation.setNbOfTxs(1);
        PaymentInformation.setCtrlSum(dDRequestItem.getAmount().setScale(2, RoundingMode.HALF_UP));
        PmtTpInf PaymentTypeInformation = new PmtTpInf();
        PaymentInformation.setPmtTpInf(PaymentTypeInformation);
        SvcLvl ServiceLevel = new SvcLvl();
        PaymentTypeInformation.setSvcLvl(ServiceLevel);
        ServiceLevel.setCd("SEPA");
        LclInstrm LocalInstrument = new LclInstrm();
        PaymentTypeInformation.setLclInstrm(LocalInstrument);
        LocalInstrument.setCd(ParamBean.getInstance().getProperty("sepa.LclInstrm", "CORE"));
        PaymentTypeInformation.setSeqTp("FRST");

        PaymentInformation.setReqdColltnDt(DateUtils.dateToXMLGregorianCalendar(new Date())); // à revoir

        BankCoordinates providerBC = appProvider.getBankCoordinates();
        if (providerBC == null) {
            throw new BusinessException("Missing bank information on provider");
        }
        Cdtr Creditor = new Cdtr();
        Creditor.setNm(appProvider.getDescription());
        PaymentInformation.setCdtr(Creditor);

        CdtrAcct creditorAccount = new CdtrAcct();
        PaymentInformation.setCdtrAcct(creditorAccount);
        Id identification = new Id();
        creditorAccount.setId(identification);
        identification.setIBAN(providerBC.getIban());

        CdtrAgt CreditorAgent = new CdtrAgt();
        PaymentInformation.setCdtrAgt(CreditorAgent);
        FinInstnId FinancialInstitutionIdentification = new FinInstnId();
        CreditorAgent.setFinInstnId(FinancialInstitutionIdentification);
        FinancialInstitutionIdentification.setBIC(providerBC.getBic());
        CdtrSchmeId CreditorSchemeIdentification = new CdtrSchmeId();
        PaymentInformation.setCdtrSchmeId(CreditorSchemeIdentification);
        org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrSchmeId.Id CdtrSchmeId = new org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.CdtrSchmeId.Id();
        CreditorSchemeIdentification.setId(CdtrSchmeId);
        PrvtId privateidentifier = new PrvtId();
        CdtrSchmeId.setPrvtId(privateidentifier);
        Othr other = new Othr();
        privateidentifier.setOthr(other);
        other.setId(providerBC.getIcs());
        SchmeNm SchemeName = new SchmeNm();
        other.setSchmeNm(SchemeName);
        SchemeName.setPrtry("SEPA");
        addTransaction(dDRequestItem.getRecordedInvoice(), PaymentInformation);
    }

    public void addTransaction(RecordedInvoice invoice, PmtInf PaymentInformation) throws Exception {
        CustomerAccount ca = invoice.getCustomerAccount();

        DrctDbtTxInf DirectDebitTransactionInformation = new DrctDbtTxInf();
        PaymentInformation.getDrctDbtTxInf().add(DirectDebitTransactionInformation);
        PmtId PaymentIdentification = new PmtId();
        DirectDebitTransactionInformation.setPmtId(PaymentIdentification);
        PaymentIdentification.setInstrId(invoice.getReference());
        PaymentIdentification.setEndToEndId(invoice.getReference());
        InstdAmt InstructedAmount = new InstdAmt();
        DirectDebitTransactionInformation.setInstdAmt(InstructedAmount);
        InstructedAmount.setValue(invoice.getAmount().setScale(2, RoundingMode.HALF_UP));
        InstructedAmount.setCcy("EUR");
        DrctDbtTx DirectDebitTransaction = new DrctDbtTx();
        DirectDebitTransactionInformation.setDrctDbtTx(DirectDebitTransaction);
        MndtRltdInf MandateRelatedInformation = new MndtRltdInf();
        DirectDebitTransaction.setMndtRltdInf(MandateRelatedInformation);
        PaymentMethod preferedPaymentMethod = ca.getPreferredPaymentMethod();
        if (preferedPaymentMethod != null && preferedPaymentMethod instanceof DDPaymentMethod) {
            MandateRelatedInformation.setMndtId(((DDPaymentMethod) preferedPaymentMethod).getMandateIdentification());
            MandateRelatedInformation.setDtOfSgntr(DateUtils.dateToXMLGregorianCalendar(((DDPaymentMethod) preferedPaymentMethod).getMandateDate()));
        }
        DbtrAgt DebtorAgent = new DbtrAgt();
        DirectDebitTransactionInformation.setDbtrAgt(DebtorAgent);
        org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DbtrAgt.FinInstnId FinancialInstitutionIdentification = new org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DbtrAgt.FinInstnId();
        FinancialInstitutionIdentification.setBIC(invoice.getPaymentInfo6());
        DebtorAgent.setFinInstnId(FinancialInstitutionIdentification);

        Dbtr Debtor = new Dbtr();
        DirectDebitTransactionInformation.setDbtr(Debtor);
        Debtor.setNm(ca.getDescription());

        DbtrAcct DebtorAccount = new DbtrAcct();
        DirectDebitTransactionInformation.setDbtrAcct(DebtorAccount);
        org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DbtrAcct.Id Identification = new org.meveo.admin.sepa.jaxb.Pain008.CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DbtrAcct.Id();
        Identification.setIBAN(invoice.getPaymentInfo());
        DebtorAccount.setId(Identification);

    }

    /*
     * private String enleverAccent(String value) { if (StringUtils.isBlank(value)) { return value; } return Normalizer.normalize(value,
     * Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", ""); }
     */
}
