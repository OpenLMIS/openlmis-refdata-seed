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

package org.openlmis.export;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openlmis.AbstractEndToEndTest;
import org.openlmis.export.utils.DataExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;

@SuppressWarnings("PMD.TooManyMethods")
public class ExportStructureEndToEndTest extends AbstractEndToEndTest {

  private static final String SCHEDULES = "RequisitionGroupProgramSchedules.csv";

  private static final List<String> EXPECTED_ENDPOINTS = asList(
      "/api/facilities/full",
      "/api/facilityOperators",
      "/api/facilityTypeApprovedProducts",
      "/api/facilityTypes",
      "/api/geographicLevels",
      "/api/geographicZones",
      "/api/orderableDisplayCategories",
      "/api/orderables",
      "/api/organizations",
      "/api/processingPeriods",
      "/api/processingSchedules",
      "/api/programs",
      "/api/requisitionGroups",
      "/api/roleAssignments",
      "/api/roles",
      "/api/stockCardLineItemReasons",
      "/api/supervisoryNodes",
      "/api/supplyLines",
      "/api/tradeItems",
      "/api/userContactDetails",
      "/api/users",
      "/api/users/auth/batch",
      "/api/validDestinations",
      "/api/validReasons",
      "/api/validSources");

  private static final Map<String, String> EXPECTED = ImmutableMap.<String, String>builder()
      .put("AuthUsers.csv", "username,password,role,enabled,email")
      .put("EmailDetails.csv", "email,username")
      .put("Facilities.csv",
          "code,name,description,geographicZone,facilityType,"
              + "facilityOperator,extraData,active,goLiveDate,goDownDate,"
              + "comment,enabled,openLMISAccessible,supportedPrograms")
      .put("FacilityOperators.csv", "code,name,description,displayorder")
      .put("FacilityTypeApprovedProducts.csv",
          "facilityTypeCode,orderableCode,programCode,maxPeriodsOfStock,"
              + "minPeriodsOfStock,emergencyOrderPoint")
      .put("FacilityTypes.csv", "code,name,description,displayorder,active")
      .put("GeographicLevels.csv", "code,name,levelNumber")
      .put("GeographicZones.csv", "code,name,level,parent")
      .put("Nodes.csv", "referenceId,isRefDataFacility")
      .put("OrderableDisplayCategories.csv", "code,displayName,displayOrder")
      .put("Orderables.csv",
          "productCode,description,name,packRoundingThreshold,packSize,"
              + "roundToZero,dispensable")
      .put("OrganizationNodes.csv", "referenceName,isRefDataFacility")
      .put("Organizations.csv", "name")
      .put("ProcessingPeriods.csv", "name,startDate,endDate,description,processingSchedule")
      .put("ProcessingSchedules.csv", "code,name,description")
      .put("ProgramOrderables.csv",
          "code,program,category,dosesPerPatient,active,fullSupply,"
              + "displayOrder,pricePerPack")
      .put("Programs.csv",
          "code,name,description,active,periodSkippable,"
              + "showNonFullSupplyTab,enableDatePhysicalStockCountCompleted,"
              + "skipAuthorization")
      .put(SCHEDULES,
          "code,program,processingSchedule,directDelivery,dropOffFacility")
      .put("RequisitionGroups.csv",
          "code,name,description,supervisoryNode,memberFacilities,"
              + "requisitionGroupProgramSchedules")
      .put("RoleAssignments.csv", "code,roleName,programCode,supervisoryNodeCode,warehouseCode")
      .put("Roles.csv", "name,description,rights")
      .put("StockCardLineItemReasons.csv", "name,reasonCategory,reasonType,isFreeTextAllowed")
      .put("SupervisoryNodes.csv", "code,facility,name,description,parentNode,childNodes")
      .put("SupplyLines.csv", "supervisoryNode,description,program,supplyingFacility")
      .put("SupportedPrograms.csv", "facilityCode,programCode,active,startDate,locallyFulfilled")
      .put("TradeItems.csv", "productCode,manufacturerOfTradeItem")
      .put("UserContactDetails.csv", "username,phoneNumber,allowNotify,email")
      .put("Users.csv", "username,firstName,lastName,homeFacilityCode,active,roleAssignments")
      .put("ValidDestinations.csv",
          "programCode,facilityTypeCode,destination,organizationName,"
              + "geoLevelAffinity")
      .put("ValidReasons.csv", "program,facilityType,reason,hidden")
      .put("ValidSources.csv",
          "programCode,facilityTypeCode,facilityCode,organizationName,"
              + "geoLevelAffinity")
      .build();

