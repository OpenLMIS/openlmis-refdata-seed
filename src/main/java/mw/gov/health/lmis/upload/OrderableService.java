package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class OrderableService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/orderables";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findBy("productCode", object.getString("productCode"));
  }
}
