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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.upload.OrderableService;
import org.openlmis.upload.ProgramService;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@RunWith(MockitoJUnitRunner.class)
public class FindProgramOrderableTypeConverterTest {

  @Mock
  private ProgramService programService;

  @Mock
  private OrderableService orderableService;

  @Mock
  private JsonObject program;

  @Mock
  private JsonObject product;

  @Mock
  private JsonArray programOrderables;

  @Mock
  private JsonObject programOrderable;

  @InjectMocks
  private FindProgramOrderableTypeConverter converter;

  @Before
  public void setUp() throws Exception {
    String programId = UUID.randomUUID().toString();

    doReturn(programId).when(program).getString("id");

    doReturn(programOrderables).when(product).getJsonArray("programs");

    doReturn(1).when(programOrderables).size();
    doReturn(programOrderable).when(programOrderables).getJsonObject(0);

    doReturn(programId).when(programOrderable).getString("programId");

  }

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports("FIND_PROGRAM_ORDERABLE"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_PRODUCT_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT"), is(false));
    assertThat(converter.supports("TO_ARRAY_BY_NAME"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_NAME"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_PRODUCT_CODE"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    doReturn(program).when(programService).findBy("code", "em");
    doReturn(product).when(orderableService).findBy("productCode", "PR012345");

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("one", "two", "FIND_PROGRAM_ORDERABLE", "", "");
    String value = "[PR012345,em]";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    assertThat(object.getJsonObject(mapping.getTo()), is(programOrderable));
  }

  @Test
  public void shouldNotSetValueIfProgramOrderableCannotbeFound() throws Exception {
    doReturn(program).when(programService).findBy("code", "em");
    doReturn(product).when(orderableService).findBy("productCode", "PR012345");
    doReturn(0).when(programOrderables).size();

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("one", "two", "FIND_PROGRAM_ORDERABLE", "", "");
    String value = "[PR012345,em]";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

  @Test
  public void shouldNotSetValueIfProductCannotbeFound() throws Exception {
    doReturn(program).when(programService).findBy("code", "em");
    doReturn(null).when(orderableService).findBy("productCode", "PR012345");

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("one", "two", "FIND_PROGRAM_ORDERABLE", "", "");
    String value = "[PR012345,em]";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

  @Test
  public void shouldNotSetValueIfProgramCannotbeFound() throws Exception {
    doReturn(null).when(programService).findBy("code", "em");

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("one", "two", "FIND_PROGRAM_ORDERABLE", "", "");
    String value = "[PR012345,em]";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }
}
