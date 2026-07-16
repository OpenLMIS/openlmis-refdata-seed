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
public class FindObjectReverseConverterTest {

  private static final String CODE = "code";
  private static final String PROGRAM = "program";
  private static final String TYPE = "TO_OBJECT_BY_CODE";
  private static final String LEVEL = "level";
  private static final String UUID = "uuid-1";
  private static final String PROGRAM_ENTITY = "Program";

  @Mock
  private ReferenceResolver resolver;

  @InjectMocks
  private FindObjectReverseConverter converter;

  @Test
  public void shouldSupportToObjectByTypesOnly() {
    assertThat(converter.supports(TYPE), is(true));
    assertThat(converter.supports("TO_OBJECT_BY_NAME"), is(true));
    assertThat(converter.supports("TO_OBJECT"), is(false));
    assertThat(converter.supports("TO_OBJECT_FROM_FILE_BY_EMAIL"), is(false));
  }

  @Test
  public void shouldReadTheByFieldFromAnEmbeddedObject() {
    final JsonObject source = Json.createObjectBuilder()
        .add(LEVEL, Json.createObjectBuilder().add(CODE, "DISTRICT"))
        .build();
    Mapping mapping = new Mapping(LEVEL, LEVEL, TYPE, "GeographicLevel", "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get(LEVEL), is("DISTRICT"));
  }

  @Test
  public void shouldResolveIdOnlyStubViaTheResolver() {
    final JsonObject source = Json.createObjectBuilder()
        .add(PROGRAM, Json.createObjectBuilder().add("id", UUID).add("href", "http://x"))
        .build();
    when(resolver.findById(PROGRAM_ENTITY, UUID))
        .thenReturn(Json.createObjectBuilder().add(CODE, "PRG001").build());
    Mapping mapping = new Mapping(PROGRAM, PROGRAM, TYPE, PROGRAM_ENTITY, "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.get(PROGRAM), is("PRG001"));
  }

  @Test
  public void shouldLeaveBlankWhenIdOnlyStubCannotBeResolved() {
    final JsonObject source = Json.createObjectBuilder()
        .add(PROGRAM, Json.createObjectBuilder().add("id", UUID))
        .build();
    when(resolver.findById(PROGRAM_ENTITY, UUID)).thenReturn(null);
    Mapping mapping = new Mapping(PROGRAM, PROGRAM, TYPE, PROGRAM_ENTITY, "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.containsKey(PROGRAM), is(false));
  }

  @Test
  public void shouldSkipWhenValueIsNotAnObject() {
    final JsonObject source = Json.createObjectBuilder().add(PROGRAM, "scalar").build();
    Mapping mapping = new Mapping(PROGRAM, PROGRAM, TYPE, PROGRAM_ENTITY, "");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, mapping, row);

    assertThat(row.isEmpty(), is(true));
  }
}
