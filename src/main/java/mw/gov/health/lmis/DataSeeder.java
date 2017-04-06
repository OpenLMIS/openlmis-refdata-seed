package mw.gov.health.lmis;

import static mw.gov.health.lmis.utils.FileNames.FACILITY_OPERATORS;
import static mw.gov.health.lmis.utils.FileNames.FACILITY_TYPES;
import static mw.gov.health.lmis.utils.FileNames.GEOGRAPHIC_LEVELS;
import static mw.gov.health.lmis.utils.FileNames.GEOGRAPHIC_ZONES;
import static mw.gov.health.lmis.utils.FileNames.PROGRAMS;
import static mw.gov.health.lmis.utils.FileNames.ROLES;
import static mw.gov.health.lmis.utils.FileNames.STOCK_ADJUSTMENT_REASONS;
import static mw.gov.health.lmis.utils.FileNames.getFullFileName;
import static mw.gov.health.lmis.utils.FileNames.getFullMappingFileName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.converter.Converter;
import mw.gov.health.lmis.converter.Mapping;
import mw.gov.health.lmis.converter.MappingConverter;
import mw.gov.health.lmis.reader.GenericReader;
import mw.gov.health.lmis.upload.AuthService;
import mw.gov.health.lmis.upload.BaseCommunicationService;
import mw.gov.health.lmis.upload.FacilityOperatorService;
import mw.gov.health.lmis.upload.FacilityTypeService;
import mw.gov.health.lmis.upload.GeographicLevelService;
import mw.gov.health.lmis.upload.GeographicZoneService;
import mw.gov.health.lmis.upload.ProgramService;
import mw.gov.health.lmis.upload.RoleService;
import mw.gov.health.lmis.upload.StockAdjustmentReasonService;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  @Autowired
  private Configuration configuration;

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
  private FacilityTypeService facilityTypeService;

  @Autowired
  private FacilityOperatorService facilityOperatorService;

  @Autowired
  private GenericReader reader;

  @Autowired
  private Converter converter;

  @Autowired
  private MappingConverter mappingConverter;

  @Autowired
  private AuthService authService;

  /**
   * Seeds data into OLMIS.
   */
  public void seedData() {
    seedFor(programService, PROGRAMS);
    seedFor(facilityOperatorService, FACILITY_OPERATORS);
    seedFor(facilityTypeService, FACILITY_TYPES);
    seedFor(geographicLevelService, GEOGRAPHIC_LEVELS);
    seedFor(geographicZoneService, GEOGRAPHIC_ZONES);
    seedFor(stockAdjustmentReasonService, STOCK_ADJUSTMENT_REASONS);
    seedFor(roleService, ROLES);
  }

  private void seedFor(BaseCommunicationService service, String entityName) {
    String inputFileName = getFullFileName(configuration.getDirectory(), entityName);
    String mappingFileName = getFullMappingFileName(configuration.getDirectory(), entityName);

    LOGGER.info(" == Seeding " + entityName + " == ");
    LOGGER.info("Using input file: " + inputFileName);
    LOGGER.info("Using mapping file: " + mappingFileName);

    List<Map<String, String>> csvs = reader.readFromFile(entityName);
    List<Mapping> mappings = mappingConverter.getMappingForFile(new File(mappingFileName));
    for (Map<String, String> csv : csvs) {
      String json = converter.convert(csv, mappings);
      LOGGER.info(json);
      service.createResource(json);
    }
  }
}
