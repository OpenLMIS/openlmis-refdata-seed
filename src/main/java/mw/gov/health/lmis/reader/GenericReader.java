package mw.gov.health.lmis.reader;

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
   * @param fileName the name of file with data.
   * @return List of map entries
   */
  @Override
  public List<Map<String, String>> readFromFile(String fileName) {
    try {
      File file = new File(fileName);
      
      CsvMapper mapper = new CsvMapper();
      CsvSchema schema = CsvSchema.emptySchema().withHeader();
      MappingIterator<Map<String, String>> iterator = mapper
          .readerFor(Map.class)
          .with(schema)
          .readValues(file);

      return iterator.readAll(Lists.newArrayList());
    } catch (IOException ex) {
      LOGGER.warn("The file with name " + fileName + " does not exist", ex);
      return Lists.newArrayList();
    }
  }
}
