package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class ProcessingScheduleService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/processingSchedules";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByCode(object.getString(CODE));
  }
  
}
