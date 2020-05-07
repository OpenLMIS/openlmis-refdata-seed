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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.openlmis.utils.AppHelper;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFileArrayTypeConverterTest {
  private static final String CODE = "code";

  @Mock
  private Configuration configuration;

  @Mock
  private AppHelper appHelper;

  @Mock
  private Converter converter;

  @InjectMocks
  private DefaultFileArrayTypeConverter typeConverter;

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
    assertThat(typeConverter.supports("DEFAULT_TO_ARRAY_FROM_FILE_BY"), is(true));
    assertThat(typeConverter.supports("DEFAULT_TO_ARRAY_FROM_FILE_BY_CODE"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(typeConverter.supports("TO_ARRAY_FROM_FILE_BY"), is(false));
    assertThat(typeConverter.supports("TO_OBJECT"), is(false));
    assertThat(typeConverter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(typeConverter.supports("TO_ARRAY_BY_NAME"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(CODE, CODE, "DEFAULT_TO_ARRAY_FROM_FILE_BY_CODE",
        "", "[one,five]");

    typeConverter.convert(builder, mapping, null);

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
    Mapping mapping = new Mapping(CODE, CODE, "DEFAULT_TO_ARRAY_FROM_FILE_BY",
        "", "[six]");

    typeConverter.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));

    JsonArray array = object.getJsonArray(mapping.getTo());
    assertThat(array, hasSize(0));
  }
}
