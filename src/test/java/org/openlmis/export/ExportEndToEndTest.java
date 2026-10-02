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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.hamcrest.Matchers;
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

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Autowired
  private DataExporter exporter;

  private File outputDirectory;
  private File mappings;

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
  public void shouldExportEveryEntityThatHasAMapping() throws IOException {
    for (SourceFile source : SourceFile.values()) {
      writeMapping(source, "id,id,DIRECT,,\n");
    }

    exporter.exportData();

    for (SourceFile source : SourceFile.values()) {
      File file = new File(outputDirectory, source.getName() + ".csv");
      assertThat(source.getName() + " was not exported", file.exists(), is(true));
      assertThat(source.getName() + " did not carry the row through",
          read(source.getName() + ".csv"), is("\uFEFFid\nthe-id\n"));
    }
  }

  @Test
  public void shouldWriteNoFileForAnEntityWithoutAMapping() {
    exporter.exportData();

    assertThat(new File(outputDirectory, "Facilities.csv").exists(), is(false));
  }

  private void writeMapping(SourceFile source, String body) throws IOException {
    Files.write(new File(mappings, source.getName() + "_mapping.csv").toPath(),
        ("from,to,type,entityName,defaultValue\n" + body).getBytes(StandardCharsets.UTF_8));
  }

  private String read(String fileName) throws IOException {
    File file = new File(outputDirectory, fileName);
    assertThat(file.getName() + " was not written", file.exists(), Matchers.is(true));
    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
        .replace("\r\n", "\n");
  }
}
