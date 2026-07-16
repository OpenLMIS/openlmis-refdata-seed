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
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ChildCsvCollector;
import org.openlmis.utils.AppHelper;

@RunWith(MockitoJUnitRunner.class)
public class SupportedProgramReverseConverterTest {

  private static final String FILE = "SupportedPrograms.csv";
  private static final String SUPPORTED_PROGRAMS = "supportedPrograms";
  private static final String PROGRAM_CODE = "programCode";
  private static final String ACTIVE = "active";
  private static final String CODE = "code";
  private static final String FACILITY = "ENTC001";

  @Mock
  private Deconverter deconverter;

  @Mock
  private AppHelper appHelper;

  @Mock
  private ChildCsvCollector collector;

  @InjectMocks
  private SupportedProgramReverseConverter converter;

  private final Mapping mapping = new Mapping(SUPPORTED_PROGRAMS, SUPPORTED_PROGRAMS,
      "TO_ARRAY_FROM_FILE_BY_PROGRAM_CODE", FILE, "");

  @Test
  public void shouldSupportOnlyTheProgramCodeFromFileType() {
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_PROGRAM_CODE"), is(true));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY"), is(false));
    assertThat(converter.supports("TO_ARRAY_BY_CODE"), is(false));
  }

  @Test
  public void shouldStampEachChildRowWithFacilityCodeAndEmitProgramList() {
    final JsonObject source = Json.createObjectBuilder()
        .add(CODE, FACILITY)
        .add(SUPPORTED_PROGRAMS, Json.createArrayBuilder()
            .add(Json.createObjectBuilder().add(CODE, "VAR").add("supportActive", true))
            .add(Json.createObjectBuilder().add(CODE, "AVS").add("supportActive", true)))
        .build();

    List<Mapping> childMappings = asList(
        new Mapping(PROGRAM_CODE, "id", "TO_ID_BY_CODE", "Program", ""),
        new Mapping(ACTIVE, "supportActive", "DIRECT", "", ""));
    when(appHelper.readMappings(FILE)).thenReturn(childMappings);
    when(deconverter.getHeader(childMappings)).thenReturn(asList(PROGRAM_CODE, ACTIVE));

    Map<String, String> row1 = new LinkedHashMap<>();
    row1.put(PROGRAM_CODE, "VAR");
    row1.put(ACTIVE, "True");
    Map<String, String> row2 = new LinkedHashMap<>();
    row2.put(PROGRAM_CODE, "AVS");
    row2.put(ACTIVE, "True");
    when(deconverter.deconvert(any(JsonObject.class), eq(childMappings)))
        .thenReturn(row1, row2);

    Map<String, String> parentRow = new LinkedHashMap<>();
    converter.deconvert(source, mapping, parentRow);

    assertThat(parentRow.get(SUPPORTED_PROGRAMS), is("[VAR,AVS]"));

    ArgumentCaptor<Map> rowCaptor = ArgumentCaptor.forClass(Map.class);
    ArgumentCaptor<List> headerCaptor = ArgumentCaptor.forClass(List.class);
    verify(collector, times(2)).add(eq(FILE), headerCaptor.capture(), rowCaptor.capture());

    assertThat(headerCaptor.getValue(), is(asList("facilityCode", PROGRAM_CODE, ACTIVE)));
    assertThat(rowCaptor.getAllValues().get(0).get("facilityCode"), is(FACILITY));
    assertThat(rowCaptor.getAllValues().get(1).get("facilityCode"), is(FACILITY));
  }

  @Test
  public void shouldDoNothingWhenFacilityHasNoSupportedPrograms() {
    final JsonObject source = Json.createObjectBuilder().add(CODE, FACILITY).build();

    Map<String, String> parentRow = new LinkedHashMap<>();
    converter.deconvert(source, mapping, parentRow);

    verify(collector, never()).add(anyString(), anyList(), anyMap());
    assertThat(parentRow.isEmpty(), is(true));
  }
}
