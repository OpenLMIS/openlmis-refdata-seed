package mw.gov.health.lmis.reader;

import com.beust.jcommander.internal.Lists;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public abstract class GenericReader<CsvT> implements Reader<CsvT> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericReader.class);

  @Override
  public List<CsvT> readFromFile() {
    try (CSVReader reader = new CSVReader(new FileReader(getFileName()))) {

      HeaderColumnNameMappingStrategy<CsvT> strategy =
          new HeaderColumnNameMappingStrategy<>();
      strategy.setType(getCsvClass());

      CsvToBean<CsvT> csvToBean = new CsvToBean<>();
      List<CsvT> beanList = csvToBean.parse(strategy, reader);

      return beanList;
    } catch (IOException ex) {
      LOGGER.warn("The file " + getFileName() + " does not exist or cannot be read.");
    }

    return Lists.newArrayList();
  }

  public abstract String getFileName();

  public abstract Class getCsvClass();
}