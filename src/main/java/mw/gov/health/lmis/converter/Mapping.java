package mw.gov.health.lmis.converter;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
