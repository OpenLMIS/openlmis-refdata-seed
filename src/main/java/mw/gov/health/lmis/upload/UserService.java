package mw.gov.health.lmis.upload;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

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
}
