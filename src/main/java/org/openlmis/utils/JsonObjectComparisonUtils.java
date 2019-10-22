package org.openlmis.utils;

import java.math.BigDecimal;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import org.apache.commons.lang3.math.NumberUtils;
import org.openlmis.DataSeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonObjectComparisonUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

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
  public static Boolean equals(JsonObject newObject, JsonObject existingObject) {
    for (Map.Entry<String, JsonValue> entry : newObject.entrySet()) {
      JsonValue existingValue = existingObject.getOrDefault(entry.getKey(), JsonValue.NULL);
      if (!jsonValuesEqual(entry.getValue(), existingValue)) {
        LOGGER.info(entry.getKey() + " has changed.");
        LOGGER.debug("Previous value: " + getStringValue(existingValue));
        LOGGER.debug("New value: " + getStringValue(entry.getValue()));
        LOGGER.debug("Skipping next checks. Note: there could be more differences.");
        return false;
      }
    }
    return true;
  }

  private static Boolean jsonArraysEqual(JsonArray newArray, JsonArray existingArray) {
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

  private static Boolean jsonValuesEqual(JsonValue newValue, JsonValue existingValue) {
    if (newValue.getValueType().equals(JsonValue.ValueType.ARRAY)
        && existingValue.getValueType().equals(JsonValue.ValueType.ARRAY)) {
      return jsonArraysEqual((JsonArray) newValue, (JsonArray) existingValue);
    } else if (newValue.getValueType().equals(JsonValue.ValueType.OBJECT)
        && existingValue.getValueType().equals(JsonValue.ValueType.OBJECT)) {
      return equals((JsonObject) newValue, (JsonObject) existingValue);
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

  private static String getStringValue(JsonValue str) {
    return str.toString().replace("\"", "");
  }

  private JsonObjectComparisonUtils() {
  }
}
