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

import com.google.common.collect.Sets;

import org.openlmis.upload.RequisitionGroupService;
import org.openlmis.upload.SupervisoryNodeService;
import org.openlmis.upload.SupplyLineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;

@Component
public class SupervisoryNodeValidator implements Validator {
  private static final Logger LOGGER = LoggerFactory.getLogger(SupervisoryNodeValidator.class);

  @Autowired
  private SupervisoryNodeService supervisoryNodeService;

  @Autowired
  private SupplyLineService supplyLineService;

  @Autowired
  private RequisitionGroupService requisitionGroupService;

  @Override
  public void validate() {
    JsonArray supplyLines = supplyLineService.findAll();
    JsonArray requisitionGroups = requisitionGroupService.findAll();

    JsonArray supervisoryNodes = supervisoryNodeService.findAll();

    Set<String> forGroupsCode = Sets.newHashSet();
    Set<String> forSupplyCode = Sets.newHashSet();
    supervisoryNodes.forEach(supervisoryNode -> {
      JsonObject object = (JsonObject) supervisoryNode;
      String code = object.getString("code");

      forGroupsCode.add(code);

      if (!object.containsKey("parentNode") || object.isNull("parentNode")) {
        forSupplyCode.add(code);
      }
    });

    for (int j = 0, length = supplyLines.size(); j < length; ++j) {
      JsonObject supplyLine = supplyLines.getJsonObject(j);
      JsonObject supervisoryNode = supplyLine.getJsonObject("supervisoryNode");
      String code = supervisoryNode.getString("code");

      if (forSupplyCode.contains(code)) {
        forSupplyCode.remove(code);
      }
    }

    for (int j = 0, length = requisitionGroups.size(); j < length; ++j) {
      JsonObject requisitionGroup = requisitionGroups.getJsonObject(j);
      JsonObject supervisoryNode = requisitionGroup.getJsonObject("supervisoryNode");
      String code = supervisoryNode.getString("code");

      if (forGroupsCode.contains(code)) {
        forGroupsCode.remove(code);
      }
    }

    if (LOGGER.isWarnEnabled() && !forSupplyCode.isEmpty()) {
      LOGGER.warn(
          "Found Supervisory node without supply lines: {}",
          String.join(", ", forSupplyCode)
      );
    }

    if (LOGGER.isWarnEnabled() && !forGroupsCode.isEmpty()) {
      LOGGER.warn(
          "Found supervisory node without requisition group(s): {}",
          String.join(", ", forGroupsCode)
      );
    }
  }

}
