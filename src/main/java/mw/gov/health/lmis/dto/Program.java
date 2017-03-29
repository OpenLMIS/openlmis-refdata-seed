package mw.gov.health.lmis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Program {

  private String code;

  private String name;

  private String description;

  private Boolean active;

  private Boolean periodsSkippable;

  private Boolean showNonFullSupplyTab;
}
