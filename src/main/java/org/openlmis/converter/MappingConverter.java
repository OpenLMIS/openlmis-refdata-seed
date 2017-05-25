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

package org.openlmis.converter;

import com.google.common.collect.Lists;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Component
public class MappingConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MappingConverter.class);

  /**
   * Gets the mapping specification fro mthe given file.
   * @param mappingFile file containing mappings
   * @return mapping spec
   */
  public List<Mapping> getMappingForFile(File mappingFile) {
    if (!mappingFile.exists() || mappingFile.isDirectory()) {
      LOGGER.warn("The mapping file {} does not exist. Entity won't be created or updated",
          mappingFile.getAbsolutePath());
      return Lists.newArrayList();
    }

    try (CSVReader reader = new CSVReader(new FileReader(mappingFile))) {
      HeaderColumnNameMappingStrategy<Mapping> strategy = new HeaderColumnNameMappingStrategy<>();
      strategy.setType(Mapping.class);

      CsvToBean<Mapping> csvToBean = new CsvToBean<>();
      return csvToBean.parse(strategy, reader);
    } catch (IOException ex) {
      LOGGER.error("The mapping file " + mappingFile.getName() + " could not be open or read.", ex);
      return Lists.newArrayList();
    }
  }
}
