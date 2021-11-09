package org.meveo.service.catalog.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.NotFoundException;

import org.meveo.api.dto.catalog.PricePlanMatrixLineDto;
import org.meveo.api.dto.catalog.PricePlanMatrixValueDto;
import org.meveo.api.dto.response.catalog.PricePlanMatrixLinesDto;
import org.meveo.model.BaseEntity;
import org.meveo.model.catalog.ColumnTypeEnum;
import org.meveo.model.catalog.PricePlanMatrixColumn;
import org.meveo.model.catalog.PricePlanMatrixVersion;
import org.meveo.model.cpq.Attribute;
import org.meveo.model.cpq.Product;
import org.meveo.model.cpq.enums.AttributeTypeEnum;
import org.meveo.model.shared.DateUtils;
import org.meveo.service.base.BusinessService;

@Stateless
public class PricePlanMatrixColumnService extends BusinessService<PricePlanMatrixColumn> {

    @Inject
    private PricePlanMatrixValueService pricePlanMatrixValueService;
    
    @Inject
    private PricePlanMatrixColumnService pricePlanMatrixColumnService;

    public List<PricePlanMatrixColumn> findByAttributes(List<Attribute> attributes) {
        try {
            return getEntityManager().createNamedQuery("PricePlanMatrixColumn.findByAttributes", PricePlanMatrixColumn.class)
                    .setParameter("attributes", attributes)
                    .getResultList();
        }catch (NoResultException exp){
            return Collections.emptyList();
        }
    }

    public List<PricePlanMatrixColumn> findByProduct(Product product) {
        try {
            return getEntityManager().createNamedQuery("PricePlanMatrixColumn.findByProduct", PricePlanMatrixColumn.class)
                    .setParameter("product", product)
                    .getResultList();
        }catch (NoResultException exp){
            return Collections.emptyList();
        }
    }

    public void removePricePlanColumn(String code) {
        PricePlanMatrixColumn ppmColumn = findByCode(code);
        if(ppmColumn == null)
            return;
        Set<Long> valuesId = ppmColumn.getPricePlanMatrixValues().stream().map(BaseEntity::getId).collect(Collectors.toSet());
        if(!valuesId.isEmpty())
            pricePlanMatrixValueService.remove(valuesId);
        remove(ppmColumn);
    }
    

	@SuppressWarnings("unchecked")
	public List<PricePlanMatrixColumn> findByCodeAndPlanMaptrixVersion(String code, PricePlanMatrixVersion pricePlanMatrixVersion) {
			return this.getEntityManager().createNamedQuery("PricePlanMatrixColumn.findByVersion")
																			.setParameter("code", code)
																			.setParameter("pricePlanMatrixVersionId", pricePlanMatrixVersion.getId()).getResultList();
	}
	
	public PricePlanMatrixLinesDto populateLinesAndValues(String pricePlanMatrixCode, String data,
			PricePlanMatrixVersion pricePlanMatrixVersion) {
		Scanner scanner = new Scanner(data);
		List<String> columns = new LinkedList<String>();
		
		// Get columns types
		String line;
		populateColumns(pricePlanMatrixVersion, scanner, columns);
		
		// populate lines
		List<PricePlanMatrixLineDto> pricePlanMatrixLines = populateLines(pricePlanMatrixCode, pricePlanMatrixVersion,
				scanner, columns);
		scanner.close();
		
		PricePlanMatrixLinesDto pricePlanMatrixLinesDto = new PricePlanMatrixLinesDto();
		pricePlanMatrixLinesDto.setPricePlanMatrixCode(pricePlanMatrixCode);
		pricePlanMatrixLinesDto.setPricePlanMatrixVersion(pricePlanMatrixVersion.getCurrentVersion());
		pricePlanMatrixLinesDto.setPricePlanMatrixLines(pricePlanMatrixLines);
		return pricePlanMatrixLinesDto;
	}

	

	private void populateColumns(PricePlanMatrixVersion pricePlanMatrixVersion, Scanner scanner, List<String> columns) {
		String line = scanner.nextLine();
		String[] firstLine = line.split(";");
		for(int i = 0; i < firstLine.length; i++) {
			String column = firstLine[i].split("\\(")[0];
			if (!(column.equals("id") || column.equals("description") || column.equals("priority") || column.equals("pricetWithoutTax"))) {
				List<PricePlanMatrixColumn> PricePlanMatrixColumnList = pricePlanMatrixColumnService.findByCodeAndPlanMaptrixVersion(column, pricePlanMatrixVersion);
				if (PricePlanMatrixColumnList.isEmpty()) {
					throw new NotFoundException(
			                "PricePlanMatrixColumn with code= "+column+" does not exists");
				}
				PricePlanMatrixColumn pricePlanMatrixColumn = PricePlanMatrixColumnList.get(0);
				ColumnTypeEnum columnType;
				
				columnType = pricePlanMatrixColumn.getAttribute().getAttributeType().getColumnType(false);
				
				AttributeTypeEnum attributeType = pricePlanMatrixColumn.getAttribute().getAttributeType();
				
				if (attributeType.equals(AttributeTypeEnum.LIST_MULTIPLE_NUMERIC) || attributeType.equals(AttributeTypeEnum.LIST_MULTIPLE_TEXT)
						|| attributeType.equals(AttributeTypeEnum.LIST_NUMERIC) || attributeType.equals(AttributeTypeEnum.LIST_TEXT)) {
					columns.add(column+"|List");
					
				}else {
					columns.add(column+"|"+columnType.toString());
				}
				
				
			}else {
				columns.add(column);
			}
		}
	}
	
