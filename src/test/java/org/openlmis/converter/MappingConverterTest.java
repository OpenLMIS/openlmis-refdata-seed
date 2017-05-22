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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;

public class MappingConverterTest {

  private MappingConverter converter = new MappingConverter();

  @Test
  public void shouldReadMappingFile() throws Exception {
    File file = ResourceUtils.getFile(getClass().getResource("/test_mapping.csv"));
    String filePath = file.getAbsolutePath();

    List<Mapping> mappings = converter.getMappingForFile(filePath);

    assertThat(mappings, Matchers.hasSize(3));

    Mapping one = mappings.get(0);
    assertThat(one.getFrom(), is(equalTo("code")));
    assertThat(one.getTo(), is(equalTo("code")));
    assertThat(one.getType(), is(equalTo("DIRECT")));
    assertThat(one.getEntityName(), is(nullValue()));
    assertThat(one.getDefaultValue(), is(nullValue()));


    Mapping two = mappings.get(1);
    assertThat(two.getFrom(), is(equalTo("facility")));
    assertThat(two.getTo(), is(equalTo("fac")));
    assertThat(two.getType(), is(equalTo("TO_OBJECT_BY_CODE")));
    assertThat(two.getEntityName(), is(equalTo("Facility")));
    assertThat(two.getDefaultValue(), is(nullValue()));

    Mapping three = mappings.get(2);
    assertThat(three.getFrom(), is(equalTo("displayOrder")));
    assertThat(three.getTo(), is(equalTo("order")));
    assertThat(three.getType(), is(equalTo("DIRECT")));
    assertThat(three.getEntityName(), is(nullValue()));
    assertThat(three.getDefaultValue(), is(equalTo("1")));
  }

  @Test
  public void shouldReturnEmptyListIfFileNotExist() throws Exception {
    assertThat(converter.getMappingForFile("abc"), hasSize(0));
  }
}
