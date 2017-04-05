package mw.gov.health.lmis.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Services {

  @Autowired
  private ProgramService programService;

  @Autowired
  private StockAdjustmentReasonService stockAdjustmentReasonService;

  @Autowired
  private GeographicLevelService geographicLevelService;

  @Autowired
  private GeographicZoneService geographicZoneService;

  @Autowired
  private RoleService roleService;

  @Autowired
  private RightService rightService;

  /**
   * Retrieves service by the human readable name.
   *
   * @param name name of the service
   * @return the corresponding service
   */
  public BaseCommunicationService getServiceByName(String name) {
    switch (name) {
      case "Program":
      case "program":
        return programService;
      case "StockAdjustmentReason":
      case "Stock Adjustment Reason":
      case "stockadjustmentreason":
      case "stock adjustment reason":
        return stockAdjustmentReasonService;
      case "Geographic Zone":
      case "GeographicZone":
      case "geographic zone":
      case "geographiczone":
        return geographicZoneService;
      case "Geographic Level":
      case "GeographicLevel":
      case "geographic level":
      case "geographiclevel":
        return geographicLevelService;
      case "role":
      case "Role":
        return roleService;
      case "right":
      case "Right":
        return rightService;
      default:
        return null;
    }
  }
}
