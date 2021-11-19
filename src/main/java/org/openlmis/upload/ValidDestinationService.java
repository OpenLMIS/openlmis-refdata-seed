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

import org.springframework.stereotype.Service;

import javax.json.JsonArray;
import javax.json.JsonObject;

@Service
public class ValidDestinationService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/validDestinations";
  }

  @Override
  public void before() {
    invalidateCache();

    if (configuration.isUpdateAllowed()) {
      logger.info("Removing all ValidDestinations and preparing to re-create.");

      JsonArray validDestinations = findAll();
      for (int i = 0; i < validDestinations.size(); ++i) {
        JsonObject validDestination = validDestinations.getJsonObject(i);
        deleteResource(validDestination.getString(ID));
      }

      logger.info("Removed all ValidDestinations");
      invalidateCache();
    }
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return null;
  }

  @Override
  public JsonArray findAll() {
    RequestParameters parameters = RequestParameters.init()
            .set("page", 0)
            .set("size", 50000); // Implementation contains ~30k elements

    return findAll("", parameters);
  }
}