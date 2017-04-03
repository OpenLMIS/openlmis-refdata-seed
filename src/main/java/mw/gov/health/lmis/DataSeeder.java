package mw.gov.health.lmis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.converter.Converter;
import mw.gov.health.lmis.converter.Mapping;
import mw.gov.health.lmis.converter.MappingConverter;
import mw.gov.health.lmis.reader.ProgramReader;
import mw.gov.health.lmis.upload.AuthService;
import mw.gov.health.lmis.upload.ProgramService;
import mw.gov.health.lmis.utils.FileNames;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  @Autowired
  private ProgramReader programReader;

  @Autowired
  private ProgramService programService;

  @Autowired
  private Converter converter;

  @Autowired
  private MappingConverter mappingConverter;

  @Autowired
  private AuthService authService;

  /**
   * Seeds data into OLMIS.
   */
  public void seedData() {
    LOGGER.info("Seeding Programs");
    List<Map<String, String>> programCsvs = programReader.readFromFile();
    List<Mapping> mappings = mappingConverter.getMappingForFile(new File(FileNames
        .PROGRAMS_MAPPING_CSV));

    for (Map<String, String> csv : programCsvs) {
      String json = converter.convert(csv, mappings);
      LOGGER.info(json);
      programService.createResource(json);
    }
  }
}
