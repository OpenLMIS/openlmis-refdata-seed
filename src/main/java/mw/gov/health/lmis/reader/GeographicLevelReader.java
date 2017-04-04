package mw.gov.health.lmis.reader;

import org.springframework.stereotype.Component;

import mw.gov.health.lmis.utils.FileNames;

@Component
public class GeographicLevelReader extends GenericReader {
  @Override
  public String getFileName() {
    return FileNames.GEOGRAPHIC_LEVELS_CSV;
  }
}
