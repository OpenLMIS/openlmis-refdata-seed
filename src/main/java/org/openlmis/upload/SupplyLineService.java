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

import org.openlmis.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

@Service
public class SupplyLineService extends BaseCommunicationService {

  @Autowired
  private Configuration configuration;

  @Override
  protected String getUrl() {
    return "/api/supplyLines";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    JsonString searchedSnCode = object.getJsonObject("supervisoryNode").getJsonString(CODE);
    JsonString searchedProgramCode = object.getJsonObject("program").getJsonString(CODE);
    JsonString searchedFacilityCode = object.getJsonObject("supplyingFacility").getJsonString(CODE);

    JsonArray array = findAll();
    for (int i = 0; i < array.size(); i++) {
      JsonObject next = array.getJsonObject(i);
      JsonString foundSnCode = next.getJsonObject("supervisoryNode").getJsonString(CODE);
      JsonString foundProgramCode = next.getJsonObject("program").getJsonString(CODE);
      JsonString foundFacilityCode = next.getJsonObject("supplyingFacility").getJsonString(CODE);

      if (searchedSnCode.equals(foundSnCode)
          && searchedProgramCode.equals(foundProgramCode)
          && searchedFacilityCode.equals(foundFacilityCode)) {
        return next;
      }
    }
    return null;
  }

  @Override
  public void before() {
    if (configuration.isUpdateAllowed()) {
      logger.info("Removing all SupplyLines and preparing to re-create.");
      JsonArray supplyLines = findAll();
      for (int i = 0; i < supplyLines.size(); ++i) {
        JsonObject supplyLine = supplyLines.getJsonObject(i);
        deleteResource(supplyLine.getString(ID));
      }
    }
  }

}
