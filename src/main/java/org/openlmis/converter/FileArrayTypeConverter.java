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

package org.openlmis.converter;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;

import com.google.common.collect.Sets;

import org.openlmis.utils.AppHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

@Component
class FileArrayTypeConverter extends BaseTypeConverter {

  @Autowired
  private Converter converter;

  @Autowired
  private AppHelper appHelper;

  private final Set<String> filesMissingParentKey = Sets.newHashSet();

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_ARRAY_FROM_FILE_BY");
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    convert(builder, mapping, value, Collections.emptyMap());
  }

  /**
   * Builds the array from the rows of the entity file whose "by" column is listed in the value.
   * When the mapping's defaultValue is {@code childColumn:parentColumn} (e.g.
   * {@code facilityCode:code}), only rows whose childColumn equals the input row's parentColumn
   * are kept, so a child file can hold the rows of many parents.
   */
  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value,
      Map<String, String> inputRow) {
    List<String> codes = getArrayValues(value);
    String by = getBy(mapping.getType());
    String entityFileName = mapping.getEntityName();
    List<Map<String, String>> entityRows = appHelper.readCsv(entityFileName);
    Predicate<Map<String, String>> belongsToInputRow =
        belongsToInputRow(mapping, inputRow, entityRows);

    List<Map<String, String>> csvs = entityRows.stream()
        .filter(map -> codes.contains(map.get(by)))
        .filter(belongsToInputRow)
        .collect(Collectors.toList());

    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    if (!csvs.isEmpty()) {
      List<Mapping> mappings = appHelper.readMappings(entityFileName);
      for (Map<String, String> csv : csvs) {
        String json = converter.convert(csv, mappings).toString();

        try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
          arrayBuilder.add(jsonReader.readObject());
        }
      }
    }

    builder.add(mapping.getTo(), arrayBuilder);
  }

  private Predicate<Map<String, String>> belongsToInputRow(Mapping mapping,
      Map<String, String> inputRow, List<Map<String, String>> entityRows) {
    String[] parentKey = split(mapping.getDefaultValue(), ':');
    if (null == parentKey || parentKey.length != 2) {
      return row -> true;
    }

    String childColumn = trim(parentKey[0]);
    String parentValue = inputRow.get(trim(parentKey[1]));

    if (!entityRows.isEmpty() && !entityRows.get(0).containsKey(childColumn)
        && filesMissingParentKey.add(mapping.getEntityName())) {
      logger.error("{} has no {} column, so no row of it can be matched to its parent",
          mapping.getEntityName(), childColumn);
    }

    return row -> !isBlank(parentValue) && parentValue.equals(row.get(childColumn));
  }

}
