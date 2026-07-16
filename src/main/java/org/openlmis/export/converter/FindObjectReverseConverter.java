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

import java.util.Map;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ReferenceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindObjectReverseConverter extends BaseReverseTypeConverter {

  private static final String ID = "id";

  @Autowired
  private ReferenceResolver resolver;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_OBJECT_BY");
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

    JsonObject reference = (JsonObject) value;
    String by = getBy(mapping.getType());
    String result = extractBy(reference, by, mapping);

    if (result != null) {
      row.put(mapping.getFrom(), result);
    }
  }

  private String extractBy(JsonObject reference, String by, Mapping mapping) {
    if (reference.containsKey(by) && !reference.isNull(by)) {
      return getCsvString(reference.get(by));
    }

    // The endpoint embedded an id-only stub ({id, href}) instead of the full object; resolve the
    // id back to the referenced entity to recover its 'by' value.
    if (reference.containsKey(ID) && !reference.isNull(ID)) {
      return resolveById(reference.getString(ID), by, mapping);
    }

    logger.warn(
        "Reference at {} has no '{}' field and no 'id' to resolve; cannot reverse. "
            + "Available fields: {}",
        mapping.getTo(), by, reference.keySet()
    );
    return null;
  }

  private String resolveById(String id, String by, Mapping mapping) {
    JsonObject resolved = resolver.findById(mapping.getEntityName(), id);

    if (resolved != null && resolved.containsKey(by) && !resolved.isNull(by)) {
      return getCsvString(resolved.get(by));
    }

    logger.warn("Id-only reference at {} (id={}) could not be resolved to '{}' via {}.",
        mapping.getTo(), id, by, mapping.getEntityName());
    return null;
  }
}
