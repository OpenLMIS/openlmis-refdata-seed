package mw.gov.health.lmis.converter;

public interface Converter<CsvT, DtoT> {
  DtoT convert(CsvT csv);
}
