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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.Configuration;
import org.openlmis.reader.GenericReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reports, per exported file, how many rows are unchanged, changed, added or gone since the
 * original master data. Only the columns both sides declare are compared, and numbers are compared
 * by value so that {@code 3} and {@code 3.0} do not read as a change.
 */
@Component
public class MasterDataComparisonReporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MasterDataComparisonReporter.class);

  private static final String HEADING = "File";
  private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");
  private static final char KEY_SEPARATOR = '\u0000';

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

    List<String> identity = identityColumns(columns, original, exported);
    Set<String> compared = new LinkedHashSet<>(columns);
    compared.removeAll(identity);

    Map<String, Map<String, String>> originalByKey = new HashMap<>();
    for (Map<String, String> row : original) {
      originalByKey.put(keyOf(row, identity), row);
    }

    for (Map<String, String> row : exported) {
      Map<String, String> counterpart = originalByKey.remove(keyOf(row, identity));
      if (counterpart == null) {
        ++comparison.added;
      } else if (sameValues(counterpart, row, compared)) {
        ++comparison.identical;
      } else {
        ++comparison.modified;
      }
    }

    comparison.removed = originalByKey.size();
    return comparison;
  }

  private String keyOf(Map<String, String> row, List<String> identity) {
    StringBuilder key = new StringBuilder();
    for (String column : identity) {
      key.append(normalise(row.get(column))).append(KEY_SEPARATOR);
    }
    return key.toString();
  }

  private boolean sameValues(Map<String, String> left, Map<String, String> right,
      Collection<String> columns) {
    for (String column : columns) {
      if (!normalise(left.get(column)).equals(normalise(right.get(column)))) {
        return false;
      }
    }
    return true;
  }

  private String normalise(String value) {
    String trimmed = StringUtils.trimToEmpty(value);
    return NUMBER.matcher(trimmed).matches()
        ? new BigDecimal(trimmed).stripTrailingZeros().toPlainString()
        : trimmed;
  }

  /**
   * The shortest run of leading columns that identifies a row on both sides. Falls back to every
   * shared column, in which case nothing can read as modified.
   */
  private List<String> identityColumns(Set<String> columns, List<Map<String, String>> original,
      List<Map<String, String>> exported) {
    List<String> identity = new ArrayList<>();
    for (String column : columns) {
      identity.add(column);
      if (isUnique(original, identity) && isUnique(exported, identity)) {
        return identity;
      }
    }

    return new ArrayList<>(columns);
  }

  private boolean isUnique(List<Map<String, String>> rows, List<String> identity) {
    Set<String> keys = new LinkedHashSet<>();
    for (Map<String, String> row : rows) {
      for (String column : identity) {
        if (StringUtils.isBlank(row.get(column))) {
          return false;
        }
      }
      if (!keys.add(keyOf(row, identity))) {
        return false;
      }
    }
    return !keys.isEmpty();
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
