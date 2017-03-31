package mw.gov.health.lmis.converter;

import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mapping {

  @CsvBindByName
  private String from;
  @CsvBindByName
  private String to;
  @CsvBindByName
  private String type;
  @CsvBindByName
  private String entityName;
  @CsvBindByName
  private String defaultValue;
}
