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
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ReferenceResolver;

@RunWith(MockitoJUnitRunner.class)
public class IdReverseConverterTest {

  private static final String PROGRAM_ID = "programId";
  private static final String PROGRAM_CODE = "programCode";

  @Mock
  private ReferenceResolver resolver;

  @InjectMocks
  private IdReverseConverter converter;

  private final Mapping mapping =
      new Mapping(PROGRAM_CODE, PROGRAM_ID, "TO_ID_BY_CODE", "Program", "");

  @Test
  public void shouldSupportToIdTypesOnly() {
    assertThat(converter.supports("TO_ID_BY_CODE"), is(true));
    assertThat(converter.supports("TO_ID_BY_NAME"), is(true));
    assertThat(converter.supports("TO_ID_BY_PRODUCT_CODE"), is(true));
    assertThat(converter.supports("DIRECT"), is(false));
    assertThat(converter.supports("TO_ARRAY_BY_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT"), is(false));
  }

  @Test
  public void shouldResolveIdToConfiguredField() {
    final JsonObject source = Json.createObjectBuilder().add(PROGRAM_ID, "uuid-1").build();
    when(resolver.findById("Program", "uuid-1"))
        .thenReturn(Json.createObjectBuilder().add("code", "PRG001").build());

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get(PROGRAM_CODE), is("PRG001"));
  }

  @Test
  public void shouldLeaveBlankWhenReferenceNotFound() {
    final JsonObject source = Json.createObjectBuilder().add(PROGRAM_ID, "missing").build();
    when(resolver.findById("Program", "missing")).thenReturn(null);

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.containsKey(PROGRAM_CODE), is(false));
  }

  @Test
  public void shouldSkipWhenFieldAbsent() {
    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(Json.createObjectBuilder().build(), mapping, row);

    assertThat(row.isEmpty(), is(true));
  }

  @Test
  public void shouldSkipWhenValueIsNotAnIdString() {
    final JsonObject source = Json.createObjectBuilder().add(PROGRAM_ID, 5).build();

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.isEmpty(), is(true));
  }
}
