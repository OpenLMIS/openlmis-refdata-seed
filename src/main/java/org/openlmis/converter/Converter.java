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

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Converts Map representation of the CSV files into JSON files.
 */
@Component
public class Converter {
  private static final List<TypeConverter> CONVERTERS = Lists.newArrayList();

  static void addConverter(TypeConverter converter) {
    CONVERTERS.add(converter);
  }

  /**
   * Converts CSV map representation into JSON object representation.
   *
   * @param input    the CSV input as a map
   * @param mappings the mapping specifiations
   * @return JSON object representation to insert into OLMIS
   */
  public JsonObject convert(Map<String, String> input, List<Mapping> mappings) {
    JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

    for (Mapping mapping : mappings) {
      String type = mapping.getType();
      String value = getValue(input, mapping);

      TypeConverter converter = CONVERTERS
          .stream()
          .filter(element -> element.supports(type))
          .findFirst()
          .orElse(null);

      if (null != converter) {
        converter.convert(jsonBuilder, mapping, value);
      } else {
        throw new UnsupportedOperationException(mapping.getType());
      }
    }

    return jsonBuilder.build();
  }

  private String getValue(Map<String, String> input, Mapping mapping) {
    String value = StringUtils.strip(input.get(mapping.getFrom()));

    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
      value = value.toLowerCase(Locale.ENGLISH);
    }

    return value;
  }

}
