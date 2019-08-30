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

package org.openlmis;

import org.openlmis.converter.Converter;
import org.openlmis.converter.Mapping;
import org.openlmis.converter.MappingConverter;
import org.openlmis.reader.GenericReader;
import org.openlmis.upload.BaseCommunicationService;
import org.openlmis.upload.Services;
import org.openlmis.utils.SourceFile;
import org.openlmis.utils.AppHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

@Component
public class DataSeeder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  private static final int DELAY_SECONDS = 240;

  @Autowired
  private Configuration configuration;

  @Autowired
  private GenericReader reader;

  @Autowired
  private Converter converter;

  @Autowired
  private MappingConverter mappingConverter;

  @Autowired
  private Services services;

  /**
   * Seeds data into OLMIS.
   */
  public void seedData() {
    Arrays
        .stream(SourceFile.values())
        .forEach(this::seedFor);
  }

  private void seedFor(SourceFile source) {
    String inputFileName = source.getFullFileName(configuration.getDirectory());
    String mappingFileName = source.getFullMappingFileName(configuration.getDirectory());

    LOGGER.info(" == Seeding {} == ", source.getName());
    LOGGER.info("Using input file: {}", inputFileName);
    LOGGER.info("Using mapping file: {}", mappingFileName);

    File inputFile = new File(inputFileName);
    File mappingFile = new File(mappingFileName);

    if (!AppHelper.shouldProcess(inputFile, mappingFile, source)) {
      return;
    }

    List<Mapping> mappings = mappingConverter.getMappingForFile(mappingFile);

    if (!AppHelper.shouldProcess(configuration, source, mappings)) {
      return;
    }

    List<Map<String, String>> csvs = reader.readFromFile(inputFile);

    BaseCommunicationService service = services.getService(source);
    service.before();

    boolean updateAllowed = !"false".equalsIgnoreCase(configuration.getUpdateAllowed());

    for (int i = 0, size = csvs.size(); i < size; ++i) {
      Map<String, String> csv = csvs.get(i);
      JsonObject jsonObject = converter.convert(csv, mappings);
      JsonObject existing = service.findUnique(jsonObject);

      LOGGER.info("{}/{}", i + 1, size);
      if (updateAllowed && existing != null) {
        if (!jsonObjectsEqual(jsonObject, existing, true)) {
          LOGGER.info("Resource exists. Attempting to update.");
          if (service.updateResource(jsonObject, existing.getString("id"))) {
            delay(source);
          }
        } else {
          LOGGER.info("Resource exists, but no update needed. Skipping.");
        }
      } else if (existing == null) {
        LOGGER.info("Creating new resource.");
        if (service.createResource(jsonObject.toString())) {
          delay(source);
        }
      } else {
        LOGGER.info("Resource exists but update has been disabled. Skipping.");
      }
      service.afterEach(jsonObject);
    }
  }

  private void delay(SourceFile source) {
    if (Arrays.asList(SourceFile.FACILITIES, SourceFile.REQUISITION_GROUP,
        SourceFile.SUPERVISORY_NODES, SourceFile.ROLES)
        .contains(source)) {
      LOGGER.info("Delaying execution by " + DELAY_SECONDS + "s");
      try {
        Thread.sleep(DELAY_SECONDS * 1000);
      } catch (InterruptedException exc) {
        LOGGER.info(exc.getMessage());
      }
    }
  }

  private Boolean jsonObjectsEqual(JsonObject newObject, JsonObject existingObject,
                                   Boolean enableLogging) {
    for (Map.Entry<String, JsonValue> entry : newObject.entrySet()) {
      JsonValue existingValue = existingObject.getOrDefault(entry.getKey(), JsonValue.NULL);
      if (!jsonValuesEqual(entry.getValue(), existingValue)) {
        if (enableLogging) {
          LOGGER.info(entry.getKey() + " has changed.");
          LOGGER.debug("Previous value: " + existingValue.toString());
          LOGGER.debug("New value: " + entry.getValue().toString());
        }
        return false;
      }
    }
    return true;
  }

  private Boolean jsonArraysEqual(JsonArray newArray, JsonArray existingArray) {
    for (JsonValue item : newArray) {
      Boolean itemExistsInBothArrays = false;
      for (JsonValue existingItem : existingArray) {
        if (jsonValuesEqual(item, existingItem)) {
          itemExistsInBothArrays = true;
          break;
        }
      }
      if (!itemExistsInBothArrays) {
        return false;
      }
    }
    return newArray.size() == existingArray.size();
  }

  private Boolean jsonValuesEqual(JsonValue newValue, JsonValue existingValue) {
    if (newValue.getValueType().equals(JsonValue.ValueType.ARRAY)
        && existingValue.getValueType().equals(JsonValue.ValueType.ARRAY)) {
      return jsonArraysEqual((JsonArray) newValue, (JsonArray) existingValue);
    } else if (newValue.getValueType().equals(JsonValue.ValueType.OBJECT)
        && existingValue.getValueType().equals(JsonValue.ValueType.OBJECT)) {
      return jsonObjectsEqual((JsonObject) newValue, (JsonObject) existingValue, false);
    }
    return newValue.toString().replace("\"", "").equals(
        existingValue.toString().replace("\"", ""));
  }
}
