package org.meveo.apiv2.services.generic.filter.filtermapper;


import org.apache.commons.lang.reflect.FieldUtils;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.apiv2.services.generic.filter.FactoryFilterMapper;
import org.meveo.apiv2.services.generic.filter.FilterMapper;
import org.meveo.model.IEntity;
import org.meveo.service.base.PersistenceService;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class ObjectMapper extends FilterMapper {
    private final Class<?> type;
    private final Function<Class, PersistenceService> serviceFunction;

    public ObjectMapper(String property, Object value, Class<?> type, Function<Class, PersistenceService> serviceFunction) {
        super(property, value);
        this.type = type;
        this.serviceFunction = serviceFunction;
    }

    @Override
    public Object mapStrategy(Object value) {
        Object target = null;
        try {
            if(value instanceof Map && !((Map) value).containsKey("id")){
                final Object targetInstanceHolder = type.newInstance();
                Map<String, Object> innerValue = (Map) value;
                innerValue.keySet()
                        .stream()
                        .map(key -> Collections.singletonMap(key, new FactoryFilterMapper().create(key.replaceFirst("[a-zA-Z]* ",""), innerValue.get(key), type, serviceFunction).map()))
                        .flatMap(entries -> entries.entrySet().stream())
                        .forEach(entry -> {
                            try {
                                FieldUtils.writeField(targetInstanceHolder, entry.getKey().replaceFirst("[a-zA-Z]* ",""), entry.getValue(), true);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
                target = targetInstanceHolder;

                if(target instanceof IEntity && ((IEntity) target).isTransient()){
                    target = serviceFunction.apply(target.getClass()).list(new PaginationConfiguration((Map) value));
                }
            } else{
                target = new FactoryFilterMapper().create("id", value, type, serviceFunction).map();
            }

        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return target;
    }
}
