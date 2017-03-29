package mw.gov.health.lmis.reader;

import org.springframework.stereotype.Component;

import mw.gov.health.lmis.csv.ProgramCsv;
import mw.gov.health.lmis.utils.FileNames;

@Component
public class ProgramReader extends GenericReader<ProgramCsv> {

  @Override
  public String getFileName() {
    return FileNames.PROGRAM_CSV;
  }

  @Override
  public Class getCsvClass() {
    return ProgramCsv.class;
  }
}
