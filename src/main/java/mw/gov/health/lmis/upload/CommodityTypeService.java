package mw.gov.health.lmis.upload;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class CommodityTypeService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/commodityTypes";
  }

  @Override
  public HttpMethod getCreateMethod() {
    return HttpMethod.PUT;
  }

}
