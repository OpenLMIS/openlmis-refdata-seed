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

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class DirectDateTypeConverterTest {
  private static final String GO_LIVE_DATE = "goLiveDate";
  private static final String TYPE = "DIRECT_DATE";
  private DirectDateTypeConverter converter = new DirectDateTypeConverter();

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports(TYPE), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(converter.supports("TO_OBJECT"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(false));
    assertThat(converter.supports("DIRECT"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    String expected = "2016-02-05";

    convert("2016-02-05", expected);
    convert("2016-2-05", expected);
    convert("2016-02-5", expected);
    convert("2016-2-5", expected);

    convert("16-02-05", expected);
    convert("16-2-05", expected);
    convert("16-02-5", expected);
    convert("16-2-5", expected);

    convert("05/02/2016", expected);
    convert("5/02/2016", expected);
    convert("5/02/2016", expected);
    convert("5/2/2016", expected);

    convert("05/02/16", expected);
    convert("05/2/16", expected);
    convert("5/02/16", expected);
    convert("5/2/16", expected);
  }

  @Test
  public void shouldSkipNullValue() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(GO_LIVE_DATE, GO_LIVE_DATE, TYPE, "", "");

    converter.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

  @Test
  public void shouldSkipIncorrectDateValue() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(GO_LIVE_DATE, GO_LIVE_DATE, TYPE, "", "");

    converter.convert(builder, mapping, "2018");

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

  private void convert(String value, String expected) {
    Mapping mapping = new Mapping(GO_LIVE_DATE, GO_LIVE_DATE, TYPE, "", "");
    JsonObjectBuilder builder = Json.createObjectBuilder();

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();

    assertThat(object.getString(mapping.getTo()), is(equalTo(expected)));
  }

}
