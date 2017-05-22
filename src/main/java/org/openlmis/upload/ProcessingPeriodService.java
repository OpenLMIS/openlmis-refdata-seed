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
import javax.json.JsonString;

@Service
public class ProcessingPeriodService extends BaseCommunicationService {

  private static final String PROCESSING_SCHEDULE = "processingSchedule";

  @Override
  protected String getUrl() {
    return "/api/processingPeriods";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    JsonString searchedName = object.getJsonString(NAME);
    JsonString searchedScheduleCode = object.getJsonObject(PROCESSING_SCHEDULE).getJsonString(CODE);

    JsonArray array = findAll();
    for (int i = 0; i < array.size(); i++) {
      JsonObject next = array.getJsonObject(i);
      JsonString foundName = next.getJsonString(NAME);
      JsonString foundScheduleCode = next.getJsonObject(PROCESSING_SCHEDULE).getJsonString(CODE);

      if (searchedName.equals(foundName) && searchedScheduleCode.equals(foundScheduleCode)) {
        return next;
      }
    }
    return null;
  }

}
