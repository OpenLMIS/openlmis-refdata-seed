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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

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
public class FileObjectReverseConverter extends BaseReverseTypeConverter {

  @Autowired
  @Lazy
  private Deconverter deconverter;

  @Autowired
  private AppHelper appHelper;

  @Autowired
  private ChildCsvCollector collector;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_OBJECT_FROM_FILE_BY");
  }

  @Override
  public void deconvert(JsonObject source, Mapping mapping, Map<String, String> row) {
    String to = mapping.getTo();

    if (!source.containsKey(to) || source.isNull(to)) {
      return;
    }

    JsonValue value = source.get(to);
    if (value.getValueType() != JsonValue.ValueType.OBJECT) {
      logger.warn("Field {} is not an object ({}); skipping.", to, value.getValueType());
      return;
    }

    String childFileName = mapping.getEntityName();
    List<Mapping> childMappings = appHelper.readMappings(childFileName);
    List<String> childHeader = deconverter.getHeader(childMappings);
    String joinColumn = getBy(mapping.getType());

    Map<String, String> childRow = deconverter.deconvert((JsonObject) value, childMappings);
    String joinValue = childRow.get(joinColumn);

    // A blank join value means this mapping does not apply to the embedded object. This is how the
    // node/organization duality (two mappings both targeting the same field) resolves itself: a
    // facility node's id only resolves in the facility service, an organization node's only in the
    // organization service, so the mapping for the other kind produces nothing and is skipped.
    if (isBlank(joinValue)) {
      return;
    }

    inheritFromParent(childRow, childHeader, joinColumn, row);

    collector.add(childFileName, childHeader, childRow);
    if (isBlank(row.get(mapping.getFrom()))) {
      row.put(mapping.getFrom(), joinValue);
    }
  }

  /**
   * Fills any child column that the embedded object could not supply (e.g. the owning user's
   * {@code username} on an {@code EmailDetails} row, which comes from {@code FIND_EMAIL_VERIFIED}
   * and lives on the parent, not on the child object) from the identically-named column on the
   * parent row. The join column is never overwritten, so a child key can never be replaced by a
   * parent value.
   */
  private void inheritFromParent(Map<String, String> childRow, List<String> childHeader,
      String joinColumn, Map<String, String> parentRow) {
    for (String column : childHeader) {
      if (column.equals(joinColumn) || !isBlank(childRow.get(column))) {
        continue;
      }
      String parentValue = parentRow.get(column);
      if (!isBlank(parentValue)) {
        childRow.put(column, parentValue);
      }
    }
  }
}
