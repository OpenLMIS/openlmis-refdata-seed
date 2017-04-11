package mw.gov.health.lmis.converter;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.Configuration;
import mw.gov.health.lmis.reader.Reader;
import mw.gov.health.lmis.upload.BaseCommunicationService;
import mw.gov.health.lmis.upload.Services;

import java.io.File;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;

/**
 * Converts Map representation of the CSV files into JSON files.
 */
@Component
public class Converter {

  private static final Logger LOGGER = LoggerFactory.getLogger(Converter.class);

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String ID = "id";

  @Autowired
  private Configuration configuration;

  @Autowired
  private Services services;

  @Autowired
  private Reader reader;

  @Autowired
  private MappingConverter mappingConverter;

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
      String value = getValue(input, mapping);

      switch (mapping.getType()) {
        case "DIRECT":
          jsonBuilder.add(mapping.getTo(), value);
          break;
        case "TO_OBJECT":
          convertToObject(jsonBuilder, mapping, value);
          break;
        case "TO_ID_BY_NAME":
          convertToIdByName(jsonBuilder, mapping, value);
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
        case "TO_ARRAY_FROM_FILE_BY_CODE":
          convertToArrayFromFileByCode(jsonBuilder, mapping, value);
          break;
        case "TO_UUID_BY_CODE":
          convertToIdByCode(jsonBuilder, mapping, value);
          break;
        case "SKIP":
          // fall through
        default:
      }
    }

    return jsonBuilder.build().toString();
  }

  private String getValue(Map<String, String> input, Mapping mapping) {
    String value = input.get(mapping.getFrom());

    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
      value = value.toLowerCase(Locale.ENGLISH);
    }
    
    return value;
  }

  private void convertToIdByName(JsonObjectBuilder jsonBuilder, Mapping mapping, String value) {
    BaseCommunicationService service = services.getService(mapping.getEntityName());
    JsonObject jsonRepresentation = service.findBy(NAME, value);
    if (jsonRepresentation != null) {
      JsonString instanceId = jsonRepresentation.getJsonString(ID);
      jsonBuilder.add(mapping.getTo(), instanceId);
    } else {
      LOGGER.warn("The CSV file contained reference to entity " + mapping.getEntityName()
          + " with name " + value + " but such reference does not exist.");
    }
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
    JsonObjectBuilder builder = Json.createObjectBuilder();
    for (String entry : entries) {
      List<String> keyValue = Lists.newArrayList(StringUtils.split(entry, ':'));
      if (keyValue.size() == 2) {
        builder.add(keyValue.get(0), keyValue.get(1));
      } else {
        LOGGER.warn("Invalid map entry representation: {}. Desired format is \"<key>:<value>\".",
            entry);
      }
    }
    jsonBuilder.add(mapping.getTo(), builder.build());
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

  private void convertToArrayFromFileByCode(JsonObjectBuilder jsonBuilder, Mapping mapping,
                                            String value) {
    List<String> codes = getArrayValues(value);

    String inputFileName = new File(configuration.getDirectory(), mapping.getEntityName())
        .getAbsolutePath();
    List<Map<String, String>> csvs = reader.readFromFile(inputFileName);
    csvs.removeIf(map -> !codes.contains(map.get(CODE)));

    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    if (!csvs.isEmpty()) {
      String mappingFileName = inputFileName.replace(".csv", "_mapping.csv");
      List<Mapping> mappings = mappingConverter.getMappingForFile(mappingFileName);

      for (Map<String, String> csv : csvs) {
        String json = convert(csv, mappings);

        try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
          arrayBuilder.add(jsonReader.readObject());
        }
      }
    }

    jsonBuilder.add(mapping.getTo(), arrayBuilder);
  }

  private void convertToIdByCode(JsonObjectBuilder jsonBuilder, Mapping mapping, String value) {
    BaseCommunicationService service = services.getService(mapping.getEntityName());
    JsonObject jsonRepresentation = service.findBy(CODE, value);
    if (jsonRepresentation != null) {
      jsonBuilder.add(mapping.getTo(), jsonRepresentation.getString("id"));
    } else {
      LOGGER.warn("The CSV file contained reference to entity " + mapping.getEntityName()
          + " with code " + value + " but such reference does not exist.");
    }
  }

  private List<String> getArrayValues(String value) {
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
