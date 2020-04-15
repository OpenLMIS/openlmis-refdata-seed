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

import java.util.Optional;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.openlmis.upload.CatalogItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindCatalogItemToObjectTypeConverter extends BaseTypeConverter {

  @Autowired
  private CatalogItemService service;

  @Override
  public boolean supports(String type) {
    return "FIND_CATALOG_ITEM_TO_OBJECT".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    findItem(value).ifPresent(item -> builder.add(mapping.getTo(), item));
  }

  private Optional<JsonObject> findItem(String key) {
    JsonObject item = service.findByEquipmentCodeAndModel(key);

    if (item == null) {
      logger.warn("Can not find catalog item with equipment code and model {}", key);
      return Optional.empty();
    }

    String itemId = item.getString("id");
    logger.debug("Found catalog item with id: {}", itemId);

    return Optional.of(item);
  }

}
