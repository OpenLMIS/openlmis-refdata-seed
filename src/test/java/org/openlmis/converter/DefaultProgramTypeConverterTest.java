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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.upload.ProgramService;

@RunWith(MockitoJUnitRunner.class)
public class DefaultProgramTypeConverterTest {

  private static final String USE_DEFAULT_PROGRAM = "USE_DEFAULT_PROGRAM";
  private static final String CODE = "code";
  private static final String VALUE = "COVID";
  private static final String PROGRAM = "program";

  @Mock
  private ProgramService programService;

  @InjectMocks
  private DefaultProgramTypeConverter converter = new DefaultProgramTypeConverter();

  @Mock
  private JsonObject program;


  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports(USE_DEFAULT_PROGRAM), is(true));
  }

  @Test
  public void shouldConvert() throws Exception {
    doReturn(program).when(programService).findBy(CODE, VALUE);

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(PROGRAM, PROGRAM, USE_DEFAULT_PROGRAM, "", VALUE);

    converter.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.getJsonObject(mapping.getTo()), is(program));
  }

  @Test
  public void shouldNotSetValueIfProgramCannotBeFound() {
    doReturn(null).when(programService).findBy(CODE, VALUE);

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(PROGRAM, PROGRAM, USE_DEFAULT_PROGRAM, "", VALUE);

    converter.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

  @Test
  public void shouldNotSetValueIfDefaultValueIsNull() {
    doReturn(null).when(programService).findBy(CODE, VALUE);

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(PROGRAM, PROGRAM, USE_DEFAULT_PROGRAM, "", "");

    converter.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

}
