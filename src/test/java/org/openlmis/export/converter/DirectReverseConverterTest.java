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

public class DirectReverseConverterTest {

  private static final String DIRECT = "DIRECT";
  private static final String NAME = "name";
  private static final String ACTIVE = "active";

  private final DirectReverseConverter converter = new DirectReverseConverter();

  @Test
  public void shouldSupportDirectOnly() {
    assertThat(converter.supports(DIRECT), is(true));
    assertThat(converter.supports("DIRECT_DATE"), is(false));
    assertThat(converter.supports("TO_OBJECT"), is(false));
  }

  @Test
  public void shouldCopyValueDirectly() {
    final JsonObject source = Json.createObjectBuilder().add(NAME, "Depot").build();
    Mapping mapping = new Mapping(NAME, NAME, DIRECT, "", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get(NAME), is("Depot"));
  }

  @Test
  public void shouldWriteBooleansCapitalised() {
    final JsonObject source = Json.createObjectBuilder().add(ACTIVE, true).build();
    Mapping mapping = new Mapping(ACTIVE, ACTIVE, DIRECT, "", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get(ACTIVE), is("True"));
  }

  @Test
  public void shouldFallBackToBooleanPropertySpellingWhenIsPrefixedFieldIsAbsent() {
    // the API returns 'refDataFacility' while the mapping targets 'isRefDataFacility'
    final JsonObject source = Json.createObjectBuilder().add("refDataFacility", true).build();
    Mapping mapping = new Mapping("isRefDataFacility", "isRefDataFacility", DIRECT, "", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get("isRefDataFacility"), is("True"));
  }

  @Test
  public void shouldSkipWhenFieldAbsent() {
    Mapping mapping = new Mapping(NAME, NAME, DIRECT, "", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(Json.createObjectBuilder().build(), mapping, row);

    assertThat(row.isEmpty(), is(true));
  }
}
