package org.openlmis.utils;

import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.openlmis.DataSeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonObjectComparisonUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  /**
   * Compares two JSONs: based on CSV entry and existing object fetched form OLMIS. The method
   * returns false only if a value from CSV differs from the value of OLMIS object. Fields which are
   * not represented by CSV object are not taken into account during the comparison.
   *
   * @param csvEntryObject JSON which was created based on CSV data and mappings.
   * @param existingObject JSON which represents object fetched from OLMIS.
   * @return The comparison result.
   */
  public static Boolean equals(JsonObject csvEntryObject, JsonObject existingObject) {
    for (Map.Entry<String, JsonValue> entry : csvEntryObject.entrySet()) {
      JsonValue existingValue = existingObject.getOrDefault(entry.getKey(), JsonValue.NULL);
      if (!jsonValuesEqual(entry.getValue(), existingValue)) {
        LOGGER.info(entry.getKey() + " has changed.");
        LOGGER.debug("Previous value: " + existingValue.toString());
        LOGGER.debug("New value: " + entry.getValue().toString());
        return false;
      }
    }
    return true;
  }

  private static Boolean jsonArraysEqual(JsonArray csvEntryArray, JsonArray existingArray) {
    for (JsonValue csvItem : csvEntryArray) {
      boolean itemExistsInBothArrays = false;
      for (JsonValue existingItem : existingArray) {
        if (jsonValuesEqual(csvItem, existingItem)) {
          itemExistsInBothArrays = true;
          break;
        }
      }
      if (!itemExistsInBothArrays) {
        return false;
      }
    }
    return csvEntryArray.size() == existingArray.size();
  }

  private static Boolean jsonValuesEqual(JsonValue csvEntryValue, JsonValue existingValue) {
    if (csvEntryValue.getValueType().equals(JsonValue.ValueType.ARRAY)
        && existingValue.getValueType().equals(JsonValue.ValueType.ARRAY)) {
      return jsonArraysEqual((JsonArray) csvEntryValue, (JsonArray) existingValue);
    } else if (csvEntryValue.getValueType().equals(JsonValue.ValueType.OBJECT)
        && existingValue.getValueType().equals(JsonValue.ValueType.OBJECT)) {
      return equals((JsonObject) csvEntryValue, (JsonObject) existingValue);
    }
    return csvEntryValue.toString().replace("\"", "").equals(
        existingValue.toString().replace("\"", ""));
  }

  private JsonObjectComparisonUtils() {
  }
}
