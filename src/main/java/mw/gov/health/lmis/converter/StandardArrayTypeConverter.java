package mw.gov.health.lmis.converter;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.upload.BaseCommunicationService;
import mw.gov.health.lmis.upload.Services;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@Component
class StandardArrayTypeConverter extends BaseTypeConverter {

  @Autowired
  private Services services;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_ARRAY_BY");
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    BaseCommunicationService service = services.getService(mapping.getEntityName());

    List<String> values = getArrayValues(value);
    String by = getBy(mapping.getType());

    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    for (String v : values) {
      JsonObject object = service.findBy(by, v);

      if (null == object) {
        logger.warn(
            "The CSV file contained reference to entity {} "
                + "with {} {} but such reference does not exist.",
            mapping.getEntityName(), by, value
        );
      } else {
        arrayBuilder.add(object);
      }
    }

    builder.add(mapping.getTo(), arrayBuilder);
  }

}
