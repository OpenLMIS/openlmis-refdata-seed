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

    for (int i = 0, size = orderables.size(); i < size; ++i) {
      JsonObject orderable = orderables.getJsonObject(i);
      JsonArray programs = orderable.getJsonArray("programs");
      String name = orderable.getString("fullProductName");
      String code = orderable.getString("productCode");

      groupByName.put(name, code);

      if (programs.size() > 1) {
        multiplePrograms.add(code);
      }
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
