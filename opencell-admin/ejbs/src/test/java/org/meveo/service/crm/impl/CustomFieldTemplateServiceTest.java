/*
 * (C) Copyright 2015-2020 Opencell SAS (https://opencellsoft.com/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW. EXCEPT WHEN
 * OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM "AS
 * IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO
 * THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * For more information on the GNU Affero General Public License, please consult
 * <https://www.gnu.org/licenses/agpl-3.0.en.html>.
 */

package org.meveo.service.crm.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.custom.CustomTableCreatorService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;;

@RunWith(MockitoJUnitRunner.class)
public class CustomFieldTemplateServiceTest {

	private static final String COLUMN_NAME_1 = "CT_1";
	private static final String COLUMN_NAME_2 = "CT_2";
	private static final String CODE = "cet";
	private static final String NO_CONSTRAINT = "";

	@InjectMocks
	private CustomFieldTemplateService customFieldTemplateService;

	@Mock
	private CustomTableCreatorService customTableCreatorService;

	@Mock
	CustomEntityTemplate cet;

	@Before
	public void init() {
		when(cet.getDbTablename()).thenReturn(CODE);
	}

	@Test
	public void should_not_update_or_drop_the_same_constraint() {
		String oldConstraintColumns = COLUMN_NAME_1;
		String newConstraintColumns = COLUMN_NAME_1;
		// Given
		// When
		customFieldTemplateService.updateConstraintByColumnsName(cet, oldConstraintColumns, newConstraintColumns, false);
		// Then
		verify(customTableCreatorService, never()).addUniqueConstraint(anyString(), anyString());
		verify(customTableCreatorService, never()).dropUniqueConstraint(anyString(), anyString());
	}

	@Test
	public void should_drop_old_and_create_new_if_constraint_changed() {
		String oldConstraintColumns = COLUMN_NAME_1;
		String newConstraintColumns = COLUMN_NAME_2;
		// Given
		// When
		customFieldTemplateService.updateConstraintByColumnsName(cet, oldConstraintColumns, newConstraintColumns, false);
		// Then
		verify(customTableCreatorService, times(1)).addUniqueConstraint(CODE, COLUMN_NAME_2);
		verify(customTableCreatorService, times(1)).dropUniqueConstraint(CODE, COLUMN_NAME_1);
	}

	@Test
	public void should_only_drop_constraint_if_cancelled() {
		String oldConstraintColumns = COLUMN_NAME_1;
		String newConstraintColumns = NO_CONSTRAINT;
		// Given
		// When
		customFieldTemplateService.updateConstraintByColumnsName(cet, oldConstraintColumns, newConstraintColumns, false);
		// Then
		verify(customTableCreatorService, never()).addUniqueConstraint(anyString(), anyString());
		verify(customTableCreatorService, times(1)).dropUniqueConstraint(CODE, COLUMN_NAME_1);
	}

	@Test
	public void should_only_create_constraint_if_new() {
		String oldConstraintColumns = NO_CONSTRAINT;
		String newConstraintColumns = COLUMN_NAME_1;
		// Given
		// When
		customFieldTemplateService.updateConstraintByColumnsName(cet, oldConstraintColumns, newConstraintColumns, false);
		// Then
		verify(customTableCreatorService, never()).dropUniqueConstraint(anyString(), anyString());
		verify(customTableCreatorService, times(1)).addUniqueConstraint(CODE, COLUMN_NAME_1);
	}
}
