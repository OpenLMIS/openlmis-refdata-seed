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
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openlmis.AbstractEndToEndTest;
import org.openlmis.export.utils.DataExporter;
import org.openlmis.export.utils.MasterDataComparisonReporter;
import org.openlmis.utils.SourceFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;

@SuppressWarnings("PMD.TooManyMethods")
public class ExportCodesEndToEndTest extends AbstractEndToEndTest {

  private static final String CHILD = "RequisitionGroupProgramSchedules.csv";
  private static final String PARENT = "RequisitionGroups.csv";
  private static final String ROLE_ASSIGNMENTS = "RoleAssignments.csv";
  private static final String STORE_MANAGER = "STORE_MANAGER";
  private static final String GROUPS_JSON = "[{\"code\":\"RG1\","
      + "\"requisitionGroupProgramSchedules\":[{\"directDelivery\":true}]}]";
  private static final String TWO_GROUPS_SHARING_A_SCHEDULE = "["
      + "{\"code\":\"RG1\","
      + "\"requisitionGroupProgramSchedules\":[{\"directDelivery\":true}]},"
      + "{\"code\":\"RG2\","
      + "\"requisitionGroupProgramSchedules\":[{\"directDelivery\":true}]}]";
  private static final String USERS_JSON = "["
      + "{\"id\":\"u-1\",\"userId\":\"u-1\",\"username\":\"u1\",\"code\":\"RG1\","
      + "\"roleName\":\"" + STORE_MANAGER + "\","
      + "\"requisitionGroupProgramSchedules\":[{\"directDelivery\":true}]},"
      + "{\"id\":\"u-2\",\"userId\":\"u-1\",\"username\":\"u2\",\"code\":\"RG1\","
      + "\"roleName\":\"STOCK_MANAGER\","
      + "\"requisitionGroupProgramSchedules\":[{\"directDelivery\":true}]}]";
  private static final String BASELINE_HEADER = "code,directDelivery\n";
  private static final String ORIGINAL = "original";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Autowired
  private DataExporter exporter;

  @Autowired
  private MasterDataComparisonReporter reporter;

  @Before
  public void setUp() throws IOException {
    mappings = folder.newFolder("mappings");
    outputDirectory = folder.newFolder("out");

    writeMapping(SourceFile.REQUISITION_GROUP, "code,code,DIRECT,,\n"
        + "requisitionGroupProgramSchedules,requisitionGroupProgramSchedules,"
        + "TO_ARRAY_FROM_FILE_BY_CODE," + CHILD + ",\n");
    Files.write(new File(mappings, "RequisitionGroupProgramSchedules_mapping.csv").toPath(),
        ("from,to,type,entityName,defaultValue\ncode,,SKIP,,\n"
            + "directDelivery,directDelivery,DIRECT,,\n").getBytes(StandardCharsets.UTF_8));

    configuration.setProperty("host", HOST);
    configuration.setProperty("directory", mappings.getAbsolutePath());
    configuration.setProperty("outputDirectory", outputDirectory.getAbsolutePath());
    configuration.remove("exportOriginalMasterDataDirectory");

    givenAnApiThatIsStubbed();
    givenApiReturns(GROUPS_JSON);
  }

  @Test
  public void shouldGenerateACodeWhenNoOriginalMasterDataIsAttached() throws IOException {
    exporter.exportData();

    assertThat(childCode(), startsWith("GEN_"));
    assertThat(read(PARENT), containsString("[GEN_"));
  }

  @Test
  public void shouldReuseTheCodeTheOriginalMasterDataGaveTheSameRow() throws IOException {
    givenOriginalMasterData("RGPS-1,True\n");

    exporter.exportData();

    assertThat(childCode(), is("RGPS-1"));
    assertThat(read(PARENT), containsString("[RGPS-1]"));
  }

