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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeSet;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openlmis.AbstractEndToEndTest;
import org.openlmis.export.utils.DataExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;

/** Asserts a full export against the real mapping files produces every file, with every column. */
public class ExportStructureEndToEndTest extends AbstractEndToEndTest {

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
      .put("RequisitionGroupProgramSchedules.csv",
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

  private static final Map<String, String> EXPECTED_ROW =
      ImmutableMap.<String, String>builder()
      .put("AuthUsers.csv", "u1,,,,a@b.test")
      .put("EmailDetails.csv", "a@b.test,")
      .put("Facilities.csv", "C1,N1,D1,,,,,,,,,,,[PRG]")
      .put("FacilityOperators.csv", "C1,N1,D1,")
      .put("FacilityTypeApprovedProducts.csv", ",,,,,")
      .put("FacilityTypes.csv", "C1,N1,D1,,")
      .put("GeographicLevels.csv", "C1,N1,")
      .put("GeographicZones.csv", "C1,N1,,")
      .put("Nodes.csv", "C1,True")
      .put("OrderableDisplayCategories.csv", "C1,,")
      .put("Orderables.csv", "P1,D1,,,,,")
      .put("OrganizationNodes.csv", "N1,True")
      .put("Organizations.csv", "N1")
      .put("ProcessingPeriods.csv", "N1,,,D1,")
      .put("ProcessingSchedules.csv", "C1,N1,D1")
      .put("ProgramOrderables.csv", "P1,,,,True,,,")
      .put("Programs.csv", "C1,N1,D1,,,,,")
      .put("RequisitionGroupProgramSchedules.csv", "GEN_64a110aa,,,True,")
      .put("RequisitionGroups.csv", "C1,N1,D1,,,[GEN_64a110aa]")
      .put("RoleAssignments.csv", "GEN_f62,,,,")
      .put("Roles.csv", "N1,D1,[N1]")
      .put("StockCardLineItemReasons.csv", "N1,,,")
      .put("SupervisoryNodes.csv", "C1,,N1,D1,,")
      .put("SupplyLines.csv", ",D1,,")
      .put("SupportedPrograms.csv", "C1,PRG,True,,")
      .put("TradeItems.csv", "P1,")
      .put("UserContactDetails.csv", ",,,a@b.test")
      .put("Users.csv", "u1,,,,,[GEN_f62]")
      .put("ValidDestinations.csv", ",,C1,N1,")
      .put("ValidReasons.csv", ",,,")
      .put("ValidSources.csv", ",,C1,N1,")
      .build();

  /** Carries every field the mappings read, including what makes the child files appear. */
  private static final String ENTITY_JSON = "[{"
      + "\"id\":\"the-id\","
      + "\"code\":\"C1\",\"name\":\"N1\",\"description\":\"D1\","
      + "\"productCode\":\"P1\",\"username\":\"u1\",\"email\":\"a@b.test\","
      + "\"userId\":\"the-id\",\"roleId\":\"role-1\","
      + "\"supportedPrograms\":[{\"code\":\"PRG\",\"supportActive\":true}],"
      + "\"programs\":[{\"programId\":\"prog-1\",\"active\":true}],"
      + "\"identifiers\":{\"tradeItem\":\"trade-1\"},"
      + "\"emailDetails\":{\"email\":\"a@b.test\"},"
      + "\"requisitionGroupProgramSchedules\":[{\"directDelivery\":true}],"
      + "\"roleAssignments\":[{\"userId\":\"the-id\",\"roleId\":\"role-1\"}],"
      + "\"node\":{\"referenceId\":\"the-id\",\"refDataFacility\":true},"
      + "\"rights\":[{\"name\":\"N1\"}]"
      + "}]";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Autowired
  private DataExporter exporter;

  private File outputDirectory;

  @Before
  public void setUp() throws IOException, URISyntaxException {
    outputDirectory = folder.newFolder("out");
    File mappings = new File(getClass().getResource("/mappings").toURI());

    configuration.setProperty("host", HOST);
    configuration.setProperty("directory", mappings.getAbsolutePath());
    configuration.setProperty("outputDirectory", outputDirectory.getAbsolutePath());
    configuration.remove("exportOriginalMasterDataDirectory");

    givenAnApiThatIsStubbed();
    server.expect(ExpectedCount.manyTimes(), requestTo(Matchers.startsWith(HOST)))
        .andRespond(withSuccess(ENTITY_JSON, MediaType.APPLICATION_JSON));
  }

  @Test
  public void shouldProduceExactlyTheFilesAFullExportProduces() {
    exporter.exportData();

    assertThat(new TreeSet<>(asList(outputDirectory.list())).toString(),
        is(new TreeSet<>(EXPECTED.keySet()).toString()));
  }

  @Test
  public void shouldGiveEveryFileTheColumnsTheSeededMasterDataExpects() throws IOException {
    exporter.exportData();

    for (Map.Entry<String, String> entry : EXPECTED.entrySet()) {
      File file = new File(outputDirectory, entry.getKey());
      assertThat(entry.getKey() + " is missing", file.exists(), is(true));
      assertThat(entry.getKey() + " has the wrong columns", header(file), is(entry.getValue()));
    }
  }

  @Test
  public void shouldWriteAtLeastOneRowInEveryFile() throws IOException {
    exporter.exportData();

    for (String fileName : EXPECTED.keySet()) {
      File file = new File(outputDirectory, fileName);
      assertThat(fileName + " has no data rows",
          Files.readAllLines(file.toPath()).size() > 1, is(true));
    }
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
  public void shouldWriteArrayColumnsAsABracketedList() throws IOException {
    exporter.exportData();

    assertThat(row(new File(outputDirectory, "Roles.csv")), Matchers.containsString("[N1]"));
    assertThat(row(new File(outputDirectory, "Facilities.csv")), Matchers.containsString("[PRG]"));
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