	private List<PricePlanMatrixLineDto> populateLines(String pricePlanMatrixCode,
			PricePlanMatrixVersion pricePlanMatrixVersion, Scanner scanner, List<String> columns) {
		String line;
		List<PricePlanMatrixLineDto> pricePlanMatrixLines = new ArrayList<PricePlanMatrixLineDto>();
			
		while (scanner.hasNextLine()) {
			
			PricePlanMatrixLineDto pricePlanMatrixLineDto = new PricePlanMatrixLineDto();
			List<PricePlanMatrixValueDto> PricePlanMatrixValueDtoList = new LinkedList<PricePlanMatrixValueDto>();
			
			line = scanner.nextLine();
			String[] nextLine = line.split(";");
			
			for(var j=0; j < columns.size() ; j++ ) {
				PricePlanMatrixValueDto pricePlanMatrixValueDto = new PricePlanMatrixValueDto();
				if (columns.get(j).toString().contains("|")) {
					
					String columnType = columns.get(j).toString().split("\\|")[1];
					String columnCode = columns.get(j).toString().split("\\|")[0];
				
					switch (columnType) {
					case "String":
						pricePlanMatrixValueDto.setPpmColumnCode(columnCode);
						pricePlanMatrixValueDto.setStringValue(nextLine[j]);
						PricePlanMatrixValueDtoList.add(pricePlanMatrixValueDto);
						break;
					case "Long":
						pricePlanMatrixValueDto.setPpmColumnCode(columnCode);
						pricePlanMatrixValueDto.setLongValue(nextLine[j].isEmpty()? null : Long.parseLong(nextLine[j]));
						PricePlanMatrixValueDtoList.add(pricePlanMatrixValueDto);
						break;
					case "Double":
						pricePlanMatrixValueDto.setPpmColumnCode(columnCode);
						pricePlanMatrixValueDto.setDoubleValue(nextLine[j].isEmpty()? null : Double.parseDouble(nextLine[j]));
						PricePlanMatrixValueDtoList.add(pricePlanMatrixValueDto);
						break;
					case "Boolean":
						pricePlanMatrixValueDto.setPpmColumnCode(columnCode);
						pricePlanMatrixValueDto.setBooleanValue(Boolean.valueOf(nextLine[j]));
						PricePlanMatrixValueDtoList.add(pricePlanMatrixValueDto);
						break;
					case "Range_Date":
						pricePlanMatrixValueDto.setPpmColumnCode(columnCode);
						pricePlanMatrixValueDto.setFromDateValue(nextLine[j].split("\\|")[0].isEmpty() ? null : DateUtils.parseDate(nextLine[j].split("\\|")[0]));
						pricePlanMatrixValueDto.setToDateValue(nextLine[j].split("\\|").length>1 ?DateUtils.parseDate(nextLine[j].split("\\|")[1]): null);
						PricePlanMatrixValueDtoList.add(pricePlanMatrixValueDto);
						break;
					case "Range_Numeric":
						pricePlanMatrixValueDto.setPpmColumnCode(columnCode);
						pricePlanMatrixValueDto.setFromDoubleValue(nextLine[j].split("\\|")[0].isEmpty() ? null : Double.parseDouble(nextLine[j].split("\\|")[0]));
						pricePlanMatrixValueDto.setToDoubleValue(nextLine[j].split("\\|").length>1 ? Double.parseDouble(nextLine[j].split("\\|")[1]) : null);
						PricePlanMatrixValueDtoList.add(pricePlanMatrixValueDto);
						break;
					case "List":
						pricePlanMatrixValueDto.setPpmColumnCode(columnCode);
						pricePlanMatrixValueDto.setStringValue(nextLine[j].replace("\\|", ";"));
						PricePlanMatrixValueDtoList.add(pricePlanMatrixValueDto);
						break;
					default:
						break;
					}
					
				}else {
					if (columns.get(j).equalsIgnoreCase("priority")) {
						pricePlanMatrixLineDto.setPriority(Integer.parseInt(nextLine[j]));
					}
					if (columns.get(j).equalsIgnoreCase("description")) {
						pricePlanMatrixLineDto.setDescription(nextLine[j]);
					}
					if (columns.get(j).equalsIgnoreCase("PricetWithoutTax")) {
						pricePlanMatrixLineDto.setPricetWithoutTax(new BigDecimal(nextLine[j]));
					}
					
				}
			
			}
			pricePlanMatrixLineDto.setPricePlanMatrixCode(pricePlanMatrixCode);
			pricePlanMatrixLineDto.setPricePlanMatrixVersion(pricePlanMatrixVersion.getCurrentVersion());
			pricePlanMatrixLineDto.setPricePlanMatrixValues(PricePlanMatrixValueDtoList);
			pricePlanMatrixLines.add(pricePlanMatrixLineDto);
		}
		return pricePlanMatrixLines;
	}
}
