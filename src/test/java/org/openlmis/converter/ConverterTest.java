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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ConverterTest {
  private static final String INPUT_ROW_TYPE = "TEST_INPUT_ROW";

  @Test
  public void shouldPassInputRowToTypeConverter() {
    Converter.addConverter(new TypeConverter() {
      @Override
      public boolean supports(String type) {
        return INPUT_ROW_TYPE.equals(type);
      }

      @Override
      public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
        builder.add(mapping.getTo(), "without input row");
      }

      @Override
      public void convert(JsonObjectBuilder builder, Mapping mapping, String value,
          Map<String, String> inputRow) {
        builder.add(mapping.getTo(), inputRow.get("code"));
      }
    });

    JsonObject object = new Converter().convert(
        ImmutableMap.of("code", "F1", "name", "Facility one"),
        Lists.newArrayList(new Mapping("name", "parentCode", INPUT_ROW_TYPE, "", "")));

    assertThat(object.getString("parentCode"), is("F1"));
  }
}
