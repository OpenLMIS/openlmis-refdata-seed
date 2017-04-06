package mw.gov.health.lmis.reader;

import static mw.gov.health.lmis.utils.FileNames.getFullFileName;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class GenericReader implements Reader {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericReader.class);

  @Autowired
  protected Configuration configuration;

  /**
   * Reads the CSV file and converts it to a collection of map entries. Each map in the
   * collection represents field values of a single CSV line.
   *
   * @param entityName the name of the entity to read
   * @return List of map entries
   */
  public List<Map<String, String>> readFromFile(String entityName) {
    try {
      File file = new File(getFullFileName(configuration.getDirectory(), entityName));
      List<Map<String, String>> response = new LinkedList<>();
      CsvMapper mapper = new CsvMapper();
      CsvSchema schema = CsvSchema.emptySchema().withHeader();
      MappingIterator<Map<String, String>> iterator = mapper.reader(Map.class)
          .with(schema)
          .readValues(file);
      while (iterator.hasNext()) {
        response.add(iterator.next());
      }
      return response;
    } catch (IOException ex) {
      LOGGER.warn("The file with name " + entityName + " does not exist in "
          + configuration.getDirectory());
    }

    return Lists.newArrayList();
  }
}