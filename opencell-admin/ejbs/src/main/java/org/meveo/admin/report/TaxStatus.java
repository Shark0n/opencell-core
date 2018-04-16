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
package org.meveo.admin.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.model.bi.OutputFormatEnum;
import org.meveo.model.bi.Report;
import org.meveo.model.crm.Provider;
import org.meveo.service.reporting.impl.JournalEntryService;
import org.meveo.util.ApplicationProvider;
import org.slf4j.Logger;

/**
 * @author Wassim Drira
 * @lastModifiedVersion 5.0
 * 
 */
@Named
public class TaxStatus extends FileProducer implements Reporting {

    @Inject
    protected Logger log;

    @Inject
    private JournalEntryService salesTransformationService;

    @Inject
    @ApplicationProvider
    private Provider appProvider;

    @Inject
    private ParamBeanFactory paramBeanFactory;

    public Map<String, Object> parameters = new HashMap<String, Object>();

    public void generateTaxStatusFile(Date startDate, Date endDate, OutputFormatEnum outputFormat) {
        // log.info("generateTaxStatusFile({},{})", startDate,endDate);
        try {
            // log.info("generateTaxStatusFile : file {}",
            // getFilename(startDate, endDate));
            File file = null;
            if (outputFormat == OutputFormatEnum.PDF) {
                file = File.createTempFile("tempAccountingDetail", ".csv");
            } else if (outputFormat == OutputFormatEnum.CSV) {
                StringBuilder sb = new StringBuilder(getFilename(startDate, endDate));
                sb.append(".csv");
                file = new File(sb.toString());
            }
            FileWriter writer = new FileWriter(file);
            writer.append("Code;Description;Pourcentage;Base HT;Taxe due");
            writer.append('\n');
            List<Object> taxes = salesTransformationService.getTaxRecodsBetweenDate(startDate, endDate);
            Iterator<Object> itr = taxes.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                if (row[0] != null)
                    writer.append(row[0].toString() + ";");
                else
                    writer.append(";");
                if (row[1] != null)
                    writer.append(row[1].toString() + ";");
                else
                    writer.append(";");
                if (row[2] != null)
                    writer.append(row[2].toString().replace('.', ',') + ";");
                else
                    writer.append(";");
                if (row[3] != null)
                    writer.append(row[3].toString().replace('.', ',') + ";");
                else
                    writer.append(";");
                if (row[4] != null)
                    writer.append(row[4].toString().replace('.', ','));
                writer.append('\n');
            }
            writer.flush();
            writer.close();
            if (outputFormat == OutputFormatEnum.PDF) {
                parameters.put("startDate", startDate);
                parameters.put("endDate", endDate);
                StringBuilder sb = new StringBuilder(getFilename(startDate, endDate));
                sb.append(".pdf");
                ParamBean param = paramBeanFactory.getInstance();
                String jasperTemplatesFolder = param.getProperty("reports.jasperTemplatesFolder", "/opt/jboss/files/reports/JasperTemplates/");
                String templateFilename = jasperTemplatesFolder + "taxStatus.jasper";
                generatePDFfile(file, sb.toString(), templateFilename, parameters);
            }
        } catch (IOException e) {
            log.error("failed to generate tax status file", e);
        }
    }

    public String getFilename(Date startDate, Date endDate) {

        String DATE_FORMAT = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        StringBuilder sb = new StringBuilder();
        ParamBean param = paramBeanFactory.getInstance();
        String reportsFolder = param.getProperty("reportsURL", "/opt/jboss/files/reports/");
        sb.append(reportsFolder);
        sb.append(appProvider.getCode());
        sb.append("_TAX_");
        sb.append(sdf.format(startDate).toString());
        sb.append("_");
        sb.append(sdf.format(endDate).toString());
        return sb.toString();
    }

    public void export(Report report) {
        generateTaxStatusFile(report.getStartDate(), report.getEndDate(), report.getOutputFormat());

    }

}
