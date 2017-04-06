package mw.gov.health.lmis.utils;

import java.nio.file.Paths;

public final class FileNames {

  public static final String CONFIG = "config.properties";

  public static final String MAPPING = "_mapping";
  public static final String CSV = ".csv";

  public static final String PROGRAMS = "Programs";
  public static final String STOCK_ADJUSTMENT_REASONS = "StockAdjustmentReasons";
  public static final String GEOGRAPHIC_LEVELS = "GeographicLevels";
  public static final String GEOGRAPHIC_ZONES = "GeographicZones";
  public static final String ROLES = "Roles";
  public static final String FACILITY_TYPES = "FacilityTypes";
  public static final String FACILITY_OPERATORS = "FacilityOperators";

  public static final String getFullFileName(String dir, String entity) {
    return Paths.get(dir, entity) + CSV;
  }

  public static final String getFullMappingFileName(String dir, String entity) {
    return Paths.get(dir, entity) + MAPPING + CSV;
  }





}