  private static final String DIRECT_DELIVERY_SCHEDULE = "GEN_64a110aa";
  private static final String PICKED_UP_SCHEDULE = "GEN_16a9b71f";

  // RequisitionGroupProgramSchedules holds two rows so the array column really joins values
  private static final Map<String, String> EXPECTED_ROW =
      ImmutableMap.<String, String>builder()
      .put("AuthUsers.csv", "u1,,,,a@b.test")
      .put("EmailDetails.csv", "a@b.test,")
      .put("Facilities.csv", "C1,N1,D1,,FT-WH,OP1,region:Kinshasa,,2026-01-01,,,,,[PRG]")
      .put("FacilityOperators.csv", "C1,N1,D1,")
      .put("FacilityTypeApprovedProducts.csv", "FT1,ORD1,PRG1,,,")
      .put("FacilityTypes.csv", "C1,N1,D1,,")
      .put("GeographicLevels.csv", "C1,N1,")
      .put("GeographicZones.csv", "C1,N1,,")
      .put("Nodes.csv", "C1,True")
      .put("OrderableDisplayCategories.csv", "C1,,")
      .put("Orderables.csv", "P1,D1,,,,,dispensingUnit:each")
      .put("OrganizationNodes.csv", "N1,True")
      .put("Organizations.csv", "N1")
      .put("ProcessingPeriods.csv", "N1,,,D1,")
      .put("ProcessingSchedules.csv", "C1,N1,D1")
      .put("ProgramOrderables.csv", "P1,,,,True,,,")
      .put("Programs.csv", "C1,N1,D1,,,,,")
      .put(SCHEDULES, PICKED_UP_SCHEDULE + ",,,False,")
      .put("RequisitionGroups.csv",
          "C1,N1,D1,,,\"[" + DIRECT_DELIVERY_SCHEDULE + "," + PICKED_UP_SCHEDULE
              + "]\"")
      .put("RoleAssignments.csv", "GEN_f62,,,,")
      .put("Roles.csv", "N1,D1,[N1]")
      .put("StockCardLineItemReasons.csv", "N1,,,")
      .put("SupervisoryNodes.csv", "C1,,N1,D1,,")
      .put("SupplyLines.csv", ",D1,PRG1,")
      .put("SupportedPrograms.csv", "C1,PRG,True,2026-02-02,")
      .put("TradeItems.csv", "P1,")
      .put("UserContactDetails.csv", ",,,a@b.test")
      .put("Users.csv", "u1,,,,,[GEN_f62]")
      .put("ValidDestinations.csv", ",,C1,N1,")
      .put("ValidReasons.csv", "PRG1,FT1,R1,")
      .put("ValidSources.csv", ",,C1,N1,")
      .build();

  // the reference objects, flat objects and dates are what make TO_OBJECT_BY_*, TO_OBJECT
  // and DIRECT_DATE produce a value at all; without them whole files export as blank rows
  private static final String ENTITY_JSON = "[{"
      + "\"id\":\"the-id\","
      + "\"code\":\"C1\",\"name\":\"N1\",\"description\":\"D1\","
      + "\"productCode\":\"P1\",\"username\":\"u1\",\"email\":\"a@b.test\","
      + "\"userId\":\"the-id\",\"roleId\":\"role-1\","
      + "\"type\":{\"code\":\"FT-WH\"},\"operator\":{\"code\":\"OP1\"},"
      + "\"facilityType\":{\"code\":\"FT1\"},"
      + "\"orderable\":{\"productCode\":\"ORD1\"},"
      + "\"program\":{\"code\":\"PRG1\"},\"reason\":{\"name\":\"R1\"},"
      + "\"goLiveDate\":\"2026-01-01\","
      + "\"extraData\":{\"region\":\"Kinshasa\"},"
      + "\"dispensable\":{\"dispensingUnit\":\"each\"},"
      + "\"supportedPrograms\":[{\"code\":\"PRG\",\"supportActive\":true,"
      + "\"supportStartDate\":\"2026-02-02\"}],"
      + "\"programs\":[{\"programId\":\"prog-1\",\"active\":true}],"
      + "\"identifiers\":{\"tradeItem\":\"trade-1\"},"
      + "\"emailDetails\":{\"email\":\"a@b.test\"},"
      + "\"requisitionGroupProgramSchedules\":[{\"directDelivery\":true},"
      + "{\"directDelivery\":false}],"
      + "\"roleAssignments\":[{\"userId\":\"the-id\",\"roleId\":\"role-1\"}],"
      + "\"node\":{\"referenceId\":\"the-id\",\"refDataFacility\":true},"
      + "\"rights\":[{\"name\":\"N1\"}]"
      + "}]";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Autowired
  private DataExporter exporter;

