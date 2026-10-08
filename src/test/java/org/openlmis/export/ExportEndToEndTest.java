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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openlmis.AbstractEndToEndTest;
import org.openlmis.export.utils.DataExporter;
import org.openlmis.utils.SourceFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;

public class ExportEndToEndTest extends AbstractEndToEndTest {

  private static final String PROGRAMS_JSON =
      "[{\"id\":\"the-id\",\"code\":\"MEG\",\"name\":\"Autres MEG\",\"active\":true}]";
  private static final String ACCENTED =
      "Diab\u00e8te - Kinsuka P\u00eacheur"; // e-grave and e-circumflex, as the real data has

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Autowired
  private DataExporter exporter;

  @Before
  public void setUp() throws IOException {
    mappings = folder.newFolder("mappings");
    outputDirectory = folder.newFolder("out");
    writeMapping(SourceFile.PROGRAMS, "code,code,DIRECT,,\nname,name,DIRECT,,\n");

    configuration.setProperty("host", HOST);
    configuration.setProperty("directory", mappings.getAbsolutePath());
    configuration.setProperty("outputDirectory", outputDirectory.getAbsolutePath());

    givenAnApiThatIsStubbed();

    server.expect(ExpectedCount.manyTimes(), requestTo(startsWith(HOST)))
        .andRespond(withSuccess(PROGRAMS_JSON, MediaType.APPLICATION_JSON));
  }

  @Test
  public void shouldExportAnEntityFromTheApiStraightToCsv() throws IOException {
    exporter.exportData();

    assertThat(read("Programs.csv"), is("\uFEFFcode,name\nMEG,Autres MEG\n"));
  }

  @Test
  public void shouldCarryAccentedCharactersFromTheApiIntoTheCsv() throws IOException {
    server.reset();
    server.expect(ExpectedCount.manyTimes(), requestTo(startsWith(HOST)))
        .andRespond(withSuccess("[{\"code\":\"DIA\",\"name\":\"" + ACCENTED + "\"}]",
            MediaType.APPLICATION_JSON));

    exporter.exportData();

    assertThat(read("Programs.csv"), is("\uFEFFcode,name\nDIA," + ACCENTED + "\n"));
  }

  @Test
  public void shouldCreateTheConfiguredOutputDirectoryIncludingItsMissingParents()
      throws IOException {
    outputDirectory = new File(folder.getRoot(), "fresh/export");
    configuration.setProperty("outputDirectory", outputDirectory.getAbsolutePath());

    exporter.exportData();

    assertThat(outputDirectory.isDirectory(), is(true));
    assertThat(read("Programs.csv"), is("\uFEFFcode,name\nMEG,Autres MEG\n"));
  }

}
