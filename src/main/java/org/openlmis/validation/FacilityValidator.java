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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import org.openlmis.upload.FacilityService;
import org.openlmis.upload.RequisitionGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;

@Component
public class FacilityValidator implements Validator {
  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityValidator.class);
  private static final String CODE = "code";

  @Autowired
  private FacilityService facilityService;

  @Autowired
  private RequisitionGroupService requisitionGroupService;

  @Override
  public void validate() {
    JsonArray facilities = facilityService.findAll();
    JsonArray requisitionGroups = requisitionGroupService.findAll();

    Set<String> facilityCodes = Sets.newHashSet();
    facilities.forEach(facility -> facilityCodes.add(((JsonObject) facility).getString(CODE)));

    for (int i = 0, size = requisitionGroups.size(); i < size; ++i) {
      JsonObject requisitionGroup = requisitionGroups.getJsonObject(i);
      JsonArray memberFacilities = requisitionGroup.getJsonArray("memberFacilities");

      for (int j = 0, length = memberFacilities.size(); j < length; ++j) {
        String code = memberFacilities.getJsonObject(j).getString(CODE);

        if (facilityCodes.contains(code)) {
          facilityCodes.remove(code);
        }
      }
    }

    Table<String, String, Integer> groupByDistrictAndName = group(facilities);

    if (LOGGER.isWarnEnabled() && !facilityCodes.isEmpty()) {
      LOGGER.warn(
          "Found facilities with no requisition group: {}",
          String.join(", ", facilityCodes)
      );
    }

    for (Table.Cell<String, String, Integer> cell : groupByDistrictAndName.cellSet()) {
      Integer count = Optional.ofNullable(cell.getValue()).orElse(0);

      if (LOGGER.isWarnEnabled() && count > 1) {
        LOGGER.warn(
            "Found facilities with the same name ({}) in the district: {}",
            cell.getColumnKey(), cell.getRowKey()
        );
      }
    }
  }

  private Table<String, String, Integer> group(JsonArray facilities) {
    Table<String, String, Integer> groupByDistrictAndName = HashBasedTable.create();
    for (int i = 0, size = facilities.size(); i < size; ++i) {
      JsonObject facility = facilities.getJsonObject(i);
      JsonObject geographicZone = facility.getJsonObject("geographicZone");
      JsonObject level = geographicZone.getJsonObject("level");
      String type = level.getString(CODE);

      if ("district".equalsIgnoreCase(type)) {
        String zone = geographicZone.getString(CODE);
        String name = facility.getString("name");
        Integer count = Optional.ofNullable(groupByDistrictAndName.get(zone, name)).orElse(0);

        groupByDistrictAndName.put(zone, name, count + 1);
      }
    }
    
    return groupByDistrictAndName;
  }

}
