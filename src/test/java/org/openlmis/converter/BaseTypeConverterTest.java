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

package org.openlmis.converter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseTypeConverterTest {

  @Spy
  private BaseTypeConverter converter;

  @Test
  public void shouldReturnNullIfThereIsNoBySection() throws Exception {
    assertThat(converter.getBy("ala"), is(nullValue()));
  }

  @Test
  public void shouldReturnCorrectValueForBySection() throws Exception {
    assertThat(converter.getBy("TO_ID_BY_NAME"), is(equalTo("name")));
    assertThat(converter.getBy("TO_ID_BY_PRODUCT_CODE"), is(equalTo("productCode")));
  }

  @Test
  public void shouldReturnEmptyListIfValueIsBlank() throws Exception {
    assertThat(converter.getArrayValues(null), hasSize(0));
    assertThat(converter.getArrayValues(""), hasSize(0));
    assertThat(converter.getArrayValues("    "), hasSize(0));
    assertThat(converter.getArrayValues("[]"), hasSize(0));
    assertThat(converter.getArrayValues(""), hasSize(0));
    assertThat(converter.getArrayValues("[    ]"), hasSize(0));
  }

  @Test
  public void shouldConvertSingleValueToArray() throws Exception {
    assertThat(converter.getArrayValues("ala"), hasItem("ala"));
  }

  @Test
  public void shouldConvertValueToArray() throws Exception {
    assertThat(converter.getArrayValues("[one,two]"), hasItems("one", "two"));
  }
}
