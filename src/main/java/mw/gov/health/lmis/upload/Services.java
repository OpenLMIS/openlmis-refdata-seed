package mw.gov.health.lmis.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mw.gov.health.lmis.utils.SourceFile;

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

  @Autowired
  private ProcessingScheduleService processingScheduleService;

  @Autowired
  private ProcessingPeriodService processingPeriodService;

  @Autowired
  private SupervisoryNodeService supervisoryNodeService;

  /**
   * Retrieves service by the human readable name.
   *
   * @param name name of the service
   * @return the corresponding service
   */
  public BaseCommunicationService getService(String name) {
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
      case "Processing Schedule":
      case "ProcessingSchedule":
        return processingScheduleService;
      case "Processing Period":
      case "ProcessingPeriod":
        return processingPeriodService;
      case "Supervisory Node":
      case "SupervisoryNode":
        return supervisoryNodeService;
      default:
        return null;
    }
  }

  /**
   * Retrieves service by the source file.
   *
   * @param source source file
   * @return the corresponding service
   */
  public BaseCommunicationService getService(SourceFile source) {
    switch (source) {
      case PROGRAMS:
        return programService;
      case STOCK_ADJUSTMENT_REASONS:
        return stockAdjustmentReasonService;
      case GEOGRAPHIC_LEVELS:
        return geographicLevelService;
      case GEOGRAPHIC_ZONES:
        return geographicZoneService;
      case ROLES:
        return roleService;
      case FACILITY_TYPES:
        return facilityTypeService;
      case FACILITY_OPERATORS:
        return facilityOperatorService;
      case PROCESSING_SCHEDULE:
        return processingScheduleService;
      case PROCESSING_PERIOD:
        return processingPeriodService;
      case SUPERVISORY_NODES:
        return supervisoryNodeService;
      default:
        return null;
    }
  }
}
