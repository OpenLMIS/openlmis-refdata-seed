package mw.gov.health.lmis.converter;

import mw.gov.health.lmis.csv.ProgramCsv;
import mw.gov.health.lmis.dto.Program;

public class ProgramConverter implements Converter<ProgramCsv, Program> {

  @Override
  public Program convert(ProgramCsv programCsv) {
    Program program = new Program();
    program.setCode(programCsv.getCode());
    program.setName(programCsv.getName());
    program.setDescription(programCsv.getDescription());
    program.setActive(programCsv.getActive());
    program.setPeriodsSkippable(programCsv.getPeriodSkippable());
    program.setShowNonFullSupplyTab(programCsv.getShowNonFullSupplyTab());

    return program;
  }
}
