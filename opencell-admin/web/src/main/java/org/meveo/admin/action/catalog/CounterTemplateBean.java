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
package org.meveo.admin.action.catalog;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.model.catalog.CounterTemplate;
import org.meveo.model.catalog.CounterTemplateLevel;
import org.meveo.model.catalog.CounterTypeEnum;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.catalog.impl.CounterTemplateService;
import org.primefaces.model.LazyDataModel;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class CounterTemplateBean extends BaseBean<CounterTemplate> {

    private static final long serialVersionUID = 1L;

    @Inject
    private CounterTemplateService counterTemplateService;

    /**
     * Constructor. Invokes super constructor and provides class type of this bean for {@link BaseBean}.
     */
    public CounterTemplateBean() {
        super(CounterTemplate.class);
    }

    /**
     * @see org.meveo.admin.action.BaseBean#getPersistenceService()
     */
    @Override
    protected IPersistenceService<CounterTemplate> getPersistenceService() {
        return counterTemplateService;
    }

    @Override
    protected String getDefaultSort() {
        return "code";
    }

    /**
     * DataModel for primefaces lazy loading datatable component.
     *
     * @param counterType counter type enumeration
     * @return LazyDataModel implementation.
     */
    public LazyDataModel<CounterTemplate> getLazyDataModel(CounterTypeEnum counterType) {
        filters.put("counterType", counterType);
        return getLazyDataModel(filters, false);
    }

    /**
     * DataModel for primefaces lazy loading datatable component.
     *
     * @return LazyDataModel implementation.
     */
    public LazyDataModel<CounterTemplate> getAccumulatorLazyDataModel() {
        filters.put("accumulator", true);
        return getLazyDataModel(filters, false);
    }

    @Override
    public String saveOrUpdate(boolean killConversation) throws BusinessException {

        String notificationLevels = entity.getNotificationLevels();
        if (!StringUtils.isBlank(notificationLevels)) {
            String[] levels = notificationLevels.split(",");
            for (String level : levels) {
                level = level.trim();
                if (StringUtils.isBlank(level)) {
                    continue;
                }
                double dblLevel = 0;
                try {
                    if (level.endsWith("%") && level.length() == 1) {
                        facesContext.validationFailed();
                        messages.error(new BundleKey("messages", "counterTemplate.invalidNotificationLevels"));
                        return null;

                    } else if (level.endsWith("%") && level.length() > 1) {
                        dblLevel = Double.parseDouble(level.substring(0, level.length() - 1));
                        if (dblLevel >= 100) {
                            facesContext.validationFailed();
                            messages.error(new BundleKey("messages", "counterTemplate.invalidNotificationLevels.higherNumbers"));
                            return null;
                        }

                    } else if (!level.endsWith("%")) {
                        dblLevel = Double.parseDouble(level);
                        if (entity.getCeiling() != null && entity.getCeiling().compareTo(BigDecimal.valueOf(dblLevel)) < 0 && !entity.getAccumulator()) {
                            facesContext.validationFailed();
                            messages.error(new BundleKey("messages", "counterTemplate.invalidNotificationLevels.higherNumbers"));
                            return null;
                        }
                    }
                } catch (Exception e) {
                    facesContext.validationFailed();
                    messages.error(new BundleKey("messages", "counterTemplate.invalidNotificationLevels"));
                    return null;
                }
            }

        }
        if (entity.getAccumulator()) {
            entity.setCeiling(BigDecimal.ZERO);
            entity.setCeilingExpressionEl("");
        }

        return super.saveOrUpdate(killConversation);
    }

    /**
     * Gets counter types depends if the counter is accumulator or not.
     *
     * @return counter types
     */
    public Object[] getCounterTypes() {
        List<Object> allCounterTypes = Arrays.asList(entity.getCounterType().getClass().getEnumConstants());
        if (entity.getAccumulator()) {
            allCounterTypes = allCounterTypes.stream().filter(counterType -> ((CounterTypeEnum) counterType).isAccumulator()).sorted(Comparator.comparing(Object::toString))
                    .collect(Collectors.toList());

        } else {
            allCounterTypes = allCounterTypes.stream().filter(counterType -> !((CounterTypeEnum) counterType).isAccumulator()).sorted(Comparator.comparing(Object::toString))
                    .collect(Collectors.toList());

        }

        return allCounterTypes.toArray();
    }

    /**
     * Update counterType and counter level.
     */
    public void updateCounterTypeAndCounterLevel() {
        if (entity.getId() == null && entity.getAccumulator()) {
            entity.setCounterType(CounterTypeEnum.USAGE);
            entity.setCounterLevel(CounterTemplateLevel.BA);
        }
        if (entity.getId() == null && !entity.getAccumulator()) {
            entity.setCounterType(CounterTypeEnum.USAGE);
            entity.setCounterLevel(CounterTemplateLevel.UA);
        }
    }

    /**
     * Gets an array of counter levels depends if the counter is an accumulator or not.
     *
     * @return an array of counter levels.
     */
    public Object[] getCounterLevels() {
        List<Object> counterLevels = Arrays.asList(entity.getCounterLevel().getClass().getEnumConstants());
        if (entity.getAccumulator() == null || !entity.getAccumulator()) {
            counterLevels = counterLevels.stream()
                    .filter(counterLevel -> counterLevel.equals(CounterTemplateLevel.SI) || counterLevel.equals(CounterTemplateLevel.SU) || counterLevel
                            .equals(CounterTemplateLevel.UA) || counterLevel.equals(CounterTemplateLevel.BA)).sorted(Comparator.comparing(Object::toString))
                    .collect(Collectors.toList());
        }

        return counterLevels.toArray();
    }

}