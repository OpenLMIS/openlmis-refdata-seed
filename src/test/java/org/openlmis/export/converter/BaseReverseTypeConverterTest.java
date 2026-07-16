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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;
import org.openlmis.converter.Mapping;

public class BaseReverseTypeConverterTest {

  private final BaseReverseTypeConverter converter = new BaseReverseTypeConverter() {
    @Override
    public boolean supports(String type) {
      return false;
    }

    @Override
    public void deconvert(JsonObject source, Mapping mapping, Map<String, String> row) {
      // only the shared helpers are under test here
    }
  };

  private final JsonObject sample = Json.createObjectBuilder()
      .add("text", "hello")
      .add("flagTrue", true)
      .add("flagFalse", false)
      .add("number", 42)
      .addNull("nothing")
      .build();

  @Test
  public void shouldReturnRawStringForStrings() {
    assertThat(converter.getCsvString(sample.get("text")), is("hello"));
  }

  @Test
  public void shouldCapitaliseBooleansToMatchSeedFormat() {
    assertThat(converter.getCsvString(sample.get("flagTrue")), is("True"));
    assertThat(converter.getCsvString(sample.get("flagFalse")), is("False"));
  }

  @Test
  public void shouldReturnNullForJsonNullAndJavaNull() {
    assertThat(converter.getCsvString(sample.get("nothing")), is(nullValue()));
    assertThat(converter.getCsvString(null), is(nullValue()));
  }

  @Test
  public void shouldStringifyNumbers() {
    assertThat(converter.getCsvString(sample.get("number")), is("42"));
  }

  @Test
  public void shouldDeriveLookupFieldFromType() {
    assertThat(converter.getBy("TO_ID_BY_CODE"), is("code"));
    assertThat(converter.getBy("TO_ID_BY_NAME"), is("name"));
    assertThat(converter.getBy("TO_ID_BY_PRODUCT_CODE"), is("productCode"));
    assertThat(converter.getBy("TO_ARRAY_FROM_FILE_BY_PROGRAM_CODE"), is("programCode"));
  }

  @Test
  public void shouldReturnNullByWhenTypeHasNoByClause() {
    assertThat(converter.getBy("DIRECT"), is(nullValue()));
  }
}
