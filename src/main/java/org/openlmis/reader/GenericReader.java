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

package org.openlmis.reader;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import org.openlmis.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class GenericReader implements Reader {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericReader.class);

  @Autowired
  protected Configuration configuration;

  /**
   * Reads the CSV file and converts it to a collection of map entries. Each map in the
   * collection represents field values of a single CSV line.
   *
   * @param fileName the name of file with data.
   * @return List of map entries
   */
  @Override
  public List<Map<String, String>> readFromFile(String fileName) {
    try {
      File file = new File(fileName);
      
      CsvMapper mapper = new CsvMapper();
      CsvSchema schema = CsvSchema.emptySchema().withHeader();
      MappingIterator<Map<String, String>> iterator = mapper
          .readerFor(Map.class)
          .with(schema)
          .readValues(file);

      return iterator.readAll(Lists.newArrayList());
    } catch (IOException ex) {
      LOGGER.warn("The file with name " + fileName + " does not exist", ex);
      return Lists.newArrayList();
    }
  }
}
