package mw.gov.health.lmis.converter;

import org.springframework.stereotype.Component;

import javax.json.JsonObjectBuilder;

@Component
class SkipTypeConverter extends BaseTypeConverter {

  @Override
  public boolean supports(String type) {
    return "SKIP".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    // nothing to do
  }

}
