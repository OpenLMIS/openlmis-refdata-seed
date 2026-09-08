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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.Configuration;
import org.openlmis.reader.GenericReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reports, per exported file, how many rows are unchanged, changed, added or gone since the
 * original master data. Only the columns both sides declare are compared.
 */
@Component
public class MasterDataComparisonReporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MasterDataComparisonReporter.class);

  private static final String CODE = "code";
  private static final String HEADING = "File";

  @Autowired
  private Configuration configuration;

  @Autowired
  private GenericReader reader;

  /**
   * Writes the comparison table to the log and returns it.
   *
   * @return the rendered table, or null when there is nothing to compare against
   */
  public String report() {
    String originalDirectory = configuration.getOriginalMasterDataDirectory();
    if (StringUtils.isBlank(originalDirectory)) {
      return null;
    }

    File outputDirectory = new File(configuration.getOutputDirectory());
    String[] exported = outputDirectory.list((dir, name) -> name.endsWith(".csv"));
    if (exported == null) {
      LOGGER.warn("Could not list {}; skipping the comparison report.", outputDirectory);
      return null;
    }

    List<Comparison> comparisons = new ArrayList<>();
    List<String> skipped = new ArrayList<>();
    for (String fileName : new TreeSet<>(Arrays.asList(exported))) {
      File original = new File(originalDirectory, fileName);
      if (!original.exists()) {
        skipped.add(fileName);
        continue;
      }
      comparisons.add(compare(fileName, reader.readFromFile(original),
          reader.readFromFile(new File(outputDirectory, fileName))));
    }

    String table = comparisons.isEmpty() ? null : render(comparisons);
    if (table != null) {
      LOGGER.info("Comparison with the original master data in {}:\n{}", originalDirectory, table);
    }
    if (!skipped.isEmpty()) {
      LOGGER.info("Not compared - no counterpart in the original master data: {}",
          String.join(", ", skipped));
    }

    return table;
  }

  private Comparison compare(String fileName, List<Map<String, String>> original,
      List<Map<String, String>> exported) {
    Comparison comparison = new Comparison(fileName);
    Set<String> columns = columnsOf(exported);
    columns.retainAll(columnsOf(original));

    String identity = identityColumn(columns, original, exported);
    Set<String> compared = new LinkedHashSet<>(columns);
    if (identity != null) {
      compared.remove(identity);
    }

    Map<String, Map<String, String>> originalByKey = new HashMap<>();
    for (Map<String, String> row : original) {
      originalByKey.put(keyOf(row, identity, columns), row);
    }

    for (Map<String, String> row : exported) {
      Map<String, String> counterpart = originalByKey.remove(keyOf(row, identity, columns));
      if (counterpart == null) {
        ++comparison.added;
      } else if (NaturalKeys.of(counterpart, compared).equals(NaturalKeys.of(row, compared))) {
        ++comparison.identical;
      } else {
        ++comparison.modified;
      }
    }

    comparison.removed = originalByKey.size();
    return comparison;
  }

  private String keyOf(Map<String, String> row, String identity, Set<String> columns) {
    return identity == null ? NaturalKeys.of(row, columns) : row.get(identity);
  }

  /**
   * The code when the file has a usable one, else its first column that is unique on both sides.
   * Null when neither holds, in which case rows are identified by all their shared values and
   * nothing can read as modified.
   */
  private String identityColumn(Set<String> columns, List<Map<String, String>> original,
      List<Map<String, String>> exported) {
    if (columns.contains(CODE) && isUnique(original, CODE) && isUnique(exported, CODE)) {
      return CODE;
    }

    for (String column : columns) {
      if (isUnique(original, column) && isUnique(exported, column)) {
        return column;
      }
    }

    return null;
  }

  private boolean isUnique(List<Map<String, String>> rows, String column) {
    Set<String> values = new LinkedHashSet<>();
    for (Map<String, String> row : rows) {
      String value = row.get(column);
      if (StringUtils.isBlank(value) || !values.add(value)) {
        return false;
      }
    }
    return !values.isEmpty();
  }

  private Set<String> columnsOf(Collection<Map<String, String>> rows) {
    Set<String> columns = new LinkedHashSet<>();
    for (Map<String, String> row : rows) {
      columns.addAll(row.keySet());
    }
    return columns;
  }

  private String render(List<Comparison> comparisons) {
    int width = HEADING.length();
    for (Comparison comparison : comparisons) {
      width = Math.max(width, comparison.fileName.length());
    }

    String format = "%-" + width + "s  %9s  %8s  %5s  %7s";
    StringBuilder table = new StringBuilder(
        String.format(format, HEADING, "Identical", "Modified", "New", "Removed"));
    for (Comparison comparison : comparisons) {
      table
          .append(System.lineSeparator())
          .append(String.format(format, comparison.fileName, comparison.identical,
              comparison.modified, comparison.added, comparison.removed));
    }

    return table.toString();
  }

  private static final class Comparison {

    private final String fileName;
    private int identical;
    private int modified;
    private int added;
    private int removed;

    private Comparison(String fileName) {
      this.fileName = fileName;
    }
  }
}
