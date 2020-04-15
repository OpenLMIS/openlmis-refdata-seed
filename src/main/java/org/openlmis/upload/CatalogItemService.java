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

package org.openlmis.upload;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class CatalogItemService extends BaseCommunicationService {

  private static final String EQUIPMENT_CODE = "equipmentCode";

  private static final String SEPARATOR = "||";

  private static final String MODEL = "model";

  @Override
  protected String getUrl() {
    return "/api/catalogItems";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByEquipmentCodeAndModel(
        getKey(object.getString(EQUIPMENT_CODE), object.getString(MODEL)));
  }

  /**
   * Finds the JSON representation of the resource by its key.
   *
   * @param key item's equipment code and model separated by '-'
   * @return JsonObject by its key
   */
  public JsonObject findByEquipmentCodeAndModel(String key) {
    JsonArray array = findAll();
    Map<String, JsonObject> items = Maps.newHashMap();

    for (int i = 0; i < array.size(); i++) {
      JsonObject item = array.getJsonObject(i);
      JsonString itemCode = item.getJsonString(EQUIPMENT_CODE);
      JsonString itemModel = item.getJsonString(MODEL);
      items.put(getKey(itemCode.toString(), itemModel.toString()), item);
    }

    return items.get(key);
  }

  private String getKey(String equipmentCode, String model) {
    return StringUtils.strip(equipmentCode, "\"")
        + SEPARATOR + StringUtils.strip(model, "\"");
  }
}
