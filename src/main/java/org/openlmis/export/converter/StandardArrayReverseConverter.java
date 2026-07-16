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

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.openlmis.converter.Mapping;
import org.springframework.stereotype.Component;

@Component
public class StandardArrayReverseConverter extends BaseReverseTypeConverter {

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_ARRAY_BY");
  }

  @Override
  public void deconvert(JsonObject source, Mapping mapping, Map<String, String> row) {
    String to = mapping.getTo();

    if (!source.containsKey(to) || source.isNull(to)) {
      return;
    }

    JsonValue value = source.get(to);
    if (value.getValueType() != JsonValue.ValueType.ARRAY) {
      logger.warn("Field {} is not an array ({}); skipping.", to, value.getValueType());
      return;
    }

    JsonArray array = (JsonArray) value;
    String by = getBy(mapping.getType());
    List<String> values = new ArrayList<>();

    for (JsonValue element : array) {
      if (element.getValueType() != JsonValue.ValueType.OBJECT) {
        logger.warn("Element of {} is not an object ({}); skipping element.",
            to, element.getValueType());
        continue;
      }

      JsonObject reference = (JsonObject) element;
      if (!reference.containsKey(by) || reference.isNull(by)) {
        // Expected, data-shaped condition: some arrays (e.g. SupervisoryNode.childNodes) come back
        // as id-only {id, href} references with no embedded code. The seed CSVs express that
        // hierarchy through parentNode instead and leave the column blank, so an id-only element is
        // skipped by design. Kept at debug to keep the log quiet.
        logger.debug(
            "Reference in {} has no '{}' field (id-only reference); skipping element. "
                + "Available fields: {}",
            to, by, reference.keySet()
        );
        continue;
      }

      values.add(getCsvString(reference.get(by)));
    }

    if (values.isEmpty()) {
      return;
    }

    row.put(mapping.getFrom(), "[" + String.join(",", values) + "]");
  }
}
