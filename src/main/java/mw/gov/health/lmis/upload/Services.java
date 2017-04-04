package mw.gov.health.lmis.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Services {

  @Autowired
  private ProgramService programService;

  @Autowired
  private StockAdjustmentReasonService stockAdjustmentReasonService;

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
      default:
        return null;
    }
  }
}
