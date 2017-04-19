package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class OrderableDisplayCategoryService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/orderableDisplayCategories";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByCode(object.getString(CODE));
  }
}
