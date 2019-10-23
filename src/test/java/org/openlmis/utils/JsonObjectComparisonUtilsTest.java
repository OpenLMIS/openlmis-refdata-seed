package org.openlmis.utils;

import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonObjectComparisonUtilsTest {

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
        createJsonObjectWithValue("A111"),
        createJsonObjectWithValue("A111"));

    Assert.assertTrue(result);
  }

  @Test
  public void equalsShouldReturnFalseIfComparingNotEqualStrings() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonObjectWithValue("A111"),
        createJsonObjectWithValue("A112"));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldReturnFalseIfValuesInNestedArrayAreNotEqual() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonArrayWithValue("A111"),
        createJsonArrayWithValue("A112"));

    Assert.assertFalse(result);
  }

  @Test
  public void equalsShouldReturnTrueIfValuesInNestedArrayAreEqual() {
    boolean result = JsonObjectComparisonUtils.equals(
        createJsonArrayWithValue("A111"),
        createJsonArrayWithValue("A111"));

    Assert.assertTrue(result);
  }

  private JsonObject createJsonArrayWithValue(String value) {
    return Json.createObjectBuilder()
        .add("fieldName", Json.createArrayBuilder().add(value))
        .build();
  }

  private JsonObject createJsonObjectWithValue(String value) {
    return Json.createObjectBuilder().add("fieldName", value).build();
  }

  private JsonObject createJsonObjectWithValue(double value) {
    return Json.createObjectBuilder().add("fieldName", value).build();
  }

  private JsonObject createJsonObjectWithValue(int value) {
    return Json.createObjectBuilder().add("fieldName", value).build();
  }
}