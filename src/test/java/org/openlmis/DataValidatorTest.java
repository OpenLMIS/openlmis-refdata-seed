/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis;

import static com.google.common.collect.ImmutableMap.of;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.validation.Validator;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class DataValidatorTest {

  @Mock
  private ApplicationContext context;

  @Mock
  private Validator first;

  @Mock
  private Validator second;

  @InjectMocks
  private DataValidator dataValidator;

  @Test
  public void shouldRunEveryRegisteredValidator() {
    when(context.getBeansOfType(Validator.class)).thenReturn(of("a", first, "b", second));

    dataValidator.validate();

    verify(first).validate();
    verify(second).validate();
  }

  @Test
  public void shouldDoNothingWhenThereAreNoValidators() {
    when(context.getBeansOfType(Validator.class)).thenReturn(Collections.emptyMap());

    dataValidator.validate();
  }
}
