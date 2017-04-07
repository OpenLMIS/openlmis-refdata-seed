package mw.gov.health.lmis.converter;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.upload.BaseCommunicationService;
import mw.gov.health.lmis.upload.Services;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Converts Map representation of the CSV files into JSON files.
 */
@Component
public class Converter {

  private static final Logger LOGGER = LoggerFactory.getLogger(Converter.class);

  private static final String CODE = "code";
  private static final String NAME = "name";

  @Autowired
  private Services services;

  /**
   * Converts CSV map representation into JSON strings.
   *
   * @param input    the CSV input as a map
   * @param mappings the mapping specifiations
   * @return JSON string to insert into OLMIS
   */
  public String convert(Map<String, String> input, List<Mapping> mappings) {
    JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
    for (Mapping mapping : mappings) {
      String value = input.get(mapping.getFrom());


      switch (mapping.getType()) {
        case "TO_OBJECT":
          convertToObject(jsonBuilder, mapping, value);
          break;
        case "TO_OBJECT_BY_CODE":
          convertToObjectByCode(jsonBuilder, mapping, value);
          break;
        case "TO_ARRAY_BY_NAME":
          convertToArrayBy(jsonBuilder, mapping, value, NAME);
          break;
        case "TO_ARRAY_BY_CODE":
          convertToArrayBy(jsonBuilder, mapping, value, CODE);
          break;
        case "DIRECT":
        default:
          jsonBuilder.add(mapping.getTo(), value);
          break;
      }
    }

    return jsonBuilder.build().toString();
  }


  private void convertToArrayBy(JsonObjectBuilder jsonBuilder, Mapping mapping, String value,
                                String by) {
    BaseCommunicationService service = services.getService(mapping.getEntityName());
    List<String> values = getArrayValues(value);
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (String v : values) {
      arrayBuilder.add(service.findBy(by, v));
    }
    jsonBuilder.add(mapping.getTo(), arrayBuilder);
  }

  private void convertToObject(JsonObjectBuilder jsonBuilder, Mapping mapping, String value) {
    List<String> entries = Lists.newArrayList(StringUtils.split(value, ','));
    for (String entry : entries) {
      List<String> keyValue = Lists.newArrayList(StringUtils.split(entry, ':'));
      if (keyValue.size() == 2) {
        JsonObject object =
            Json.createObjectBuilder().add(keyValue.get(0), keyValue.get(1)).build();
        jsonBuilder.add(mapping.getTo(), object);
      } else {
        LOGGER.warn("Invalid map entry representation: {}. Desired format is \"<key>:<value>\".",
            entry);
      }
    }
  }

  private void convertToObjectByCode(JsonObjectBuilder jsonBuilder, Mapping mapping, String value) {
    BaseCommunicationService service = services.getService(mapping.getEntityName());
    JsonObject jsonRepresentation = service.findBy(CODE, value);
    if (jsonRepresentation != null) {
      jsonBuilder.add(mapping.getTo(), jsonRepresentation);
    } else {
      LOGGER.warn("The CSV file contained reference to entity " + mapping.getEntityName()
          + " with code " + value + " but such reference does not exist.");
    }
  }

  private List<String> getArrayValues(String value) {
    String rawValues = StringUtils.substringBetween(value, "[", "]");
    if (StringUtils.isNotBlank(rawValues)) {
      return Lists.newArrayList(StringUtils.split(rawValues, ','));
    } else {
      return Collections.EMPTY_LIST;
    }
  }
}
