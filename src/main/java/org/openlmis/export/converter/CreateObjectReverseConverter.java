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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.openlmis.converter.Mapping;
import org.springframework.stereotype.Component;

@Component
public class CreateObjectReverseConverter extends BaseReverseTypeConverter {

  @Override
  public boolean supports(String type) {
    return "TO_OBJECT".equalsIgnoreCase(type);
  }

  @Override
  public void deconvert(JsonObject source, Mapping mapping, Map<String, String> row) {
    String to = mapping.getTo();

    if (!source.containsKey(to) || source.isNull(to)) {
      return;
    }

    JsonValue value = source.get(to);
    if (value.getValueType() != JsonValue.ValueType.OBJECT) {
      logger.warn("Field {} is not an object ({}); skipping.", to, value.getValueType());
      return;
    }

    JsonObject object = (JsonObject) value;
    List<String> entries = new ArrayList<>();

    for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
      JsonValue.ValueType entryType = entry.getValue().getValueType();
      if (entryType == JsonValue.ValueType.OBJECT || entryType == JsonValue.ValueType.ARRAY) {
        logger.warn(
            "Field {} entry '{}' is a nested {} which cannot be represented as \"key:value\"; "
                + "skipping entry.",
            to, entry.getKey(), entryType
        );
        continue;
      }

      entries.add(entry.getKey() + ":" + getCsvString(entry.getValue()));
    }

    if (entries.isEmpty()) {
      return;
    }

    row.put(mapping.getFrom(), String.join(",", entries));
  }
}
