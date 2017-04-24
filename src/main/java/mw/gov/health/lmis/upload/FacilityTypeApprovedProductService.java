package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

@Service
public class FacilityTypeApprovedProductService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/facilityTypeApprovedProducts";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    JsonString searchedFacilityTypeCode = object.getJsonObject("facilityType").getJsonString(CODE);
    JsonString searchedProgramCode = object.getJsonObject("program").getJsonString(CODE);
    JsonString searchedProductCode = object.getJsonObject("orderable").getJsonString("productCode");

    JsonArray array = findAll();
    for (int i = 0; i < array.size(); i++) {
      JsonObject next = array.getJsonObject(i);
      JsonString foundFacilityTypeCode = next.getJsonObject("facilityType").getJsonString(CODE);
      JsonString foundProgramCode = next.getJsonObject("program").getJsonSqtring(CODE);
      JsonString foundProductCode = next.getJsonObject("orderable").getJsonString("productCode");

      if (searchedFacilityTypeCode.equals(foundFacilityTypeCode)
          && searchedProgramCode.equals(foundProgramCode)
          && searchedProductCode.equals(foundProductCode)) {
        return next;
      }
    }
    return null;
  }
}
