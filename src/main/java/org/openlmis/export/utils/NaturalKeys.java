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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;

final class NaturalKeys {

  private static final char KEY_VALUE_SEPARATOR = '\u0000';
  private static final String PART_SEPARATOR = "\u0001";

  private NaturalKeys() {
  }

  static Set<String> comparableColumns(Collection<String> header, String excluded) {
    Set<String> columns = new TreeSet<>(header);
    columns.remove(excluded);
    return columns;
  }

  static String of(Map<String, String> row, Collection<String> columns) {
    Set<String> parts = new TreeSet<>();
    for (String column : columns) {
      String value = row.get(column);
      if (StringUtils.isNotBlank(value)) {
        parts.add(column + KEY_VALUE_SEPARATOR + value);
      }
    }
    return String.join(PART_SEPARATOR, parts);
  }
}
