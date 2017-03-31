package mw.gov.health.lmis.reader;

import java.util.List;
import java.util.Map;

public interface Reader {

  List<Map<String, String>> readFromFile();
}
