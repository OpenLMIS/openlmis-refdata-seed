package mw.gov.health.lmis.utils;

import lombok.Getter;

import java.nio.file.Paths;

public enum SourceFile {
  PROGRAMS("Programs"),
  STOCK_ADJUSTMENT_REASONS("StockAdjustmentReasons"),
  GEOGRAPHIC_LEVELS("GeographicLevels"),
  GEOGRAPHIC_ZONES("GeographicZones"),
  ROLES("Roles"),
  FACILITY_TYPES("FacilityTypes"),
  FACILITY_OPERATORS("FacilityOperators"),
  PROCESSING_SCHEDULE("ProcessingSchedules"),
  PROCESSING_PERIOD("ProcessingPeriods"),
  SUPERVISORY_NODES("SupervisoryNodes");

  @Getter
  private final String name;
  private final String fileName;
  private final String mappingFileName;

  SourceFile(String entity) {
    name = entity;
    fileName = entity + ".csv";
    mappingFileName = entity + "_mapping.csv";
  }

  public String getFullFileName(String dir) {
    return Paths.get(dir, fileName).toString();
  }

  public String getFullMappingFileName(String dir) {
    return Paths.get(dir, mappingFileName).toString();
  }

}
