package mw.gov.health.lmis.upload;

import static mw.gov.health.lmis.utils.SourceFile.COMMODITY_TYPES;
import static mw.gov.health.lmis.utils.SourceFile.FACILITIES;
import static mw.gov.health.lmis.utils.SourceFile.FACILITY_OPERATORS;
import static mw.gov.health.lmis.utils.SourceFile.FACILITY_TYPES;
import static mw.gov.health.lmis.utils.SourceFile.GEOGRAPHIC_LEVELS;
import static mw.gov.health.lmis.utils.SourceFile.GEOGRAPHIC_ZONES;
import static mw.gov.health.lmis.utils.SourceFile.ORDERABLE_DISPLAY_CATEGORIES;
import static mw.gov.health.lmis.utils.SourceFile.PROCESSING_PERIOD;
import static mw.gov.health.lmis.utils.SourceFile.PROCESSING_SCHEDULE;
import static mw.gov.health.lmis.utils.SourceFile.PROGRAMS;
import static mw.gov.health.lmis.utils.SourceFile.REQUISITION_GROUP;
import static mw.gov.health.lmis.utils.SourceFile.ROLES;
import static mw.gov.health.lmis.utils.SourceFile.STOCK_ADJUSTMENT_REASONS;
import static mw.gov.health.lmis.utils.SourceFile.SUPERVISORY_NODES;
import static mw.gov.health.lmis.utils.SourceFile.SUPPLY_LINE;
import static mw.gov.health.lmis.utils.SourceFile.USERS;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;

import mw.gov.health.lmis.utils.SourceFile;

import java.util.Arrays;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServicesTest.Config.class)
public class ServicesTest {
  private static final Map<SourceFile, Class> MAP;

  static {
    MAP = ImmutableMap
        .<SourceFile, Class>builder()
        .put(PROGRAMS, ProgramService.class)
        .put(STOCK_ADJUSTMENT_REASONS, StockAdjustmentReasonService.class)
        .put(ORDERABLE_DISPLAY_CATEGORIES, OrderableDisplayCategoryService.class)
        .put(FACILITY_TYPES, FacilityTypeService.class)
        .put(COMMODITY_TYPES, CommodityTypeService.class)
        .put(PROCESSING_SCHEDULE, ProcessingScheduleService.class)
        .put(PROCESSING_PERIOD, ProcessingPeriodService.class)
        .put(FACILITY_OPERATORS, FacilityOperatorService.class)
        .put(GEOGRAPHIC_LEVELS, GeographicLevelService.class)
        .put(GEOGRAPHIC_ZONES, GeographicZoneService.class)
        .put(FACILITIES, FacilityService.class)
        .put(SUPERVISORY_NODES, SupervisoryNodeService.class)
        .put(REQUISITION_GROUP, RequisitionGroupService.class)
        .put(SUPPLY_LINE, SupplyLineService.class)
        .put(ROLES, RoleService.class)
        .put(USERS, UserService.class)
        .build();
  }

  @Configuration
  @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = CommandLineRunner.class))
  @EnableAutoConfiguration
  static class Config {

    @Bean
    public mw.gov.health.lmis.Configuration configuration() {
      return new mw.gov.health.lmis.Configuration();
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
  }

  private void assertServiceType(String name, Class clazz) {
    assertThat(services.getService(name), instanceOf(clazz));
  }

  private void assertServiceType(SourceFile source, Class clazz) {
    assertThat("Add entry in static block to fix this assert", clazz, is(notNullValue()));
    assertThat(services.getService(source), instanceOf(clazz));
  }

}
