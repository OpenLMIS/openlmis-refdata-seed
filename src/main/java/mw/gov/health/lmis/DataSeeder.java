package mw.gov.health.lmis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.converter.Converter;
import mw.gov.health.lmis.converter.Mapping;
import mw.gov.health.lmis.converter.MappingConverter;
import mw.gov.health.lmis.reader.GenericReader;
import mw.gov.health.lmis.upload.BaseCommunicationService;
import mw.gov.health.lmis.upload.Services;
import mw.gov.health.lmis.utils.SourceFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  @Autowired
  private Configuration configuration;

  @Autowired
  private GenericReader reader;

  @Autowired
  private Converter converter;

  @Autowired
  private MappingConverter mappingConverter;

  @Autowired
  private Services services;

  /**
   * Seeds data into OLMIS.
   */
  public void seedData() {
    Arrays
        .stream(SourceFile.values())
        .forEach(this::seedFor);
  }

  private void seedFor(SourceFile source) {
    BaseCommunicationService service = services.getService(source);
    String inputFileName = source.getFullFileName(configuration.getDirectory());
    String mappingFileName = source.getFullMappingFileName(configuration.getDirectory());

    LOGGER.info(" == Seeding {} == ", source.getName());
    LOGGER.info("Using input file: {}", inputFileName);
    LOGGER.info("Using mapping file: {}", mappingFileName);

    List<Map<String, String>> csvs = reader.readFromFile(inputFileName);
    List<Mapping> mappings = mappingConverter.getMappingForFile(mappingFileName);
    for (Map<String, String> csv : csvs) {
      String json = converter.convert(csv, mappings);
      LOGGER.info(json);
      service.createResource(json);
    }
  }
}
