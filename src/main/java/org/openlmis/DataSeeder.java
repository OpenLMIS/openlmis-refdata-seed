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

import javax.json.JsonObject;

@Component
public class DataSeeder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  @Autowired
  private Configuration configuration;

  @Autowired
  private GenericReader reader;

  @Autowired
  private Converter converter;

  @Autowired
  private MappingConverter mappingConverter;

  @Autowired
  private AppHelper appHelper;

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

    if (!appHelper.inputAndMappingFileExist(inputFile, mappingFile, source)) {
      return;
    }

    List<Mapping> mappings = mappingConverter.getMappingForFile(mappingFile);

    if (!appHelper.shouldProcess(source, mappings)) {
      return;
    }

    List<Map<String, String>> csvs = reader.readFromFile(inputFile);

    BaseCommunicationService service = services.getService(source);
    service.before();

    boolean updateAllowed = configuration.isUpdateAllowed();

    for (int i = 0, size = csvs.size(); i < size; ++i) {
      Map<String, String> csv = csvs.get(i);
      JsonObject jsonObject = converter.convert(csv, mappings);
      JsonObject existing = service.findUnique(jsonObject);

      LOGGER.info("{}/{}", i + 1, size);
      if (updateAllowed && existing != null) {
        updateIfNeeded(service, jsonObject, existing);
      } else if (existing == null) {
        LOGGER.info("Creating new resource.");
        service.createResource(jsonObject.toString());
      } else {
        LOGGER.info("Resource exists but update has been disabled. Skipping.");
      }
      service.afterEach(jsonObject);
    }
  }

  private void updateIfNeeded(BaseCommunicationService service, JsonObject newObject,
      JsonObject existingObject) {
    if (service.isUpdateNeeded(newObject, existingObject)) {
      LOGGER.info("Resource exists. Attempting to update.");
      service.updateResource(newObject, existingObject.getString("id"));
    } else {
      LOGGER.info("Resource exists, but no update needed. Skipping.");
    }
  }
}
