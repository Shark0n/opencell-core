package org.meveo.apiv2.generic;

import org.immutables.value.Value;
import org.meveo.apiv2.models.PaginatedResource;
import org.meveo.model.BaseEntity;

@Value.Immutable
@Value.Style(jdkOnly=true)
public interface GenericPaginatedResource extends PaginatedResource<BaseEntity> {
}
