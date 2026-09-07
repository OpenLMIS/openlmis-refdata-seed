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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ChildCsvCollector;
import org.openlmis.export.utils.OriginalCodeResolver;
import org.openlmis.utils.AppHelper;

@RunWith(MockitoJUnitRunner.class)
public class FileArrayReverseConverterTest {

  private static final String ROLE_ASSIGNMENTS = "roleAssignments";
  private static final String ROLE_NAME = "roleName";
  private static final String CODE = "code";
  private static final String FILE = "RoleAssignments.csv";
  private static final String STORE_MANAGER = "STORE_MANAGER";
  private static final String ARRAY_FROM_FILE_BY_CODE = "TO_ARRAY_FROM_FILE_BY_CODE";

  @Mock
  private Deconverter deconverter;

  @Mock
  private AppHelper appHelper;

  @Mock
  private ChildCsvCollector collector;

  @Mock
  private OriginalCodeResolver originalCodeResolver;

  @InjectMocks
  private FileArrayReverseConverter converter;

  @Test
  public void shouldSupportGenericFromFileArraysButNotProgramCode() {
    // BY_PROGRAM_CODE is delegated to SupportedProgramReverseConverter, so it is excluded here.
    assertThat(converter.supports(ARRAY_FROM_FILE_BY_CODE), is(true));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY"), is(true));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_PROGRAM_CODE"), is(false));
    assertThat(converter.supports("TO_ARRAY_BY_CODE"), is(false));
  }

  @Test
  public void shouldCollectChildRowsAndEmitTheJoinList() {
    final JsonObject source = Json.createObjectBuilder()
        .add(ROLE_ASSIGNMENTS, Json.createArrayBuilder()
            .add(Json.createObjectBuilder().add(ROLE_NAME, STORE_MANAGER)))
        .build();

    List<Mapping> childMappings = asList(
        new Mapping(CODE, "", "SKIP", "", ""),
        new Mapping(ROLE_NAME, "roleId", "TO_ID_BY_NAME", "Role", ""));
    when(appHelper.readMappings(FILE)).thenReturn(childMappings);
    when(deconverter.getHeader(childMappings)).thenReturn(asList(CODE, ROLE_NAME));

    Map<String, String> childRow = new LinkedHashMap<>();
    childRow.put(CODE, "RA1");
    childRow.put(ROLE_NAME, STORE_MANAGER);
    when(deconverter.deconvert(any(JsonObject.class), eq(childMappings))).thenReturn(childRow);

    Mapping mapping =
        new Mapping(ROLE_ASSIGNMENTS, ROLE_ASSIGNMENTS, ARRAY_FROM_FILE_BY_CODE, FILE, "");
    Map<String, String> parentRow = new LinkedHashMap<>();

    converter.deconvert(source, mapping, parentRow);

    verify(collector).add(eq(FILE), eq(asList(CODE, ROLE_NAME)), eq(childRow));
    assertThat(parentRow.get(ROLE_ASSIGNMENTS), is("[RA1]"));
  }

  @Test
  public void shouldUseTheOriginalCodeInsteadOfAGeneratedOne() {
    final JsonObject source = Json.createObjectBuilder()
        .add(ROLE_ASSIGNMENTS, Json.createArrayBuilder()
            .add(Json.createObjectBuilder().add(ROLE_NAME, STORE_MANAGER)))
        .build();

    List<Mapping> childMappings = asList(
        new Mapping(CODE, "", "SKIP", "", ""),
        new Mapping(ROLE_NAME, "roleId", "TO_ID_BY_NAME", "Role", ""));
    when(appHelper.readMappings(FILE)).thenReturn(childMappings);
    when(deconverter.getHeader(childMappings)).thenReturn(asList(CODE, ROLE_NAME));

    Map<String, String> childRow = new LinkedHashMap<>();
    childRow.put(ROLE_NAME, STORE_MANAGER);
    when(deconverter.deconvert(any(JsonObject.class), eq(childMappings))).thenReturn(childRow);
    when(originalCodeResolver.findOriginalCode(eq(FILE), eq(asList(CODE, ROLE_NAME)),
        eq(childRow), eq(CODE)))
        .thenReturn("RA_0001");

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, new Mapping(ROLE_ASSIGNMENTS, ROLE_ASSIGNMENTS,
        ARRAY_FROM_FILE_BY_CODE, FILE, ""), row);

    assertThat(childRow.get(CODE), is("RA_0001"));
    assertThat(row.get(ROLE_ASSIGNMENTS), is("[RA_0001]"));
  }

  @Test
  public void shouldGenerateACodeWhenTheRowHasNoOriginalCounterpart() {
    final JsonObject source = Json.createObjectBuilder()
        .add(ROLE_ASSIGNMENTS, Json.createArrayBuilder()
            .add(Json.createObjectBuilder().add(ROLE_NAME, STORE_MANAGER)))
        .build();

    List<Mapping> childMappings = asList(
        new Mapping(CODE, "", "SKIP", "", ""),
        new Mapping(ROLE_NAME, "roleId", "TO_ID_BY_NAME", "Role", ""));
    when(appHelper.readMappings(FILE)).thenReturn(childMappings);
    when(deconverter.getHeader(childMappings)).thenReturn(asList(CODE, ROLE_NAME));

    Map<String, String> childRow = new LinkedHashMap<>();
    childRow.put(ROLE_NAME, STORE_MANAGER);
    when(deconverter.deconvert(any(JsonObject.class), eq(childMappings))).thenReturn(childRow);
    when(originalCodeResolver.findOriginalCode(eq(FILE), eq(asList(CODE, ROLE_NAME)),
        eq(childRow), eq(CODE)))
        .thenReturn(null);

    Map<String, String> row = new LinkedHashMap<>();
    converter.deconvert(source, new Mapping(ROLE_ASSIGNMENTS, ROLE_ASSIGNMENTS,
        ARRAY_FROM_FILE_BY_CODE, FILE, ""), row);

    assertThat(childRow.get(CODE).startsWith("GEN_"), is(true));
  }
}
