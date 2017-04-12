package mw.gov.health.lmis.converter;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

abstract class BaseTypeConverter implements TypeConverter {
  final Logger logger = LoggerFactory.getLogger(getClass());

  BaseTypeConverter() {
    Converter.addConverter(this);
  }

  String getBy(String type) {
    if (!type.contains("BY_")) {
      return null;
    }

    return type
        .substring(type.lastIndexOf("BY_") + 3)
        .toLowerCase(Locale.ENGLISH);
  }

  List<String> getArrayValues(String value) {
    // single value
    if (!(value.startsWith("[") && value.endsWith("]"))) {
      return Lists.newArrayList(value);
    }

    String rawValues = StringUtils.substringBetween(value, "[", "]");
    if (StringUtils.isNotBlank(rawValues)) {
      return Lists.newArrayList(StringUtils.split(rawValues, ','));
    } else {
      return Collections.emptyList();
    }
  }

}
