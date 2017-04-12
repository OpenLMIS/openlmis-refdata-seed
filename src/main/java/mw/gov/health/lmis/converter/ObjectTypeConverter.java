package mw.gov.health.lmis.converter;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.upload.BaseCommunicationService;
import mw.gov.health.lmis.upload.Services;

import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@Component
public class ObjectTypeConverter extends BaseTypeConverter {

  @Autowired
  private Services services;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_OBJECT");
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    String by = getBy(mapping.getType());

    if (isBlank(by)) {
      createObject(builder, mapping, value);
    } else {
      findObject(builder, mapping, value, by);
    }
  }

  private void createObject(JsonObjectBuilder jsonBuilder, Mapping mapping, String value) {
    List<String> entries = Lists.newArrayList(StringUtils.split(value, ','));
    JsonObjectBuilder builder = Json.createObjectBuilder();

    for (String entry : entries) {
      List<String> keyValue = Lists.newArrayList(StringUtils.split(entry, ':'));

      if (keyValue.size() == 2) {
        builder.add(keyValue.get(0), keyValue.get(1));
      } else {
        logger.warn(
            "Invalid map entry representation: {}. Desired format is \"<key>:<value>\".", entry
        );
      }
    }

    jsonBuilder.add(mapping.getTo(), builder.build());
  }

  private void findObject(JsonObjectBuilder jsonBuilder, Mapping mapping, String value, String by) {
    BaseCommunicationService service = services.getService(mapping.getEntityName());
    JsonObject jsonRepresentation = service.findBy(by, value);

    if (jsonRepresentation != null) {
      jsonBuilder.add(mapping.getTo(), jsonRepresentation);
    } else {
      logger.warn("The CSV file contained reference to entity " + mapping.getEntityName()
          + " with code " + value + " but such reference does not exist.");
    }
  }

}
