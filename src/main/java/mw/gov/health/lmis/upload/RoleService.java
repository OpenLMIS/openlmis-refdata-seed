package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class RoleService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/roles";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByName(object.getString(NAME));
  }
}
