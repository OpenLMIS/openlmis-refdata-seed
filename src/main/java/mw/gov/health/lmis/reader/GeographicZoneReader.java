package mw.gov.health.lmis.reader;

import org.springframework.stereotype.Component;

import mw.gov.health.lmis.utils.FileNames;

@Component
public class GeographicZoneReader extends GenericReader {
  @Override
  public String getEntityName() {
    return FileNames.GEOGRAPHIC_ZONES;
  }
}
