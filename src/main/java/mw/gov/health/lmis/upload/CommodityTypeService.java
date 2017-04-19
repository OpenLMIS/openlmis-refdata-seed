package mw.gov.health.lmis.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class CommodityTypeService extends BaseCommunicationService {

  @Autowired
  private OrderableService orderableService;

  @Override
  protected String getUrl() {
    return "/api/commodityTypes";
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
    return orderableService.findUnique(object);
  }
}
