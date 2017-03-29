package mw.gov.health.lmis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.csv.ProgramCsv;
import mw.gov.health.lmis.reader.ProgramReader;

import java.util.List;

@Component
public class DataSeeder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  @Autowired
  private ProgramReader programReader;

  public void seedData() {
    LOGGER.info("Seeding Programs");
    List<ProgramCsv> programCsvs = programReader.readFromFile();
  }
}
