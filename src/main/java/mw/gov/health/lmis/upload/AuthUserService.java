package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class AuthUserService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/users/auth";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByName(object.getString("username"));
  }
}
