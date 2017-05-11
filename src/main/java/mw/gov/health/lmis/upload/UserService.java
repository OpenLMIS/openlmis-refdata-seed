package mw.gov.health.lmis.upload;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class UserService extends BaseCommunicationService {
  @Override
  protected String getUrl() {
    return "/api/users";
  }

  @Override
  public HttpMethod getCreateMethod() {
    return HttpMethod.PUT;
  }

  @Override
  public String buildUpdateUrl(String base, String id) {
    return base;
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByName(object.getString("username"));
  }
}
