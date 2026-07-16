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

package org.openlmis.export.utils;

import java.util.HashMap;
import java.util.Map;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.openlmis.upload.BaseCommunicationService;
import org.openlmis.upload.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Resolves an entity's UUID back to its full JSON representation, so reverse converters can turn
 * id-only references (either bare {@code TO_ID_BY_*} ids or {@code {id, href}} stubs embedded by
 * some endpoints) into the human-readable {@code code}/{@code name} that appears in the seed CSVs.
 * Builds and caches an id-to-object index per entity on first use.
 */
@Component
public class ReferenceResolver {
  private static final String ID = "id";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private Services services;

  private final Map<String, Map<String, JsonObject>> indexes = new HashMap<>();

  /**
   * Finds the full JSON representation of an entity by its UUID.
   *
   * @param entityName the human-readable entity name (as used in the mapping's entityName column)
   * @param id         the UUID of the entity to resolve
   * @return the entity's JSON object, or null if no entity with that id exists
   */
  public JsonObject findById(String entityName, String id) {
    return indexes.computeIfAbsent(entityName, this::buildIndex).get(id);
  }

  private Map<String, JsonObject> buildIndex(String entityName) {
    BaseCommunicationService service = services.getService(entityName);
    Map<String, JsonObject> index = new HashMap<>();

    for (JsonValue value : service.findAll()) {
      if (value.getValueType() != JsonValue.ValueType.OBJECT) {
        continue;
      }

      JsonObject object = (JsonObject) value;
      if (object.containsKey(ID) && !object.isNull(ID)) {
        index.put(object.getString(ID), object);
      }
    }

    logger.info("Indexed {} {} entities by id for reference resolution.", index.size(), entityName);
    return index;
  }
}
