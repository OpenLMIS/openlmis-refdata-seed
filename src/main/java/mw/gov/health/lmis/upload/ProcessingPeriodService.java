package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

@Service
public class ProcessingPeriodService extends BaseCommunicationService {

  private static final String PROCESSING_SCHEDULE = "processingSchedule";

  @Override
  protected String getUrl() {
    return "/api/processingPeriods";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    JsonString searchedName = object.getJsonString(NAME);
    JsonString searchedScheduleCode = object.getJsonObject(PROCESSING_SCHEDULE).getJsonString(CODE);

    JsonArray array = findAll();
    for (int i = 0; i < array.size(); i++) {
      JsonObject next = array.getJsonObject(i);
      JsonString foundName = next.getJsonString(NAME);
      JsonString foundScheduleCode = next.getJsonObject(PROCESSING_SCHEDULE).getJsonString(CODE);

      if (searchedName.equals(foundName) && searchedScheduleCode.equals(foundScheduleCode)) {
        return next;
      }
    }
    return null;
  }

}
