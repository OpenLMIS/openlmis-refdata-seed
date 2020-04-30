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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.UUID;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.upload.BaseCommunicationService;
import org.openlmis.upload.Services;

@RunWith(MockitoJUnitRunner.class)
public class DefaultIdTypeConverterTest {

  @Mock
  private Services services;

  @Mock
  private BaseCommunicationService service;

  @Mock
  private JsonObject mockJson;

  @InjectMocks
  private DefaultIdTypeConverter converter;

  @Before
  public void setUp() throws Exception {
    doReturn(UUID.randomUUID().toString()).when(mockJson).getString("id");

    doReturn(mockJson).when(service).findBy("productCode", "PC187");
    doReturn(service).when(services).getService("Entity");
  }

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports("DEFAULT_TO_ID_BY"), is(true));
    assertThat(converter.supports("DEFAULT_TO_ID_BY_NAME"), is(true));
    assertThat(converter.supports("DEFAULT_TO_ID_BY_CODE"), is(true));
    assertThat(converter.supports("DEFAULT_TO_ID_BY_PRODUCT_CODE"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_PRODUCT_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT"), is(false));
    assertThat(converter.supports("TO_ARRAY_BY_NAME"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_NAME"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_PRODUCT_CODE"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping =
        new Mapping("productCode", "product", "DEFAULT_TO_ID_BY_PRODUCT_CODE", "Entity", "PC187");

    converter.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    assertThat(object.getString(mapping.getTo()), is(mockJson.getString("id")));
  }

  @Test
  public void shouldHandleNullObject() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping =
        new Mapping("productCode", "product", "DEFAULT_TO_ID_BY_PRODUCT_CODE", "Entity", "PC987");

    converter.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }
}
