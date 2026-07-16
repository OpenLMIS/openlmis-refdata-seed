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

import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

import javax.json.JsonString;
import javax.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseReverseTypeConverter implements ReverseTypeConverter {
  final Logger logger = LoggerFactory.getLogger(getClass());

  String getCsvString(JsonValue json) {
    if (json == null || json.equals(JsonValue.NULL)) {
      return null;
    } else if (json.getValueType() == JsonValue.ValueType.STRING) {
      return ((JsonString) json).getString();
    } else if (json.getValueType() == JsonValue.ValueType.TRUE) {
      return "True";
    } else if (json.getValueType() == JsonValue.ValueType.FALSE) {
      return "False";
    }
    return json.toString();
  }

  String getBy(String type) {
    if (!type.contains("BY_")) {
      return null;
    }

    String by = type.substring(type.lastIndexOf("BY_") + 3);
    by = capitalizeFully(by, '_');
    by = remove(by, "_");

    return Character.toLowerCase(by.charAt(0)) + by.substring(1);
  }
}
