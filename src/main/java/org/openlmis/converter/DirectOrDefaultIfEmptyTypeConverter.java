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

import java.util.Arrays;
import javax.json.JsonObjectBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class DirectOrDefaultIfEmptyTypeConverter extends BaseTypeConverter {

  private static final String NULL = "null";

  @Override
  public boolean supports(String type) {
    return "DIRECT_OR_DEFAULT_IF_EMPTY".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    for (String val : Arrays.asList(value, mapping.getDefaultValue())) {
      String buildValue = getValue(val);
      if (buildValue != null) {
        builder.add(mapping.getTo(), buildValue);
        break;
      }
    }
  }

  private String getValue(String value) {
    return (StringUtils.isEmpty(value) || NULL.equals(value)) ? null : value;
  }
}
