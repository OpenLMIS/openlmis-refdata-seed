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

public class StandardArrayReverseConverterTest {

  private static final String RIGHTS = "rights";
  private static final String CHILD_NODES = "childNodes";
  private static final String SUPERVISORY_NODE = "SupervisoryNode";

  private final StandardArrayReverseConverter converter = new StandardArrayReverseConverter();

  @Test
  public void shouldSupportToArrayByTypesOnly() {
    assertThat(converter.supports("TO_ARRAY_BY_CODE"), is(true));
    assertThat(converter.supports("TO_ARRAY_BY_NAME"), is(true));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ID_BY_CODE"), is(false));
  }

  @Test
  public void shouldEmitBracketedListOfCodes() {
    final JsonObject source = Json.createObjectBuilder()
        .add(RIGHTS, Json.createArrayBuilder()
            .add(Json.createObjectBuilder().add("name", "R1"))
            .add(Json.createObjectBuilder().add("name", "R2")))
        .build();
    Mapping mapping = new Mapping(RIGHTS, RIGHTS, "TO_ARRAY_BY_NAME", "Right", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get(RIGHTS), is("[R1,R2]"));
  }

  @Test
  public void shouldSkipIdOnlyReferencesThatLackTheLookupField() {
    final JsonObject source = Json.createObjectBuilder()
        .add(CHILD_NODES, Json.createArrayBuilder()
            .add(Json.createObjectBuilder().add("id", "x").add("href", "y")))
        .build();
    Mapping mapping =
        new Mapping(CHILD_NODES, CHILD_NODES, "TO_ARRAY_BY_CODE", SUPERVISORY_NODE, "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.containsKey(CHILD_NODES), is(false));
  }

  @Test
  public void shouldLeaveBlankForEmptyArray() {
    final JsonObject source = Json.createObjectBuilder()
        .add(CHILD_NODES, Json.createArrayBuilder())
        .build();
    Mapping mapping =
        new Mapping(CHILD_NODES, CHILD_NODES, "TO_ARRAY_BY_CODE", SUPERVISORY_NODE, "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.isEmpty(), is(true));
  }
}