  private final List<String> requestedUris = new ArrayList<>();

  @Before
  public void setUp() throws IOException, URISyntaxException {
    outputDirectory = folder.newFolder("out");
    File mappings = new File(getClass().getResource("/mappings").toURI());

    configuration.setProperty("host", HOST);
    configuration.setProperty("directory", mappings.getAbsolutePath());
    configuration.setProperty("outputDirectory", outputDirectory.getAbsolutePath());
    configuration.remove("exportOriginalMasterDataDirectory");

    givenAnApiThatIsStubbed();
    server.expect(ExpectedCount.manyTimes(), requestTo(startsWith(HOST)))
        .andRespond(request -> {
          requestedUris.add(request.getURI().toString());
          return withSuccess(ENTITY_JSON, MediaType.APPLICATION_JSON).createResponse(request);
        });
  }

  @Test
  public void shouldProduceExactlyTheFilesAFullExportProduces() {
    exporter.exportData();

    assertThat(new TreeSet<>(asList(outputDirectory.list())).toString(),
        is(new TreeSet<>(EXPECTED.keySet()).toString()));
  }

  @Test
  public void shouldGiveEveryFileItsExpectedColumns() throws IOException {
    exporter.exportData();

    for (Map.Entry<String, String> entry : EXPECTED.entrySet()) {
      File file = new File(outputDirectory, entry.getKey());
      assertThat(entry.getKey() + " is missing", file.exists(), is(true));
      assertThat(entry.getKey() + " has the wrong columns", header(file), is(entry.getValue()));
    }
  }

  @Test
  public void shouldWriteOneRowPerEntityAndOneChildRowPerArrayMember()
      throws IOException {
    exporter.exportData();

    for (String fileName : EXPECTED.keySet()) {
      File file = new File(outputDirectory, fileName);
      int expected = SCHEDULES.equals(fileName) ? 2 : 1;
      assertThat(fileName + " wrote the wrong number of rows",
          Files.readAllLines(file.toPath()).size() - 1, is(expected));
    }

    assertThat(joinColumnOf(SCHEDULES),
        containsInAnyOrder(DIRECT_DELIVERY_SCHEDULE, PICKED_UP_SCHEDULE));
  }

  @Test
  public void shouldPutEveryValueInTheColumnItBelongsTo() throws IOException {
    exporter.exportData();

    for (Map.Entry<String, String> entry : EXPECTED_ROW.entrySet()) {
      assertThat(entry.getKey() + " wrote the wrong row",
          row(new File(outputDirectory, entry.getKey())), is(entry.getValue()));
    }
  }

  @Test
  public void shouldFetchEachEntityFromItsExpectedEndpoint() {
    exporter.exportData();

    assertThat(pathsRequested().toString(), is(new TreeSet<>(EXPECTED_ENDPOINTS).toString()));
    assertThat(firstRequestUnder("/api/roleAssignments"),
        both(containsString("page=0"))
            .and(containsString("size=5000000")));
  }

  private TreeSet<String> pathsRequested() {
    TreeSet<String> paths = new TreeSet<>();
    for (String uri : requestedUris) {
      String path = uri.substring(HOST.length());
      paths.add(path.contains("?") ? path.substring(0, path.indexOf('?')) : path);
    }
    return paths;
  }

  private String firstRequestUnder(String path) {
    for (String uri : requestedUris) {
      if (uri.startsWith(HOST + path)) {
        return uri;
      }
    }
    return null;
  }

  private List<String> joinColumnOf(String fileName) throws IOException {
    List<String> lines = Files.readAllLines(new File(outputDirectory, fileName).toPath());
    List<String> codes = new ArrayList<>();
    for (String line : lines.subList(1, lines.size())) {
      codes.add(line.split(",")[0]);
    }
    return codes;
  }

  private String row(File file) throws IOException {
    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
        .replace("\uFEFF", "").replace("\r\n", "\n").split("\n")[1];
  }

  private String header(File file) throws IOException {
    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
        .replace("\uFEFF", "").replace("\r\n", "\n").split("\n")[0];
  }
}
