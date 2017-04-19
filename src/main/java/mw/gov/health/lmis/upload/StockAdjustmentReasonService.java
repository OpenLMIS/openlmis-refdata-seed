package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

@Service
public class StockAdjustmentReasonService extends BaseCommunicationService {

  private static final String PROGRAM = "program";

  @Override
  protected String getUrl() {
    return "/api/stockAdjustmentReasons";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    JsonString searchedName = object.getJsonString(NAME);
    JsonString searchedProgramCode = object.getJsonObject(PROGRAM).getJsonString(CODE);

    JsonArray array = findAll();
    for (int i = 0; i < array.size(); i++) {
      JsonObject next = array.getJsonObject(i);
      JsonString foundName = next.getJsonString(NAME);
      JsonString foundProgramCode = next.getJsonObject(PROGRAM).getJsonString(CODE);

      if (searchedName.equals(foundName) && searchedProgramCode.equals(foundProgramCode)) {
        return next;
      }
    }
    return null;
  }
}
