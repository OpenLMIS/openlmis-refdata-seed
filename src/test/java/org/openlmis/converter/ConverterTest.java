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

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonObjectBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConverterTest {

  private static final String DIRECT = "DIRECT";
  private static final String UNKNOWN = "TO_SOMETHING_NEW";
  private static final String CODE = "code";

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Mock
  private TypeConverter typeConverter;

  private final Converter converter = new Converter();
  private List<TypeConverter> registered;
  private List<TypeConverter> original;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    registered = (List<TypeConverter>) ReflectionTestUtils.getField(Converter.class, "CONVERTERS");
    original = new ArrayList<>(registered);
    registered.clear();
    registered.add(typeConverter);
    when(typeConverter.supports(DIRECT)).thenReturn(true);
  }

  @After
  public void tearDown() {
    registered.clear();
    registered.addAll(original);
  }

  @Test
  public void shouldPassTheColumnValueToTheConverterThatSupportsTheType() {
    convert(row(CODE, "F1"), mapping(CODE, DIRECT));

    verify(typeConverter).convert(any(JsonObjectBuilder.class), any(Mapping.class), eq("F1"));
  }

  @Test
  public void shouldStripSurroundingWhitespaceFromTheValue() {
    convert(row(CODE, "  F1  "), mapping(CODE, DIRECT));

    verify(typeConverter).convert(any(JsonObjectBuilder.class), any(Mapping.class), eq("F1"));
  }

  @Test
  public void shouldLowerCaseBooleanValuesSoTheyAreValidJson() {
    convert(row(CODE, "True"), mapping(CODE, DIRECT));
    convert(row(CODE, "FALSE"), mapping(CODE, DIRECT));

    verify(typeConverter).convert(any(JsonObjectBuilder.class), any(Mapping.class), eq("true"));
    verify(typeConverter).convert(any(JsonObjectBuilder.class), any(Mapping.class), eq("false"));
  }

  @Test
  public void shouldLeaveNonBooleanValuesAsTheyAre() {
    convert(row(CODE, "Truest"), mapping(CODE, DIRECT));

    verify(typeConverter).convert(any(JsonObjectBuilder.class), any(Mapping.class), eq("Truest"));
  }

  @Test
  public void shouldPassNullWhenTheRowHasNoSuchColumn() {
    convert(new LinkedHashMap<>(), mapping(CODE, DIRECT));

    verify(typeConverter)
        .convert(any(JsonObjectBuilder.class), any(Mapping.class), eq((String) null));
  }

  @Test
  public void shouldFailOnAMappingTypeNoConverterSupports() {
    expected.expect(UnsupportedOperationException.class);
    expected.expectMessage(UNKNOWN);

    convert(row(CODE, "F1"), mapping(CODE, UNKNOWN));
  }

  private void convert(Map<String, String> row, Mapping mapping) {
    converter.convert(row, singletonList(mapping));
  }

  private Mapping mapping(String from, String type) {
    return new Mapping(from, from, type, "", "");
  }

  private Map<String, String> row(String column, String value) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(column, value);
    return row;
  }
}
