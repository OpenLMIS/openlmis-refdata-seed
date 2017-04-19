package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

import javax.json.JsonObject;

@Service
public class RequisitionGroupService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/requisitionGroups";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findByCode(object.getString(CODE));
  }
}
