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
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.converter.Mapping;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DeconverterTest {

  private static final String DIRECT = "DIRECT";
  private static final String SKIP = "SKIP";
  private static final String UNKNOWN = "TO_SOMETHING_NEW";
  private static final String CODE = "code";
  private static final String NAME = "name";

  @Mock
  private ReverseTypeConverter directConverter;

  @Mock
  private ReverseTypeConverter skipConverter;

  private final Deconverter deconverter = new Deconverter();

  @Before
  public void setUp() {
    when(directConverter.supports(DIRECT)).thenReturn(true);
    when(skipConverter.supports(SKIP)).thenReturn(true);
    ReflectionTestUtils.setField(deconverter, "converters",
        asList(directConverter, skipConverter));
  }

  @Test
  public void shouldPassEachMappingToTheConverterThatSupportsIt() {
    JsonObject source = Json.createObjectBuilder().add(CODE, "F1").build();
    List<Mapping> mappings = asList(mapping(CODE, DIRECT), mapping(NAME, SKIP));

    deconverter.deconvert(source, mappings);

    verify(directConverter).deconvert(eq(source), eq(mappings.get(0)), any(Map.class));
    verify(skipConverter).deconvert(eq(source), eq(mappings.get(1)), any(Map.class));
  }

  @Test
  public void shouldUseOnlyTheFirstConverterThatSupportsAType() {
    when(skipConverter.supports(DIRECT)).thenReturn(true);

    deconverter.deconvert(Json.createObjectBuilder().build(), singletonList(mapping(CODE, DIRECT)));

    verify(directConverter).deconvert(any(JsonObject.class), any(Mapping.class), any(Map.class));
    verify(skipConverter, never())
        .deconvert(any(JsonObject.class), any(Mapping.class), any(Map.class));
  }

  @Test
  public void shouldSkipAMappingNoConverterSupportsRatherThanFail() {
    Map<String, String> row = deconverter
        .deconvert(Json.createObjectBuilder().build(), singletonList(mapping(CODE, UNKNOWN)));

    assertThat(row.isEmpty(), is(true));
  }

  @Test
  public void shouldSupportAllOnlyWhenEveryTypeHasAConverter() {
    assertThat(deconverter.supportsAll(asList(mapping(CODE, DIRECT), mapping(NAME, SKIP))),
        is(true));
    assertThat(deconverter.supportsAll(asList(mapping(CODE, DIRECT), mapping(NAME, UNKNOWN))),
        is(false));
  }

  @Test
  public void shouldReportUnsupportedTypesOnceInFirstSeenOrder() {
    List<Mapping> mappings = asList(mapping(CODE, UNKNOWN), mapping(NAME, "TO_OTHER"),
        mapping("extra", UNKNOWN), mapping("kept", DIRECT));

    assertThat(deconverter.unsupportedTypes(mappings), contains(UNKNOWN, "TO_OTHER"));
  }

  @Test
  public void shouldBuildTheHeaderFromDistinctNonBlankFromColumnsInMappingOrder() {
    List<Mapping> mappings = asList(mapping(NAME, DIRECT), mapping(CODE, DIRECT),
        mapping(NAME, SKIP), mapping("", DIRECT), mapping(null, DIRECT));

    assertThat(deconverter.getHeader(mappings), contains(NAME, CODE));
  }

  private Mapping mapping(String from, String type) {
    return new Mapping(from, from, type, "", "");
  }
}
