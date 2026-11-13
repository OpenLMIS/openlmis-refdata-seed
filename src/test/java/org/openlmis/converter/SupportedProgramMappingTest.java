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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

public class SupportedProgramMappingTest {
  private static final String DIRECT = "DIRECT";

  private final List<Mapping> mappings = Lists.newArrayList(
      new Mapping("programCode", "code", DIRECT, "", ""),
      new Mapping("active", "supportActive", DIRECT, "", ""),
      new Mapping("startDate", "supportStartDate", "DIRECT_DATE", "", ""),
      new Mapping("locallyFulfilled", "supportLocallyFulfilled", DIRECT, "", "")
  );

  private final Converter converter = new Converter();

  @BeforeClass
  public static void registerConverters() {
    new DirectTypeConverter();
    new DirectDateTypeConverter();
  }

  @Test
  public void shouldMapStartDateAndLocallyFulfilled() {
    Map<String, String> row = ImmutableMap.of(
        "facilityCode", "HC02",
        "programCode", "HIV",
        "active", "TRUE",
        "startDate", "01/09/2026",
        "locallyFulfilled", "FALSE"
    );

    JsonObject supportedProgram = converter.convert(row, mappings);

    assertThat(supportedProgram.getString("code", null), is("HIV"));
    assertThat(supportedProgram.getString("supportActive", null), is("true"));
    assertThat(supportedProgram.getString("supportStartDate", null), is("2026-09-01"));
    assertThat(supportedProgram.getString("supportLocallyFulfilled", null), is("false"));
    assertThat(supportedProgram.size(), is(4));
  }
}
