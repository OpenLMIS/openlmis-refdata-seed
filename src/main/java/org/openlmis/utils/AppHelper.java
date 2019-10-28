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

package org.openlmis.utils;

import static org.apache.commons.lang3.StringUtils.INDEX_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.indexOfIgnoreCase;

import com.google.common.collect.Maps;

import org.openlmis.Configuration;
import org.openlmis.converter.Mapping;
import org.openlmis.converter.MappingConverter;
import org.openlmis.reader.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AppHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppHelper.class);
  private static final String CAMEL_CASE_PATTERN = "([a-z]+[a-zA-Z0-9]+)+";

  @Autowired
  private Configuration configuration;

  @Autowired
  private MappingConverter mappingConverter;

  @Autowired
  private Reader reader;

  private Map<String, List<Map<String, String>>> cachedCsvs = Maps.newHashMap();

  private Map<String, List<Mapping>> cachedMappings = Maps.newHashMap();

  /**
   * Reads a list of CSV entries by entityFileName with caching.
   *
   * @param entityFileName the entity file name
   * @return the list of CSV entries
   */
  public List<Map<String, String>> readCsv(String entityFileName) {
    if (cachedCsvs.containsKey(entityFileName)) {
      return cachedCsvs.get(entityFileName);
    }
    List<Map<String, String>> csv = Collections.unmodifiableList(reader
        .readFromFile(new File(configuration.getDirectory(), entityFileName)));

    cachedCsvs.put(entityFileName, csv);
    return csv;
  }

  /**
   * Reads mappings from CSV by entityFileName with caching.
   *
   * @param entityFileName the entity file name
   * @return the list of mappings
   */
  public List<Mapping> readMappings(String entityFileName) {
    if (cachedMappings.containsKey(entityFileName)) {
      return cachedMappings.get(entityFileName);
    }
    String mappingFileName = entityFileName.replace(".csv", "_mapping.csv");
    List<Mapping> mappingForFile = Collections.unmodifiableList(mappingConverter
        .getMappingForFile(new File(configuration.getDirectory(), mappingFileName)));

    cachedMappings.put(entityFileName, mappingForFile);
    return mappingForFile;
  }

  /**
   * Checks if mappings contains valid data.
   *
   * @param source the processing source file
   * @param mappings the mapping list
   * @return result of validation
   */
  public boolean shouldProcess(SourceFile source, List<Mapping> mappings) {
    for (Mapping mapping : mappings) {
      if (isFromWrittenInCamelCase(mapping, source)) {
        return false;
      }

      String type = mapping.getType();
      if (indexOfIgnoreCase(type, "FROM_FILE") != INDEX_NOT_FOUND) {
        String entityFileName = mapping.getEntityName();
        if (!shouldProcess(source, entityFileName)) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean shouldProcess(SourceFile source, String entityFileName) {
    String parent = configuration.getDirectory();
    String inputFileName = new File(parent, entityFileName).getAbsolutePath();
    String mappingFileName = inputFileName.replace(".csv", "_mapping.csv");

    if (!inputAndMappingFileExist(inputFileName, mappingFileName, source)) {
      return false;
    }

    List<Mapping> mappings = readMappings(entityFileName);
    if (!shouldProcess(source, mappings)) {
      return false;
    }
    return true;
  }

  private boolean inputAndMappingFileExist(String input, String mapping, SourceFile source) {
    return inputAndMappingFileExist(new File(input), new File(mapping), source);
  }

  public boolean inputAndMappingFileExist(File input, File mapping, SourceFile source) {
    return inputAndMappingFileExist(input, source) && inputAndMappingFileExist(mapping, source);
  }

  private boolean inputAndMappingFileExist(File file, SourceFile source) {
    if (!file.exists() || file.isDirectory()) {
      LOGGER.warn(
          "{} will not be processed due to missing file: {}",
          source.getName(), file.getName()
      );
      return false;
    }

    return true;
  }

  private boolean isFromWrittenInCamelCase(Mapping mapping, SourceFile source) {
    // some converters assume that from is written in camelCase style
    String from = mapping.getFrom();
    if (!isWrittenInCamelCase(from)) {
      LOGGER.error("{} will not be processed because the phrase in the mapping 'from' "
          + "is not written in the camelCase style: {}", source.getName(), from);
      return true;
    }
    return false;
  }

  private boolean isWrittenInCamelCase(String str) {
    return str != null && str.matches(CAMEL_CASE_PATTERN);
  }
}
