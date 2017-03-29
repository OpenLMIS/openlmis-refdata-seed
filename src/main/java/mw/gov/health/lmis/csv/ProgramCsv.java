package mw.gov.health.lmis.csv;

import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProgramCsv {

  @CsvBindByName
  private String code;

  @CsvBindByName
  private String name;

  @CsvBindByName
  private String description;

  @CsvBindByName
  private Boolean active;

  @CsvBindByName
  private Boolean periodSkippable;

  @CsvBindByName
  private Boolean showNonFullSupplyTab;
}
