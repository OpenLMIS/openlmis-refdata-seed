package mw.gov.health.lmis.converter;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Component
public class MappingConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MappingConverter.class);

  /**
   * Gets the mapping specification fro mthe given file.
   * @param file file containing mappings
   * @return mapping spec
   */
  public List<Mapping> getMappingForFile(File file) {
    try (CSVReader reader = new CSVReader(new FileReader(file))) {

      HeaderColumnNameMappingStrategy<Mapping> strategy =
          new HeaderColumnNameMappingStrategy<>();
      strategy.setType(Mapping.class);

      CsvToBean<Mapping> csvToBean = new CsvToBean<>();
      List<Mapping> beanList = csvToBean.parse(strategy, reader);

      return beanList;
    } catch (IOException ex) {
      LOGGER.warn("The mapping file " + file.getAbsolutePath() + " does not exist.");
      return null;
    }
  }
}
