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

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import org.openlmis.Configuration;
import org.openlmis.converter.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public final class AppHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppHelper.class);

  private AppHelper() {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if mappings contains valid data.
   */
  public static boolean shouldProcess(Configuration configuration, SourceFile source,
                                      List<Mapping> mappings) {
    for (int i = 0, size = mappings.size(); i < size; ++i) {
      Mapping mapping = mappings.get(i);
      String type = mapping.getType();

      if (startsWithIgnoreCase(type, "TO_ARRAY_FROM_FILE_BY")) {
        String parent = configuration.getDirectory();
        String inputFileName = new File(parent, mapping.getEntityName()).getAbsolutePath();
        String mappingFileName = inputFileName.replace(".csv", "_mapping.csv");

        if (!shouldProcess(inputFileName, mappingFileName, source)) {
          return false;
        }
      }
    }

    return true;
  }

  private static boolean shouldProcess(String input, String mapping, SourceFile source) {
    return shouldProcess(new File(input), new File(mapping), source);
  }

  public static boolean shouldProcess(File input, File mapping, SourceFile source) {
    return shouldProcess(input, source) && shouldProcess(mapping, source);
  }

  private static boolean shouldProcess(File file, SourceFile source) {
    if (!file.exists() || file.isDirectory()) {
      LOGGER.warn(
          "{} will not be processed due to missing file: {}",
          source.getName(), file.getName()
      );
      return false;
    }

    return true;
  }

}
