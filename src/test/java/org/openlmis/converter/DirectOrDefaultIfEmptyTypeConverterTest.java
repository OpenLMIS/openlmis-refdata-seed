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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Test;

public class DirectOrDefaultIfEmptyTypeConverterTest {

  private static final String DESCRIPTION = "description";
  private static final String TYPE = "DIRECT_OR_DEFAULT_IF_EMPTY";
  private static final String DEFAULT_VAL = "DEFAULT_VAL";
  private final DirectOrDefaultIfEmptyTypeConverter con = new DirectOrDefaultIfEmptyTypeConverter();

  @Test
  public void shouldSupportTypes() {
    assertThat(con.supports(TYPE), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() {
    assertThat(con.supports("TO_OBJECT"), is(false));
    assertThat(con.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(con.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(false));
  }

  @Test
  public void shouldConvert() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(DESCRIPTION, DESCRIPTION, TYPE, "", "");
    String value = "short description";

    con.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.getString(mapping.getTo()), is(equalTo(value)));
  }

  @Test
  public void shouldUseDefaultInsteadOfNullValue() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(DESCRIPTION, DESCRIPTION, TYPE, "", DEFAULT_VAL);

    con.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    assertThat(object.getString(mapping.getTo()), is(equalTo(DEFAULT_VAL)));
  }

  @Test
  public void shouldSkipNullValueAndNullDefaultValue() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(DESCRIPTION, DESCRIPTION, TYPE, "", null);

    con.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

  @Test
  public void shouldUseDefaultInsteadOfEmptyValue() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(DESCRIPTION, DESCRIPTION, TYPE, "", DEFAULT_VAL);

    con.convert(builder, mapping, "");

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    assertThat(object.getString(mapping.getTo()), is(equalTo(DEFAULT_VAL)));
  }

  @Test
  public void shouldSkipEmptyValueAndEmptyDefaultValue() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(DESCRIPTION, DESCRIPTION, TYPE, "", "");

    con.convert(builder, mapping, "");

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

  @Test
  public void shouldNotSkipValueWithWhitespace() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    final String whitespace = " ";
    Mapping mapping = new Mapping(DESCRIPTION, DESCRIPTION, TYPE, "", null);

    con.convert(builder, mapping, whitespace);

    JsonObject object = builder.build();
    assertThat(object.getString(mapping.getTo()), is(equalTo(whitespace)));
  }

  @Test
  public void shouldNotSkipDefaultValueWithWhitespace() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    final String whitespace = " ";
    Mapping mapping = new Mapping(DESCRIPTION, DESCRIPTION, TYPE, "", whitespace);

    con.convert(builder, mapping, whitespace);

    JsonObject object = builder.build();
    assertThat(object.getString(mapping.getTo()), is(equalTo(whitespace)));
  }

  @Test
  public void shouldSetValueWhenBoth() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    final String value = "4.03";
    final String defaultValue = "0";

    Mapping mapping = new Mapping(DESCRIPTION, DESCRIPTION, TYPE, "", defaultValue);

    con.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    assertThat(object.getString(mapping.getTo()), is(equalTo(value)));
  }

}
