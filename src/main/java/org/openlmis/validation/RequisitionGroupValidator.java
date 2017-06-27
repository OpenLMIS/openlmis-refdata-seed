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

package org.openlmis.validation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.openlmis.upload.RequisitionGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.json.JsonArray;
import javax.json.JsonObject;

@Component
public class RequisitionGroupValidator implements Validator {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequisitionGroupValidator.class);
  private static final String CODE = "code";

  @Autowired
  private RequisitionGroupService requisitionGroupService;

  @Override
  public void validate() {
    JsonArray requisitionGroups = requisitionGroupService.findAll();

    for (int i = 0, size = requisitionGroups.size(); i < size; ++i) {
      JsonObject requisitionGroup = requisitionGroups.getJsonObject(i);
      JsonArray memberFacilities = requisitionGroup.getJsonArray("memberFacilities");

      Multimap<String, String> groupByZone = HashMultimap.create();
      for (int j = 0, length = memberFacilities.size(); j < length; ++j) {
        JsonObject facility = memberFacilities.getJsonObject(j);
        JsonObject geographicZone = facility.getJsonObject("geographicZone");
        JsonObject level = geographicZone.getJsonObject("level");
        String type = level.getString(CODE);

        if ("district".equalsIgnoreCase(type)) {
          String zone = geographicZone.getString(CODE);
          String code = facility.getString(CODE);

          groupByZone.put(zone, code);
        }
      }

      if (groupByZone.keySet().size() > 1) {
        LOGGER.warn(
            "Found facilities in the same requisition group ({}) but different districts",
            requisitionGroup.getString(CODE)
        );
      }
    }

  }

}
