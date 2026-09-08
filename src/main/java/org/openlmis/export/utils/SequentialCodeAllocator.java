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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.Configuration;
import org.openlmis.reader.GenericReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Continues the numbering of the original master data for child rows that have no counterpart
 * there, so a file seeded with {@code RA-1..RA-934} gains {@code RA-935} onwards. Allocations last
 * for a single run; the original master data is only ever read.
 */
@Component
public class SequentialCodeAllocator {

  private static final Logger LOGGER = LoggerFactory.getLogger(SequentialCodeAllocator.class);

  private static final Pattern SEQUENTIAL = Pattern.compile("^(.*\\D)?(\\d{1,9})$");

  @Autowired
  private Configuration configuration;

  @Autowired
  private GenericReader reader;

  private final Map<String, Sequence> sequences = new HashMap<>();
  private final Map<String, String> allocations = new HashMap<>();

  public boolean isEnabled() {
    return StringUtils.isNotBlank(configuration.getOriginalMasterDataDirectory());
  }

  /**
   * Allocates the next code of the file's sequence, returning the same code every time the same row
   * is encountered.
   *
   * @param header the columns the child file declares
   * @return the allocated code, or null when there is no sequence to continue
   */
  public String allocate(String fileName, Collection<String> header, Map<String, String> row,
      String joinColumn) {
    if (!isEnabled()) {
      return null;
    }

    Sequence sequence = sequences.computeIfAbsent(fileName, name -> readSequence(name, joinColumn));
    if (!sequence.isUsable()) {
      return null;
    }

    String key = fileName + '#'
        + NaturalKeys.of(row, NaturalKeys.comparableColumns(header, joinColumn));
    String allocated = allocations.get(key);
    if (allocated == null) {
      allocated = sequence.next();
      allocations.put(key, allocated);
    }

    return allocated;
  }

  /**
   * Reports the codes allocated for each child file.
   */
  public void logSummary() {
    for (Map.Entry<String, Sequence> entry : sequences.entrySet()) {
      Sequence sequence = entry.getValue();
      if (sequence.isUsable() && sequence.count > 0) {
        LOGGER.info("Allocated {} new codes for {} ({} .. {}).", sequence.count, entry.getKey(),
            sequence.first(), sequence.last());
      }
    }
  }

  private Sequence readSequence(String fileName, String joinColumn) {
    File file = new File(configuration.getOriginalMasterDataDirectory(), fileName);
    List<Map<String, String>> originalRows = reader.readFromFile(file);

    String prefix = null;
    int highest = 0;
    int shortest = Integer.MAX_VALUE;
    int longest = 0;

    for (Map<String, String> originalRow : originalRows) {
      String code = originalRow.get(joinColumn);
      if (StringUtils.isBlank(code)) {
        continue;
      }

      Matcher matcher = SEQUENTIAL.matcher(code);
      if (!matcher.matches()) {
        LOGGER.info("{} holds codes that are not sequential (e.g. {}); new rows keep a generated "
            + "code.", fileName, code);
        return Sequence.unusable();
      }

      String codePrefix = StringUtils.defaultString(matcher.group(1));
      String digits = matcher.group(2);
      if (prefix != null && !prefix.equals(codePrefix)) {
        LOGGER.info("{} mixes the code prefixes {} and {}; new rows keep a generated code.",
            fileName, prefix, codePrefix);
        return Sequence.unusable();
      }

      prefix = codePrefix;
      shortest = Math.min(shortest, digits.length());
      longest = Math.max(longest, digits.length());
      highest = Math.max(highest, Integer.parseInt(digits));
    }

    if (prefix == null) {
      return Sequence.unusable();
    }

    Sequence sequence = new Sequence(prefix, shortest == longest ? longest : 0, highest + 1);
    LOGGER.info("Continuing the {} sequence of {} from {}.", prefix, fileName, sequence.first());
    return sequence;
  }

  private static final class Sequence {

    private final String prefix;
    private final int width;
    private final int start;
    private int count;

    private Sequence(String prefix, int width, int start) {
      this.prefix = prefix;
      this.width = width;
      this.start = start;
    }

    private static Sequence unusable() {
      return new Sequence(null, 0, 0);
    }

    private boolean isUsable() {
      return prefix != null;
    }

    private String next() {
      return format(start + count++);
    }

    private String first() {
      return format(start);
    }

    private String last() {
      return format(start + count - 1);
    }

    private String format(int number) {
      return prefix + (width > 1
          ? String.format("%0" + width + "d", number)
          : Integer.toString(number));
    }
  }
}
