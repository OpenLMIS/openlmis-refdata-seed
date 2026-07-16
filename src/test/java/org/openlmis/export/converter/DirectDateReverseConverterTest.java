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

package org.openlmis.export.converter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;
import org.openlmis.converter.Mapping;

public class DirectDateReverseConverterTest {

  private static final String DIRECT_DATE = "DIRECT_DATE";
  private static final String GO_LIVE = "goLiveDate";

  private final DirectDateReverseConverter converter = new DirectDateReverseConverter();

  @Test
  public void shouldSupportDirectDateOnly() {
    assertThat(converter.supports(DIRECT_DATE), is(true));
    assertThat(converter.supports("DIRECT"), is(false));
  }

  @Test
  public void shouldCopyDateValue() {
    final JsonObject source = Json.createObjectBuilder().add(GO_LIVE, "2026-01-01").build();
    Mapping mapping = new Mapping(GO_LIVE, GO_LIVE, DIRECT_DATE, "", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get(GO_LIVE), is("2026-01-01"));
  }

  @Test
  public void shouldSkipWhenFieldAbsent() {
    Mapping mapping = new Mapping(GO_LIVE, GO_LIVE, DIRECT_DATE, "", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(Json.createObjectBuilder().build(), mapping, row);

    assertThat(row.isEmpty(), is(true));
  }
}
