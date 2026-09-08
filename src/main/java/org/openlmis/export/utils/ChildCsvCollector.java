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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Accumulates the rows of secondary ("child") CSV files that are produced as a side effect of
 * reversing {@code *_FROM_FILE_*} mappings. A single child file (e.g. {@code Nodes.csv}) can be
 * referenced by several parent entities, and a single join key can map to several distinct rows
 * (e.g. one orderable belonging to several programs), so rows are de-duplicated by their full
 * content across the whole run. {@link DataExporter} flushes the collected files after all parents
 * are processed.
 */
@Component
public class ChildCsvCollector {

  private static final Pattern TRAILING_NUMBER = Pattern.compile("^(.*\\D)?(\\d{1,18})$");

  private final Map<String, List<String>> headers = new LinkedHashMap<>();
  private final Map<String, Set<Map<String, String>>> rows = new LinkedHashMap<>();

  /**
   * Records one child row for the given child CSV file. Rows are de-duplicated by their full
   * content, so re-encountering an identical row is a no-op while rows that differ in any column
   * are all kept.
   *
   * @param fileName the child CSV file name (e.g. {@code RequisitionGroupProgramSchedules.csv})
   * @param header the ordered column names for the child file
   * @param row the child CSV row
   */
  public void add(String fileName, List<String> header, Map<String, String> row) {
    headers.putIfAbsent(fileName, header);
    rows.computeIfAbsent(fileName, name -> new LinkedHashSet<>()).add(row);
  }

  public Set<String> fileNames() {
    return rows.keySet();
  }

  public List<String> header(String fileName) {
    return headers.get(fileName);
  }

  /**
   * Returns the file's rows ordered by its leading column - the one the parents join on -
   * numerically where the codes end in a number, with the other columns breaking ties.
   *
   * @param fileName the child CSV file name
   * @return the ordered rows
   */
  public Collection<Map<String, String>> rows(String fileName) {
    List<Map<String, String>> ordered = new ArrayList<>(rows.get(fileName));
    List<String> header = headers.get(fileName);
    String leading = header.get(0);

    Comparator<Map<String, String>> comparator = (left, right) ->
        compareCodes(left.get(leading), right.get(leading));
    for (String column : header.subList(1, header.size())) {
      comparator = comparator.thenComparing(
          row -> StringUtils.defaultString(row.get(column)));
    }

    ordered.sort(comparator);
    return ordered;
  }

  private static int compareCodes(String left, String right) {
    String leftCode = StringUtils.defaultString(left);
    String rightCode = StringUtils.defaultString(right);

    Matcher leftMatch = TRAILING_NUMBER.matcher(leftCode);
    Matcher rightMatch = TRAILING_NUMBER.matcher(rightCode);
    if (leftMatch.matches() && rightMatch.matches()) {
      String leftPrefix = StringUtils.defaultString(leftMatch.group(1));
      String rightPrefix = StringUtils.defaultString(rightMatch.group(1));
      if (leftPrefix.equals(rightPrefix)) {
        return Long.compare(Long.parseLong(leftMatch.group(2)),
            Long.parseLong(rightMatch.group(2)));
      }
    }

    return leftCode.compareTo(rightCode);
  }
}
