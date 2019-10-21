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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.openlmis.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class FacilityTypeApprovedProductService extends BaseCommunicationService {

  @Autowired
  private OrderableService orderableService;

  @Autowired
  private FacilityTypeService facilityTypeService;

  @Autowired
  private Configuration configuration;

  private ArrayList<JsonObject> ftapList = new ArrayList<>();

  @Override
  protected String getUrl() {
    return "/api/facilityTypeApprovedProducts";
  }

  @Override
  public void before() {
    orderableService.invalidateCache();

    if (configuration.isUpdateAllowed()) {
      logger.info("Retrive all FacilityTypeApprovedProducts.");
      JsonArray types = facilityTypeService.findAll();
      for (int i = 0; i < types.size(); ++i) {
        JsonObject type = types.getJsonObject(i);
        String facilityTypeCode = type.getString(CODE);

        RequestParameters parameters = RequestParameters
            .init()
            .set("facilityType", facilityTypeCode);

        invalidateCache();
        JsonArray ftaps = findAll("", parameters);

        for (int j = 0; j < ftaps.size(); j++) {
          ftapList.add(ftaps.getJsonObject(j));
        }

        logger.info(
            "Retrieved {} FacilityTypeApprovedProducts for facility type {}",
            ftaps.size(), facilityTypeCode);
      }
    }
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    for (JsonObject ftap: ftapList) {
      if (Objects.equals(ftap.getJsonString("facilityTypeId"), object.getJsonString(
          "facilityTypeId"))
          && Objects.equals(ftap.getJsonString("programId"), object.getJsonString(
              "programId"))
          && Objects.equals(ftap.getJsonString("orderableId"), object.getJsonString(
              "orderableId"))) {
        logger.info(
            "Found {} FacilityTypeApprovedProduct",
            ftap.toString());
        return ftap;
      }
    }
    return null;
  }

  @Override
  public boolean updateResource(JsonObject jsonObject, String id) {
    JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

    jsonObject.forEach(jsonBuilder::add);
    jsonBuilder.add("meta",  Json.createObjectBuilder());

    return super.updateResource(jsonBuilder.build(), id, true);
  }

}
