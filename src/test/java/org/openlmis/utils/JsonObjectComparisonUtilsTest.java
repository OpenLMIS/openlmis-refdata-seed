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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.json.Json;
import javax.json.JsonObject;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class JsonObjectComparisonUtilsTest {

  public static final String FIELD_NAME = "fieldName";
  public static final String STRING_VALUE_2 = "A112";
  public static final String STRING_VALUE_1 = "A111";

  @Test
  public void equalsShouldCompareDoubleWithIntNumerically() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue("123"),
        createJsonObjectWithValue(123.00));

    Assert.assertTrue(result);
  }

  @Test
  public void equalsShouldCompareWithIntDoubleNumerically() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue("123.00"),
        createJsonObjectWithValue(123));

    Assert.assertTrue(result);
  }

  @Test
  public void equalsShouldReturnFalseWhenTryToCompareWithNullNumerically() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue("null"),
        createJsonObjectWithValue(0));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldReturnFalseWhenTryToCompareWithEmptyStringNumerically() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue(""),
        createJsonObjectWithValue(123.00));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldNotCompareNumericallyIfExistingValueIsNotNumeric() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue("123.00"),
        createJsonObjectWithValue("123"));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldCompareWithoutRoundingWhenNumberScaleIsEqualPrecision() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue("0.33333"),
        createJsonObjectWithValue(0.33334));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldCompareWithRoundingDownWhenNumberScaleIsLowerThenPrecision() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue("0.333333"),
        createJsonObjectWithValue(0.333334));

    Assert.assertTrue(result);
  }

  @Test
  public void equalsShouldCompareWithRoundingUpWhenNumberScaleIsLowerThenPrecision() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue("0.333333"),
        createJsonObjectWithValue(0.333336));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldReturnTrueIfComparingEqualStrings() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue(STRING_VALUE_1),
        createJsonObjectWithValue(STRING_VALUE_1));

    Assert.assertTrue(result);
  }

  @Test
  public void equalsShouldReturnFalseIfComparingNotEqualStrings() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue(STRING_VALUE_1),
        createJsonObjectWithValue(STRING_VALUE_2));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldReturnFalseIfValuesInNestedArrayAreNotEqual() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonArrayWithValue(STRING_VALUE_1),
        createJsonArrayWithValue(STRING_VALUE_2));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldReturnTrueIfValuesInNestedArrayAreEqual() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonArrayWithValue(STRING_VALUE_1),
        createJsonArrayWithValue(STRING_VALUE_1));

    Assert.assertTrue(result);
  }

  @Test
  public void equalsShouldReturnTrueIfValuesInNestedObjectAreEqual() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue(createJsonObjectWithValue(STRING_VALUE_1)),
        createJsonObjectWithValue(createJsonObjectWithValue(STRING_VALUE_1)));

    Assert.assertTrue(result);
  }

  @Test
  public void equalsShouldReturnFalseIfValuesInNestedObjectAreNotEqual() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue(createJsonObjectWithValue(STRING_VALUE_1)),
        createJsonObjectWithValue(createJsonObjectWithValue(STRING_VALUE_2)));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldReturnFalseIfItIsObjectCleanupCase() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue(Json.createObjectBuilder().build()),
        createJsonObjectWithValue(createJsonObjectWithValue(STRING_VALUE_2)));

    Assert.assertFalse(result);
  }

  private JsonObject createJsonArrayWithValue(String value) {
    return Json.createObjectBuilder()
        .add(FIELD_NAME, Json.createArrayBuilder().add(value))
        .build();
  }

  private JsonObject createJsonObjectWithValue(String value) {
    return Json.createObjectBuilder().add(FIELD_NAME, value).build();
  }

  private JsonObject createJsonObjectWithValue(double value) {
    return Json.createObjectBuilder().add(FIELD_NAME, value).build();
  }

  private JsonObject createJsonObjectWithValue(int value) {
    return Json.createObjectBuilder().add(FIELD_NAME, value).build();
  }

  private JsonObject createJsonObjectWithValue(JsonObject value) {
    return Json.createObjectBuilder().add(FIELD_NAME, value).build();
  }
}