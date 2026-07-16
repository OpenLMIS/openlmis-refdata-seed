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

public class CreateObjectReverseConverterTest {

  private static final String TO_OBJECT = "TO_OBJECT";
  private static final String DISPENSABLE = "dispensable";

  private final CreateObjectReverseConverter converter = new CreateObjectReverseConverter();

  @Test
  public void shouldSupportToObjectOnly() {
    assertThat(converter.supports(TO_OBJECT), is(true));
    assertThat(converter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT_FROM_FILE_BY_EMAIL"), is(false));
  }

  @Test
  public void shouldSerialiseFlatObjectAsKeyValuePairs() {
    final JsonObject source = Json.createObjectBuilder()
        .add(DISPENSABLE, Json.createObjectBuilder()
            .add("dispensingUnit", "each")
            .add("displayUnit", "pack"))
        .build();
    Mapping mapping = new Mapping(DISPENSABLE, DISPENSABLE, TO_OBJECT, "", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get(DISPENSABLE), is("dispensingUnit:each,displayUnit:pack"));
  }

  @Test
  public void shouldSkipNestedEntriesThatCannotBeFlattened() {
    final JsonObject source = Json.createObjectBuilder()
        .add(DISPENSABLE, Json.createObjectBuilder()
            .add("dispensingUnit", "each")
            .add("nested", Json.createObjectBuilder().add("x", "y")))
        .build();
    Mapping mapping = new Mapping(DISPENSABLE, DISPENSABLE, TO_OBJECT, "", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get(DISPENSABLE), is("dispensingUnit:each"));
  }

  @Test
  public void shouldSkipWhenValueIsNotAnObject() {
    final JsonObject source = Json.createObjectBuilder().add(DISPENSABLE, "scalar").build();
    Mapping mapping = new Mapping(DISPENSABLE, DISPENSABLE, TO_OBJECT, "", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.isEmpty(), is(true));
  }
}
