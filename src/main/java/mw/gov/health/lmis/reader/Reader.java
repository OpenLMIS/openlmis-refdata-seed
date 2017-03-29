package mw.gov.health.lmis.reader;

import java.util.List;

public interface Reader<CsvT> {

  List<CsvT> readFromFile();
}
