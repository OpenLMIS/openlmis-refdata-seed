package mw.gov.health.lmis.reader;

import org.springframework.stereotype.Component;

import mw.gov.health.lmis.utils.FileNames;

@Component
public class ProgramReader extends GenericReader {

  @Override
  public String getEntityName() {
    return FileNames.PROGRAMS;
  }
}
