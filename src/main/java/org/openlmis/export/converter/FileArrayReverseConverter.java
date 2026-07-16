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

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ChildCsvCollector;
import org.openlmis.utils.AppHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class FileArrayReverseConverter extends BaseReverseTypeConverter {

  @Autowired
  @Lazy
  private Deconverter deconverter;

  @Autowired
  private AppHelper appHelper;

  @Autowired
  private ChildCsvCollector collector;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_ARRAY_FROM_FILE_BY")
        && !equalsIgnoreCase(type, "TO_ARRAY_FROM_FILE_BY_PROGRAM_CODE");
  }

  @Override
  public void deconvert(JsonObject source, Mapping mapping, Map<String, String> row) {
    String to = mapping.getTo();

    if (!source.containsKey(to) || source.isNull(to)) {
      return;
    }

    JsonValue value = source.get(to);
    if (value.getValueType() != JsonValue.ValueType.ARRAY) {
      logger.warn("Field {} is not an array ({}); skipping.", to, value.getValueType());
      return;
    }

    String childFileName = mapping.getEntityName();
    List<Mapping> childMappings = appHelper.readMappings(childFileName);
    List<String> childHeader = deconverter.getHeader(childMappings);
    String joinColumn = getBy(mapping.getType());

    // When the mapping's 'from' column was already produced by an earlier mapping (e.g. a DIRECT
    // productCode that this mapping merely joins children by), that value is the shared join key
    // for every child row and the parent column must NOT be overwritten. Otherwise 'from' is a
    // dedicated column that holds the bracketed list of the children's own (synthetic) codes.
    String sharedKey = row.get(mapping.getFrom());
    boolean joinsByExistingColumn = !isBlank(sharedKey);

    List<String> joinValues = new ArrayList<>();

    for (JsonValue element : source.getJsonArray(to)) {
      if (element.getValueType() != JsonValue.ValueType.OBJECT) {
        logger.warn("Element of {} is not an object ({}); skipping element.",
            to, element.getValueType());
        continue;
      }

      Map<String, String> childRow = deconverter.deconvert((JsonObject) element, childMappings);
      String joinValue = joinsByExistingColumn ? sharedKey : resolveJoinValue(childRow, joinColumn);
      childRow.put(joinColumn, joinValue);
      collector.add(childFileName, childHeader, childRow);
      joinValues.add(joinValue);
    }

    if (!joinsByExistingColumn && !joinValues.isEmpty()) {
      row.put(mapping.getFrom(), "[" + String.join(",", joinValues) + "]");
    }
  }

  /**
   * Determines the value that links a child row back to its parent. If the reversed child row
   * already carries the join column (a key recoverable from the API, e.g. a program code), that
   * value is used directly. Otherwise the column is a seed-time-only synthetic key (marked SKIP in
   * the child mapping) with no API counterpart, so a deterministic code is synthesized from the
   * row's contents. It is stable across runs and de-duplicated, so the child CSV and every parent
   * reference stay internally consistent (which is all the seed tool needs on re-import); it will
   * not, however, reproduce the original hand-authored codes from the seed CSVs.
   */
  private String resolveJoinValue(Map<String, String> childRow, String joinColumn) {
    String existing = childRow.get(joinColumn);
    if (!isBlank(existing)) {
      return existing;
    }

    return "GEN_" + Integer.toHexString(childRow.toString().hashCode());
  }
}
