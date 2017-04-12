package mw.gov.health.lmis.converter;

import javax.json.JsonObjectBuilder;

public interface TypeConverter {

  boolean supports(String type);

  void convert(JsonObjectBuilder builder, Mapping mapping, String value);

}
