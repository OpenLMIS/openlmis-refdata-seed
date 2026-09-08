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

package org.openlmis.export.utils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.Configuration;
import org.openlmis.reader.GenericReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reuses the join codes of the original master data, matching each {@code *_FROM_FILE_*} child row
 * to its original counterpart by the values of the columns the child file declares, so extra
 * columns carried by the original master data are ignored.
 */
@Component
public class OriginalCodeResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(OriginalCodeResolver.class);

  @Autowired
  private Configuration configuration;

  @Autowired
  private GenericReader reader;

  private final Map<String, Map<String, String>> indexes = new HashMap<>();
  private int matched;
  private int generated;

  public boolean isEnabled() {
    return StringUtils.isNotBlank(configuration.getOriginalMasterDataDirectory());
  }

  /**
   * Finds the code the original master data used for the given child row.
   *
   * @param header the columns the child file declares; columns outside it are not compared
   * @return the original code, or null when there is no original counterpart
   */
  public String findOriginalCode(String fileName, Collection<String> header,
      Map<String, String> row, String joinColumn) {
    if (!isEnabled()) {
      return null;
    }

    Set<String> columns = NaturalKeys.comparableColumns(header, joinColumn);
    String code = indexes
        .computeIfAbsent(fileName + '#' + joinColumn, key -> buildIndex(fileName, joinColumn,
            columns))
        .get(NaturalKeys.of(row, columns));

    if (code == null) {
      ++generated;
    } else {
      ++matched;
    }

    return code;
  }

  /**
   * Reports how many code lookups were answered from the original master data, and how many fell
   * back to a generated code.
   */
  public void logSummary() {
    if (isEnabled()) {
      LOGGER.info("Original code lookups - reused: {}, generated: {}", matched, generated);
    }
  }

  private Map<String, String> buildIndex(String fileName, String joinColumn,
      Set<String> columns) {
    Map<String, String> index = new HashMap<>();
    File file = new File(configuration.getOriginalMasterDataDirectory(), fileName);
    List<Map<String, String>> originalRows = reader.readFromFile(file);

    for (Map<String, String> originalRow : originalRows) {
      String code = originalRow.get(joinColumn);
      if (StringUtils.isNotBlank(code)) {
        index.put(NaturalKeys.of(originalRow, columns), code);
      }
    }

    LOGGER.info("Indexed {} original rows from {} for code reuse.", index.size(), fileName);
    return index;
  }
}
