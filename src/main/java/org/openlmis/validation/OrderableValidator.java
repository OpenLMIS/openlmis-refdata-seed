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
import com.google.common.collect.Sets;

import org.openlmis.upload.OrderableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;

@Component
public class OrderableValidator implements Validator {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderableValidator.class);

  @Autowired
  private OrderableService orderableService;

  @Override
  public void validate() {
    JsonArray orderables = orderableService.findAll();

    Multimap<String, String> groupByName = HashMultimap.create();
    Set<String> multiplePrograms = Sets.newHashSet();
    Set<String> zeroPrograms = Sets.newHashSet();

    for (int i = 0, size = orderables.size(); i < size; ++i) {
      JsonObject orderable = orderables.getJsonObject(i);
      JsonArray programs = orderable.getJsonArray("programs");
      String name = orderable.getString("fullProductName");
      String code = orderable.getString("productCode");

      groupByName.put(name, code);

      if (programs == null || programs.isEmpty()) {
        zeroPrograms.add(code);
      }

      if (programs.size() > 1) {
        multiplePrograms.add(code);
      }
    }

    logWarnings(groupByName, multiplePrograms, zeroPrograms);
  }

  private void logWarnings(Multimap<String, String> groupByName, Set<String> multiplePrograms,
                           Set<String> zeroPrograms) {
    if (LOGGER.isWarnEnabled() && !zeroPrograms.isEmpty()) {
      LOGGER.warn(
          "Found products not included in any program: {}",
          String.join(", ", zeroPrograms)
      );
    }

    if (LOGGER.isWarnEnabled() && !multiplePrograms.isEmpty()) {
      LOGGER.warn(
          "Found products included on multiple programs: {}",
          String.join(", ", multiplePrograms)
      );
    }

    for (Map.Entry<String, Collection<String>> entry : groupByName.asMap().entrySet()) {
      if (LOGGER.isWarnEnabled() && entry.getValue().size() > 1) {
        LOGGER.warn(
            "Found products with the same name ({}): {}",
            entry.getKey(), String.join(", ", entry.getValue())
        );
      }
    }
  }

}
