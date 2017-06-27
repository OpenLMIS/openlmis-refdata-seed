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
        String type = level.getString("code");

        if ("district".equalsIgnoreCase(type)) {
          String zone = geographicZone.getString("code");
          String code = facility.getString("code");

          groupByZone.put(zone, code);
        }
      }

      if (groupByZone.keySet().size() > 1) {
        LOGGER.warn(
            "Found facilities in the same requisition group ({}) but different districts",
            requisitionGroup.getString("code")
        );
      }
    }

  }

}
