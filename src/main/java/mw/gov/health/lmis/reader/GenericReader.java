package mw.gov.health.lmis.reader;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import mw.gov.health.lmis.Arguments;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class GenericReader implements Reader {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericReader.class);

  @Autowired
  private Arguments arguments;

  @Override
  public List<Map<String, String>> readFromFile() {
    try {
      File file = new File(getFileName());
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
      LOGGER.warn("The file with name " + getFileName() + " does not exist.");
    }

    return Lists.newArrayList();
  }

  public abstract String getFileName();
}