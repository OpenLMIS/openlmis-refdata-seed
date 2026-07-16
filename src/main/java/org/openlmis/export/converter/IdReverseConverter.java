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
import javax.json.JsonString;
import javax.json.JsonValue;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ReferenceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdReverseConverter extends BaseReverseTypeConverter {

  @Autowired
  private ReferenceResolver resolver;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_ID_BY");
  }

  @Override
  public void deconvert(JsonObject source, Mapping mapping, Map<String, String> row) {
    String to = mapping.getTo();

    if (!source.containsKey(to) || source.isNull(to)) {
      return;
    }

    JsonValue value = source.get(to);
    if (value.getValueType() != JsonValue.ValueType.STRING) {
      logger.warn("Field {} is not an id string ({}); skipping.", to, value.getValueType());
      return;
    }

    String id = ((JsonString) value).getString();
    String entityName = mapping.getEntityName();
    String by = getBy(mapping.getType());

    JsonObject reference = resolver.findById(entityName, id);
    if (reference == null) {
      // Not-found is an expected, data-shaped condition here: when the same id is reversed by more
      // than one mapping (e.g. the Node vs OrganizationNode duality) the inapplicable side always
      // misses and the row is left blank/skipped. A genuinely orphaned reference still surfaces as
      // a blanked cell in the exported CSV, so this stays at debug to keep the log quiet.
      logger.debug("No {} entity found with id {} (referenced by {}); left blank.",
          entityName, id, to);
      return;
    }

    if (!reference.containsKey(by) || reference.isNull(by)) {
      logger.warn("{} entity {} has no '{}' field; cannot reverse. Available fields: {}",
          entityName, id, by, reference.keySet());
      return;
    }

    row.put(mapping.getFrom(), getCsvString(reference.get(by)));
  }
}
