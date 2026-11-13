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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.openlmis.utils.AppHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class FileArrayTypeConverterTest {
  private static final String CODE = "code";
  private static final String PROGRAM_CODE = "programCode";
  private static final String FACILITY_CODE = "facilityCode";
  private static final String FACILITY_KEY = "facilityCode:code";
  private static final String SUPPORTED_PROGRAMS = "supportedPrograms";
  private static final String HIV = "HIV";
  private static final String TB = "TB";
  private static final String F1 = "F1";
  private static final String TB_AT_F1 = "TB/F1";
  
  @Mock
  private Configuration configuration;

  @Mock
  private AppHelper appHelper;

  @Mock
  private Converter converter;

  @InjectMocks
  private FileArrayTypeConverter typeConverter;

  @Before
  public void setUp() throws Exception {
    List<Map<String, String>> innerData = Lists.newArrayList(
        ImmutableMap.of(CODE, "one"),
        ImmutableMap.of(CODE, "two"),
        ImmutableMap.of(CODE, "three"),
        ImmutableMap.of(CODE, "four"),
        ImmutableMap.of(CODE, "five")
    );

    List<Mapping> innerMapping = Lists.newArrayList(
        new Mapping(CODE, CODE, "DIRECT", "", "")
    );

    doReturn("").when(configuration).getDirectory();
    doReturn(innerData).when(appHelper).readCsv(anyString());
    doReturn(innerMapping).when(appHelper).readMappings(anyString());
    doAnswer(invocation -> {
      JsonObjectBuilder inner = Json.createObjectBuilder();
      Mapping mapping = innerMapping.get(0);
      inner.add(
          mapping.getTo(),
          invocation.getArgumentAt(0, Map.class).get(mapping.getFrom()).toString()
      );

      return inner.build();
    }).when(converter).convert(anyMap(), anyList());
  }

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(typeConverter.supports("TO_ARRAY_FROM_FILE_BY"), is(true));
    assertThat(typeConverter.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(true));
    assertThat(typeConverter.supports("TO_ARRAY_FROM_FILE_BY_CODE"), is(true));
    assertThat(typeConverter.supports("TO_ARRAY_FROM_FILE_BY_PRODUCT_CODE"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(typeConverter.supports("TO_OBJECT"), is(false));
    assertThat(typeConverter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(typeConverter.supports("TO_ARRAY_BY_NAME"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(CODE, CODE, "TO_ARRAY_FROM_FILE_BY_CODE", "inner.csv", "");
    String value = "[one,five]";

    typeConverter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));

    JsonArray array = object.getJsonArray(mapping.getTo());

    assertThat(array, hasSize(2));
    assertThat(array.getJsonObject(0).getString(CODE), is(equalTo("one")));
    assertThat(array.getJsonObject(1).getString(CODE), is(equalTo("five")));
  }

  @Test
  public void shouldCreateEmptyListIfThereIsNoMatchingItems() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(CODE, CODE, "TO_ARRAY_FROM_FILE_BY_CODE", "inner.csv", "");
    String value = "[six]";

    typeConverter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));

    JsonArray array = object.getJsonArray(mapping.getTo());
    assertThat(array, hasSize(0));
  }

  @Test
  public void shouldFilterChildRowsByParentKey() {
    givenChildRows(Lists.newArrayList(
        childRow(HIV, F1), childRow(HIV, "F2"), childRow(TB, F1)));

    List<String> converted = convertSupportedPrograms(FACILITY_KEY);

    assertThat(converted, contains("HIV/F1", TB_AT_F1));
  }

  @Test
  public void shouldKeepAllMatchingRowsWithoutParentKey() {
    givenChildRows(Lists.newArrayList(
        childRow(HIV, F1), childRow(HIV, "F2"), childRow(TB, F1)));

    List<String> converted = convertSupportedPrograms("");

    assertThat(converted, contains("HIV/F1", "HIV/F2", TB_AT_F1));
  }

  @Test
  public void shouldSkipChildRowsWithBlankParentKey() {
    givenChildRows(Lists.newArrayList(
        childRow(HIV, ""), childRow(TB, F1)));

    List<String> converted = convertSupportedPrograms(FACILITY_KEY);

    assertThat(converted, contains(TB_AT_F1));
  }

  @Test
  public void shouldReturnNoRowsWhenParentKeyColumnMissing() {
    givenChildRows(Lists.newArrayList(
        ImmutableMap.of(PROGRAM_CODE, HIV), ImmutableMap.of(PROGRAM_CODE, TB)));

    List<String> converted = convertSupportedPrograms(FACILITY_KEY);

    assertThat(converted, hasSize(0));
  }

  @Test
  public void shouldReturnNoRowsForMalformedParentKey() {
    givenChildRows(Lists.newArrayList(childRow(HIV, F1), childRow(TB, F1)));

    List<String> converted = convertSupportedPrograms("facilityCode:");

    assertThat(converted, hasSize(0));
  }

  @Test
  public void shouldMatchParentKeyIgnoringSurroundingSpaces() {
    givenChildRows(Lists.newArrayList(childRow(HIV, " F1"), childRow(TB, "F2")));

    List<String> converted = convertSupportedPrograms(FACILITY_KEY, "F1 ");

    assertThat(converted, contains("HIV/ F1"));
  }

  @Test
  public void shouldIgnoreDefaultValueWithoutColon() {
    givenChildRows(Lists.newArrayList(
        childRow(HIV, F1), childRow(HIV, "F2"), childRow(TB, F1)));

    List<String> converted = convertSupportedPrograms(FACILITY_CODE);

    assertThat(converted, contains("HIV/F1", "HIV/F2", TB_AT_F1));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldCountJoinColumnsAsMapped() {
    givenChildRows(Lists.newArrayList(childRow(HIV, F1)));
    ArgumentCaptor<List> mappings = ArgumentCaptor.forClass(List.class);

    convertSupportedPrograms(FACILITY_KEY);

    verify(appHelper).warnUnmappedColumns(anyString(), anyList(), mappings.capture());
    List<String> mappedColumns = ((List<Mapping>) mappings.getValue()).stream()
        .map(Mapping::getFrom)
        .collect(Collectors.toList());
    assertThat(mappedColumns, hasItems(CODE, PROGRAM_CODE, FACILITY_CODE));
  }

  private Map<String, String> childRow(String programCode, String facilityCode) {
    return ImmutableMap.of(PROGRAM_CODE, programCode, FACILITY_CODE, facilityCode);
  }

  private void givenChildRows(List<Map<String, String>> rows) {
    doReturn(rows).when(appHelper).readCsv(anyString());
    doAnswer(invocation -> {
      Map<String, String> row = invocation.getArgumentAt(0, Map.class);
      JsonObjectBuilder child = Json.createObjectBuilder();
      row.forEach(child::add);
      return child.build();
    }).when(converter).convert(anyMap(), anyList());
  }

  private List<String> convertSupportedPrograms(String parentKey) {
    return convertSupportedPrograms(parentKey, F1);
  }

  private List<String> convertSupportedPrograms(String parentKey, String parentCode) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(SUPPORTED_PROGRAMS, SUPPORTED_PROGRAMS,
        "TO_ARRAY_FROM_FILE_BY_PROGRAM_CODE", "SupportedPrograms.csv", parentKey);

    typeConverter.convert(builder, mapping, "[HIV,TB]", ImmutableMap.of(CODE, parentCode));

    return builder.build().getJsonArray(SUPPORTED_PROGRAMS)
        .getValuesAs(JsonObject.class)
        .stream()
        .map(child -> child.getString(PROGRAM_CODE) + "/" + child.getString(FACILITY_CODE, ""))
        .collect(Collectors.toList());
  }

}
