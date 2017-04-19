package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class RightService extends BaseCommunicationService {
  @Override
  protected String getUrl() {
    return "/api/rights";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByName(object.getString(NAME));
  }
}
