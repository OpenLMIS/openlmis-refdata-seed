package mw.gov.health.lmis.converter;

import org.springframework.stereotype.Component;

import javax.json.JsonObjectBuilder;

@Component
class DefaultTypeConverter extends BaseTypeConverter {

  @Override
  public boolean supports(String type) {
    return "USE_DEFAULT".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    String defaultValue = mapping.getDefaultValue();

    if (null == defaultValue) {
      logger.warn("Null value for field: {}", mapping.getTo());
    } else {
      builder.add(mapping.getTo(), defaultValue);
    }
  }

}
