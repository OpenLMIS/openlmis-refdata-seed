package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

@Service
public class SupplyLineService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/supplyLines";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    JsonString searchedSnCode = object.getJsonObject("supervisoryNode").getJsonString(CODE);
    JsonString searchedProgramCode = object.getJsonObject("program").getJsonString(CODE);
    JsonString searchedFacilityCode = object.getJsonObject("supplyingFacility").getJsonString(CODE);

    JsonArray array = findAll();
    for (int i = 0; i < array.size(); i++) {
      JsonObject next = array.getJsonObject(i);
      JsonString foundSnCode = next.getJsonObject("supervisoryNode").getJsonString(CODE);
      JsonString foundProgramCode = next.getJsonObject("program").getJsonString(CODE);
      JsonString foundFacilityCode = next.getJsonObject("supplyingFacility").getJsonString(CODE);

      if (searchedSnCode.equals(foundSnCode)
          && searchedProgramCode.equals(foundProgramCode)
          && searchedFacilityCode.equals(foundFacilityCode)) {
        return next;
      }
    }
    return null;
  }

}
