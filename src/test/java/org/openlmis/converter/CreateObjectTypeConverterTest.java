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

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class CreateObjectTypeConverterTest {
  private CreateObjectTypeConverter converter = new CreateObjectTypeConverter();

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports("TO_OBJECT"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(converter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("code", "code", "TO_OBJECT", "", "");
    String value = "key:1,key2:2,type:R";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));

    object = object.getJsonObject(mapping.getTo());

    assertThat(object.getString("key"), is("1"));
    assertThat(object.getString("key2"), is("2"));
    assertThat(object.getString("type"), is("R"));
  }

  @Test
  public void shouldSkipIncorrectValues() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("code", "code", "TO_OBJECT", "", "");
    String value = "key:1,key2,type:R";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));

    object = object.getJsonObject(mapping.getTo());

    assertThat(object.containsKey("key2"), is(false));
    assertThat(object.getString("key"), is("1"));
    assertThat(object.getString("type"), is("R"));
  }
}
