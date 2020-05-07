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

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import org.openlmis.utils.AppHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultFileArrayTypeConverter extends BaseTypeConverter {

  @Autowired
  private Converter converter;

  @Autowired
  private AppHelper appHelper;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "DEFAULT_TO_ARRAY_FROM_FILE_BY");
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    String defaultValue = mapping.getDefaultValue();
    List<String> codes = getArrayValues(defaultValue);
    String by = getBy(mapping.getType());
    String entityFileName = mapping.getEntityName();

    List<Map<String, String>> csvs = appHelper.readCsv(entityFileName).stream()
        .filter(map -> codes.contains(map.get(by)))
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
}
