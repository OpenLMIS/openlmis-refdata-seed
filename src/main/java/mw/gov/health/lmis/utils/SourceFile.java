package mw.gov.health.lmis.utils;

import lombok.Getter;

import java.nio.file.Paths;

public enum SourceFile {
  PROGRAMS("Programs"),
  STOCK_ADJUSTMENT_REASONS("StockAdjustmentReasons"),
  ORDERABLE_DISPLAY_CATEGORIES("OrderableDisplayCategory", "OrderableDisplayCategories"),
  FACILITY_TYPES("FacilityTypes"),
  COMMODITY_TYPES("CommodityTypes"),
  PROCESSING_SCHEDULE("ProcessingSchedules"),
  PROCESSING_PERIOD("ProcessingPeriods"),
  FACILITY_OPERATORS("FacilityOperators"),
  GEOGRAPHIC_LEVELS("GeographicLevels"),
  GEOGRAPHIC_ZONES("GeographicZones"),
  FACILITIES("Facility", "Facilities"),
  SUPERVISORY_NODES("SupervisoryNodes"),
  REQUISITION_GROUP("RequisitionGroups"),
  SUPPLY_LINE("SupplyLines"),
  ROLES("Roles"),
  USERS("Users"),
  AUTH_USERS("AuthUsers");

  @Getter
  private final String name;
  @Getter
  private final String singularName;
  private final String fileName;
  private final String mappingFileName;

  SourceFile(String entitySingular, String entityPlural) {
    name = entityPlural;
    singularName = entitySingular;
    fileName = entityPlural + ".csv";
    mappingFileName = entityPlural + "_mapping.csv";
  }

  SourceFile(String entity) {
    this(entity.substring(0, entity.length() - 1), entity);
  }

  public String getFullFileName(String dir) {
    return Paths.get(dir, fileName).toString();
  }

  public String getFullMappingFileName(String dir) {
    return Paths.get(dir, mappingFileName).toString();
  }

}
