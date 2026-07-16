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

package org.openlmis.export.converter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.converter.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Deconverter {

  @Autowired
  private List<ReverseTypeConverter> converters;

  /**
   * Converts a single JSON entity fetched from OLMIS into a CSV row, keyed by the mapping's
   * {@code from} columns. Inverse of {@link org.openlmis.converter.Converter#convert}.
   *
   * @param sourceRow the JSON representation of one entity
   * @param mappings the mapping specifications for the entity
   * @return the CSV row as a map of column name to value
   */
  public Map<String, String> deconvert(JsonObject sourceRow, List<Mapping> mappings) {
    Map<String, String> row = new LinkedHashMap<>();
    for (Mapping mapping : mappings) {
      converters.stream()
          .filter(c -> c.supports(mapping.getType()))
          .findFirst()
          .ifPresent(c -> c.deconvert(sourceRow, mapping, row));
    }
    return row;
  }

  /**
   * Checks whether every mapping type in the given list has a registered reverse converter.
   *
   * @param mappings the mapping specifications for the entity
   * @return true if all mapping types are supported
   */
  public boolean supportsAll(List<Mapping> mappings) {
    return mappings.stream()
        .map(Mapping::getType)
        .allMatch(type -> converters.stream()
            .anyMatch(c -> c.supports(type)));
  }

  /**
   * Returns the distinct mapping types that have no registered reverse converter, for logging.
   *
   * @param mappings the mapping specifications for the entity
   * @return the set of unsupported type names, in first-seen order
   */
  public Set<String> unsupportedTypes(List<Mapping> mappings) {
    return mappings.stream()
        .map(Mapping::getType)
        .filter(type -> converters.stream()
            .noneMatch(c -> c.supports(type)))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * Builds the CSV header from the mappings: the distinct, non-blank {@code from} columns in
   * mapping order.
   *
   * @param mappings the mapping specifications for the entity
   * @return the ordered list of CSV column names
   */
  public List<String> getHeader(List<Mapping> mappings) {
    return mappings.stream()
        .map(Mapping::getFrom)
        .filter(StringUtils::isNotBlank)
        .distinct()
        .collect(Collectors.toList());
  }
}
