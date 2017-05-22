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

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

@Component
public class CreateObjectTypeConverter extends BaseTypeConverter {

  @Override
  public boolean supports(String type) {
    return "TO_OBJECT".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    List<String> entries = Lists.newArrayList(StringUtils.split(value, ','));
    JsonObjectBuilder inner = Json.createObjectBuilder();

    for (String entry : entries) {
      List<String> keyValue = Lists.newArrayList(StringUtils.split(entry, ':'));

      if (keyValue.size() == 2) {
        inner.add(keyValue.get(0), keyValue.get(1));
      } else {
        logger.warn(
            "Invalid map entry representation: {}. Desired format is \"<key>:<value>\".", entry
        );
      }
    }

    builder.add(mapping.getTo(), inner.build());
  }

}
