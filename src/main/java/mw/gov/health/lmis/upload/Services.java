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

  @Autowired
  private FacilityOperatorService facilityOperatorService;

  @Autowired
  private FacilityTypeService facilityTypeService;

  /**
   * Retrieves service by the human readable name.
   *
   * @param name name of the service
   * @return the corresponding service
   */
  public BaseCommunicationService getServiceByName(String name) {
    switch (name) {
      case "Program":
        return programService;
      case "StockAdjustmentReason":
      case "Stock Adjustment Reason":
        return stockAdjustmentReasonService;
      case "Geographic Zone":
      case "GeographicZone":
        return geographicZoneService;
      case "Geographic Level":
      case "GeographicLevel":
        return geographicLevelService;
      case "Facility Type":
      case "FacilityType":
        return facilityTypeService;
      case "Facility Operator":
      case "FacilityOperator":
        return facilityOperatorService;
      case "Role":
        return roleService;
      case "Right":
        return rightService;
      default:
        return null;
    }
  }
}
