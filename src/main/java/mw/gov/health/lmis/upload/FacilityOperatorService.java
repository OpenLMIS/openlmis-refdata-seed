package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class FacilityOperatorService extends BaseCommunicationService {
  @Override
  protected String getUrl() {
    return "/api/facilityOperators";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByCode(object.getString(CODE));
  }
}
