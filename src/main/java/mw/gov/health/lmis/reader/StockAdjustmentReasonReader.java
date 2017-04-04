package mw.gov.health.lmis.reader;

import org.springframework.stereotype.Component;

import mw.gov.health.lmis.utils.FileNames;

@Component
public class StockAdjustmentReasonReader extends GenericReader {

  @Override
  public String getFileName() {
    return FileNames.STOCK_ADJUSTMENT_REASONS_CSV;
  }
}
