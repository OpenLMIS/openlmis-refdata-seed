package mw.gov.health.lmis.converter;

import com.google.common.collect.Lists;

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
   * @param mappingFileName file containing mappings
   * @return mapping spec
   */
  public List<Mapping> getMappingForFile(String mappingFileName) {
    try (CSVReader reader = new CSVReader(new FileReader(new File(mappingFileName)))) {
      HeaderColumnNameMappingStrategy<Mapping> strategy = new HeaderColumnNameMappingStrategy<>();
      strategy.setType(Mapping.class);

      CsvToBean<Mapping> csvToBean = new CsvToBean<>();
      return csvToBean.parse(strategy, reader);
    } catch (IOException ex) {
      LOGGER.warn("The mapping file " + mappingFileName + " does not exist.", ex);
      return Lists.newArrayList();
    }
  }
}
