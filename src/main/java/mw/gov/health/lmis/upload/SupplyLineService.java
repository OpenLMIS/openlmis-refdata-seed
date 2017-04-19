package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class SupplyLineService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/supplyLines";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByCode(object.getString(CODE));
  }

}
