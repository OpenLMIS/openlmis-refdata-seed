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

package org.openlmis.utils;

import java.math.BigDecimal;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonObjectComparisonUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonObjectComparisonUtils.class);

  private static final int FLOAT_PRECISION = 5;

  /**
   * Compares two JSONs: based on input entry (CSV) and existing object fetched form OLMIS. The
   * method returns false only if a value from input differs from the value of OLMIS object. Fields
   * which are not represented by input object are not taken into account during the comparison.
   *
   * @param newObject JSON which was created based on input data (CSV) and mappings.
   * @param existingObject JSON which represents object fetched from OLMIS.
   * @return The comparison result.
   */
  public static boolean equals(JsonObject newObject, JsonObject existingObject) {
    for (Map.Entry<String, JsonValue> entry : newObject.entrySet()) {
      JsonValue existingValue = existingObject.getOrDefault(entry.getKey(), JsonValue.NULL);
      if (!jsonValuesEqual(entry.getValue(), existingValue)) {
        LOGGER.info("{} has changed.", entry.getKey());
        LOGGER.debug("Previous value: {}", getStringValue(existingValue));
        LOGGER.debug("New value: {}", getStringValue(entry.getValue()));
        LOGGER.debug("Skipping next checks. Note: there could be more differences.");
        return false;
      }
    }
    return true;
  }

  private static boolean jsonArraysEqual(JsonArray newArray, JsonArray existingArray) {
    for (JsonValue newItem : newArray) {
      boolean itemExistsInBothArrays = false;
      for (JsonValue existingItem : existingArray) {
        if (jsonValuesEqual(newItem, existingItem)) {
          itemExistsInBothArrays = true;
          break;
        }
      }
      if (!itemExistsInBothArrays) {
        return false;
      }
    }
    return newArray.size() == existingArray.size();
  }

  private static boolean jsonValuesEqual(JsonValue newValue, JsonValue existingValue) {
    if (newValue.getValueType().equals(JsonValue.ValueType.ARRAY)
        && existingValue.getValueType().equals(JsonValue.ValueType.ARRAY)) {
      return jsonArraysEqual((JsonArray) newValue, (JsonArray) existingValue);
    } else if (newValue.getValueType().equals(JsonValue.ValueType.OBJECT)
        && existingValue.getValueType().equals(JsonValue.ValueType.OBJECT)) {
      return !isObjectCleanupCase((JsonObject) newValue, (JsonObject) existingValue)
          && equals((JsonObject) newValue, (JsonObject) existingValue);
    } else if (existingValue.getValueType().equals(ValueType.NUMBER)) {
      return isEqualNumerically(getStringValue(existingValue), getStringValue(newValue));
    }
    return getStringValue(newValue).equals(getStringValue(existingValue));
  }

  private static boolean isEqualNumerically(String value1, String value2) {
    boolean isExitingValueNumeric = NumberUtils.isParsable(value1);
    boolean isNewValueNumeric = NumberUtils.isParsable(value2);

    if (isExitingValueNumeric && isNewValueNumeric) {
      return createBigDecimal(value1).compareTo(createBigDecimal(value2)) == 0;
    }
    return false;
  }

  private static BigDecimal createBigDecimal(String value) {
    return NumberUtils.createBigDecimal(value)
        .setScale(FLOAT_PRECISION, BigDecimal.ROUND_HALF_EVEN);
  }

  /**
   * In some situation we want to clean up the content of an object. Especially when the object is
   * built basing on an array (in a domain object entity is represented by one-element-array, but in
   * REST API by an object with one element). If we want to clean up the object like this it is not
   * possible because the equal function does not analyze the object's fields that exist in a new
   * object. An example that requires this logic can be found in identifiers of Orderable. There is
   * the assumption that an Orderable can have one identifier attached, so this logic allows to
   * recognize when identifiers should be removed from the object.
   *
   * @param newObject JSON which was created based on input data (CSV) and mappings.
   * @param existingObject JSON which represents object fetched from OLMIS.
   * @return The comparison result.
   */
  private static boolean isObjectCleanupCase(JsonObject newObject, JsonObject existingObject) {
    return newObject.isEmpty() && !existingObject.isEmpty();
  }

  private static String getStringValue(JsonValue str) {
    return str.toString().replace("\"", "");
  }

  private JsonObjectComparisonUtils() {
  }
}
