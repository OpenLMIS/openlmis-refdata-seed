package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class FacilityTypeApprovedProductService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/facilityTypeApprovedProducts";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return null;
  }
}
