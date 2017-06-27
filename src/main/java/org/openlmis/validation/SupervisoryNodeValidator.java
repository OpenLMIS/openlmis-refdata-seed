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
      forSupplyCode.add(code);
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

    if (LOGGER.isWarnEnabled() && !forGroupsCode.isEmpty()) {
      LOGGER.warn(
          "Found Supervisory node without supply lines: {}",
          String.join(", ", forGroupsCode)
      );
    }

    if (LOGGER.isWarnEnabled() && !forSupplyCode.isEmpty()) {
      LOGGER.warn(
          "Found supervisory node without requisition group(s): {}",
          String.join(", ", forSupplyCode)
      );
    }
  }

}
