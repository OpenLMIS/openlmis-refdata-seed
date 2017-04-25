package mw.gov.health.lmis.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class FacilityTypeApprovedProductService extends BaseCommunicationService {

  @Autowired
  private OrderableService orderableService;

  @Override
  protected String getUrl() {
    return "/api/facilityTypeApprovedProducts";
  }

  @Override
  public void before() {
    orderableService.invalidateCache();
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return null;
  }
}
