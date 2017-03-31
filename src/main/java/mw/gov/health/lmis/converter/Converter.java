package mw.gov.health.lmis.converter;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

/**
 * Converts Map representation of the CSV files into JSON files.
 */
@Component
public class Converter {

  /**
   * Converts CSV map representation into JSON strings.
   *
   * @param input the CSV input as a map
   * @param mappings the mapping specifiations
   * @return JSON string to insert into OLMIS
   */
  public String convert(Map<String, String> input, List<Mapping> mappings) {
    JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
    for (Mapping mapping : mappings) {
      String value = input.get(mapping.getFrom());

      switch (mapping.getType()) {
        case "DIRECT":
        default:
          jsonBuilder.add(mapping.getTo(), value);
          break;
      }
    }

    return jsonBuilder.build().toString();
  }
}