  @Test
  public void shouldContinueTheSequenceForAChildRowTheOriginalMasterDataDoesNotHave()
      throws IOException {
    givenOriginalMasterData("RGPS-1,False\n");

    exporter.exportData();

    assertThat(childCode(), is("RGPS-2"));
    assertThat(read(PARENT), containsString("[RGPS-2]"));
  }

  @Test
  public void shouldReportTheRowThatReusedItsOriginalCodeAsUnchanged() throws IOException {
    givenOriginalMasterData("RGPS-1,True\n");

    exporter.exportData();

    assertThat(lineFor(CHILD), is(CHILD + " 1 0 0 0"));
  }

  @Test
  public void shouldWriteOneChildRowForTheRowSeveralParentsAllProduce() throws IOException {
    server.reset();
    givenApiReturns(TWO_GROUPS_SHARING_A_SCHEDULE);
    givenOriginalMasterData("RGPS-1,True\n");

    exporter.exportData();

    assertThat(codesIn(CHILD), is(singletonList("RGPS-1")));
    assertThat(dataRowsIn(PARENT), is(asList("RG1,[RGPS-1]", "RG2,[RGPS-1]")));
  }

  @Test
  public void shouldResolveTheCodesOfEachChildFileAgainstThatFileAlone() throws IOException {
    givenUsersAreExportedToo();
    givenOriginalMasterData(CHILD, BASELINE_HEADER, "GR-PS-A,False\n");
    givenOriginalMasterData(ROLE_ASSIGNMENTS, "code,roleName\n", "RA-1," + STORE_MANAGER + "\n");

    exporter.exportData();

    assertThat(childCode(), startsWith("GEN_"));
    assertThat(codesIn(ROLE_ASSIGNMENTS), is(asList("RA-1", "RA-2")));
  }

  private void givenUsersAreExportedToo() throws IOException {
    writeMapping(SourceFile.USERS, "username,username,DIRECT,,\n"
        + "roleAssignments,roleAssignments,TO_ARRAY_FROM_FILE_BY_CODE," + ROLE_ASSIGNMENTS + ",\n");
    Files.write(new File(mappings, "RoleAssignments_mapping.csv").toPath(),
        ("from,to,type,entityName,defaultValue\ncode,,SKIP,,\n"
            + "roleName,roleName,DIRECT,,\n").getBytes(StandardCharsets.UTF_8));
    server.reset();
    givenApiReturns(USERS_JSON);
  }

  private void givenApiReturns(String body) {
    server.expect(ExpectedCount.manyTimes(), requestTo(startsWith(HOST)))
        .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
  }

  private void givenOriginalMasterData(String childRows) throws IOException {
    givenOriginalMasterData(CHILD, BASELINE_HEADER, childRows);
  }

  private void givenOriginalMasterData(String fileName, String header, String rows)
      throws IOException {
    File baseline = new File(folder.getRoot(), ORIGINAL);
    assertThat("could not create " + baseline, baseline.isDirectory() || baseline.mkdir(),
        is(true));
    Files.write(new File(baseline, fileName).toPath(),
        (header + rows).getBytes(StandardCharsets.UTF_8));
    configuration.setProperty("exportOriginalMasterDataDirectory", baseline.getAbsolutePath());
  }

  private String lineFor(String fileName) {
    for (String line : reporter.report().split(System.lineSeparator())) {
      if (line.startsWith(fileName)) {
        return line.trim().replaceAll(" +", " ");
      }
    }
    return null;
  }

  private String childCode() throws IOException {
    return codesIn(CHILD).get(0);
  }

  private List<String> codesIn(String fileName) throws IOException {
    List<String> codes = new ArrayList<>();
    for (String line : dataRowsIn(fileName)) {
      codes.add(line.split(",")[0].trim());
    }
    return codes;
  }

  private List<String> dataRowsIn(String fileName) throws IOException {
    String[] lines = read(fileName).split("\n");
    return new ArrayList<>(asList(lines).subList(1, lines.length));
  }

}
