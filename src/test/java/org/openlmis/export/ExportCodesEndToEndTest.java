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

/** Code reuse and allocation with the original master data attached as the baseline. */
public class ExportCodesEndToEndTest extends AbstractEndToEndTest {

  private static final String CHILD = "RequisitionGroupProgramSchedules.csv";
  private static final String PARENT = "RequisitionGroups.csv";
  private static final String GROUPS_JSON = "[{\"code\":\"RG1\","
      + "\"requisitionGroupProgramSchedules\":[{\"directDelivery\":true}]}]";
  private static final String BASELINE_HEADER = "code,directDelivery\n";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Autowired
  private DataExporter exporter;

  @Autowired
  private MasterDataComparisonReporter reporter;

  private File outputDirectory;
  private File mappings;

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
    server.expect(ExpectedCount.manyTimes(), requestTo(startsWith(HOST)))
        .andRespond(withSuccess(GROUPS_JSON, MediaType.APPLICATION_JSON));
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
  public void shouldGenerateACodeWhenTheOriginalCodesAreNotASequence() throws IOException {
    givenOriginalMasterData("GR-PS-MEG-A,False\n");

    exporter.exportData();

    assertThat(childCode(), startsWith("GEN_"));
  }

  @Test
  public void shouldReportHowTheExportComparesWithTheOriginalMasterData() throws IOException {
    givenOriginalMasterData("RGPS-1,True\n");

    exporter.exportData();

    assertThat(reporter.report(), containsString("RequisitionGroupProgramSchedules.csv"));
  }

  private void givenOriginalMasterData(String childRows) throws IOException {
    File baseline = folder.newFolder("original");
    Files.write(new File(baseline, CHILD).toPath(),
        (BASELINE_HEADER + childRows).getBytes(StandardCharsets.UTF_8));
    configuration.setProperty("exportOriginalMasterDataDirectory", baseline.getAbsolutePath());
  }

  private void writeMapping(SourceFile source, String body) throws IOException {
    Files.write(new File(mappings, source.getName() + "_mapping.csv").toPath(),
        ("from,to,type,entityName,defaultValue\n" + body).getBytes(StandardCharsets.UTF_8));
  }

  private String childCode() throws IOException {
    return read(CHILD).split("\n")[1].split(",")[0].trim();
  }

  private String read(String fileName) throws IOException {
    File file = new File(outputDirectory, fileName);
    assertThat(fileName + " was not written", file.exists(), is(true));
    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
        .replace("\r\n", "\n");
  }
}
