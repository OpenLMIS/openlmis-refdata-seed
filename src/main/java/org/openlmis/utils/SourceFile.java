/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.utils;

import lombok.Getter;

import java.nio.file.Paths;

public enum SourceFile {
  PROGRAMS("Programs"),
  STOCK_ADJUSTMENT_REASONS("StockCardLineItemReasons"),
  VALID_REASONS("ValidReasons"),
  VALID_SOURCES("ValidSources"),
  VALID_DESTINATIONS("ValidDestinations"),
  ORDERABLE_DISPLAY_CATEGORIES("OrderableDisplayCategory", "OrderableDisplayCategories"),
  FACILITY_TYPES("FacilityTypes"),
  ORDERABLES("Orderables"),
  FACILITY_TYPE_APPROVED_PRODUCTS("FacilityTypeApprovedProducts"),
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
  AUTH_USERS("AuthUsers"),
  USER_CONTACT_DETAILS("UserContactDetails");

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
