package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class GeographicZoneService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/geographicZones";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByCode(object.getString(CODE));
  }
}
