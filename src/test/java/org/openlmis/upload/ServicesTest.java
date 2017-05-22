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

package org.openlmis.upload;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.utils.SourceFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServicesTest.Config.class)
public class ServicesTest {
  private static final Map<SourceFile, Class> MAP;

  static {
    MAP = ImmutableMap
        .<SourceFile, Class>builder()
        .put(SourceFile.PROGRAMS, ProgramService.class)
        .put(SourceFile.STOCK_ADJUSTMENT_REASONS, StockAdjustmentReasonService.class)
        .put(SourceFile.ORDERABLE_DISPLAY_CATEGORIES, OrderableDisplayCategoryService.class)
        .put(SourceFile.FACILITY_TYPES, FacilityTypeService.class)
        .put(SourceFile.COMMODITY_TYPES, CommodityTypeService.class)
        .put(SourceFile.FACILITY_TYPE_APPROVED_PRODUCTS, FacilityTypeApprovedProductService.class)
        .put(SourceFile.PROCESSING_SCHEDULE, ProcessingScheduleService.class)
        .put(SourceFile.PROCESSING_PERIOD, ProcessingPeriodService.class)
        .put(SourceFile.FACILITY_OPERATORS, FacilityOperatorService.class)
        .put(SourceFile.GEOGRAPHIC_LEVELS, GeographicLevelService.class)
        .put(SourceFile.GEOGRAPHIC_ZONES, GeographicZoneService.class)
        .put(SourceFile.FACILITIES, FacilityService.class)
        .put(SourceFile.SUPERVISORY_NODES, SupervisoryNodeService.class)
        .put(SourceFile.REQUISITION_GROUP, RequisitionGroupService.class)
        .put(SourceFile.SUPPLY_LINE, SupplyLineService.class)
        .put(SourceFile.ROLES, RoleService.class)
        .put(SourceFile.USERS, UserService.class)
        .put(SourceFile.AUTH_USERS, AuthUserService.class)
        .build();
  }

  @Configuration
  @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = CommandLineRunner.class))
  @EnableAutoConfiguration
  static class Config {

    @Bean
    public org.openlmis.Configuration configuration() {
      return new org.openlmis.Configuration();
    }

  }

  @Autowired
  private Services services;

  @Test
  public void shouldFindServiceBySourceFile() throws Exception {
    Arrays
        .stream(SourceFile.values())
        .forEach(source -> {
          assertServiceType(source, MAP.get(source));
        });
  }

  @Test
  public void shouldFindServiceByName() throws Exception {
    assertServiceType("Program", ProgramService.class);

    assertServiceType("StockAdjustmentReason", StockAdjustmentReasonService.class);
    assertServiceType("Stock Adjustment Reason", StockAdjustmentReasonService.class);

    assertServiceType("Geographic Zone", GeographicZoneService.class);
    assertServiceType("GeographicZone", GeographicZoneService.class);

    assertServiceType("Geographic Level", GeographicLevelService.class);
    assertServiceType("GeographicLevel", GeographicLevelService.class);

    assertServiceType("Facility", FacilityService.class);

    assertServiceType("Facility Type", FacilityTypeService.class);
    assertServiceType("FacilityType", FacilityTypeService.class);

    assertServiceType("Facility Operator", FacilityOperatorService.class);
    assertServiceType("FacilityOperator", FacilityOperatorService.class);

    assertServiceType("Role", RoleService.class);

    assertServiceType("Right", RightService.class);

    assertServiceType("Processing Schedule", ProcessingScheduleService.class);
    assertServiceType("ProcessingSchedule", ProcessingScheduleService.class);

    assertServiceType("Processing Period", ProcessingPeriodService.class);
    assertServiceType("ProcessingPeriod", ProcessingPeriodService.class);

    assertServiceType("Supervisory Node", SupervisoryNodeService.class);
    assertServiceType("SupervisoryNode", SupervisoryNodeService.class);

    assertServiceType("Orderable", OrderableService.class);
  }

  private void assertServiceType(String name, Class clazz) {
    assertThat(services.getService(name), instanceOf(clazz));
  }

  private void assertServiceType(SourceFile source, Class clazz) {
    assertThat("Add entry in static block to fix this assert", clazz, is(notNullValue()));
    assertThat(services.getService(source), instanceOf(clazz));
  }

}
