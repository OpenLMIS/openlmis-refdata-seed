package mw.gov.health.lmis.converter;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.Configuration;
import mw.gov.health.lmis.reader.Reader;

import java.io.File;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

@Component
class FileArrayTypeConverter extends BaseTypeConverter {

  @Autowired
  private Configuration configuration;

  @Autowired
  private Reader reader;

  @Autowired
  private MappingConverter mappingConverter;

  @Autowired
  private Converter converter;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_ARRAY_FROM_FILE_BY");
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    List<String> codes = getArrayValues(value);
    String by = getBy(mapping.getType());

    String parent = configuration.getDirectory();
    String inputFileName = new File(parent, mapping.getEntityName()).getAbsolutePath();
    List<Map<String, String>> csvs = reader.readFromFile(inputFileName);
    csvs.removeIf(map -> !codes.contains(map.get(by)));

    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    if (!csvs.isEmpty()) {
      String mappingFileName = inputFileName.replace(".csv", "_mapping.csv");
      List<Mapping> mappings = mappingConverter.getMappingForFile(mappingFileName);

      for (Map<String, String> csv : csvs) {
        String json = converter.convert(csv, mappings);

        try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
          arrayBuilder.add(jsonReader.readObject());
        }
      }
    }

    builder.add(mapping.getTo(), arrayBuilder);
  }

}
