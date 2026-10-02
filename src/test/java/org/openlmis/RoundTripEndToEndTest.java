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

package org.openlmis;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openlmis.export.utils.DataExporter;
import org.openlmis.upload.BaseCommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;

/** Exports an entity then seeds the result back, checking the two directions against each other. */
public class RoundTripEndToEndTest extends AbstractEndToEndTest {

  private static final String MAPPING = "Programs_mapping.csv";
  private static final String PROGRAM = "[{\"id\":\"prog-1\",\"code\":\"MEG\","
      + "\"name\":\"Autres MEG\",\"description\":\"Autres Medicaments\","
      + "\"active\":true,\"periodsSkippable\":true,\"showNonFullSupplyTab\":false,"
      + "\"enableDatePhysicalStockCountCompleted\":false,\"skipAuthorization\":false}]";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Autowired
  private DataExporter exporter;

  @Autowired
  private DataSeeder seeder;

  private File exported;
  private final List<String> sentBodies = new ArrayList<>();

  @Before
  public void setUp() throws IOException, URISyntaxException {
    File mappings = folder.newFolder("mappings");
    exported = folder.newFolder("exported");
    File source = new File(getClass().getResource("/mappings").toURI());
    Files.copy(new File(source, MAPPING).toPath(),
        new File(mappings, MAPPING).toPath(), StandardCopyOption.REPLACE_EXISTING);

    configuration.setProperty("host", HOST);
    configuration.setProperty("directory", mappings.getAbsolutePath());
    configuration.setProperty("outputDirectory", exported.getAbsolutePath());
    configuration.setProperty("updateAllowed", "true");
    configuration.remove("exportOriginalMasterDataDirectory");

    givenAnApiThatIsStubbed();
  }

  @Test
  public void shouldSeedBackEveryValueItJustExported() throws IOException, URISyntaxException {
    givenApiReturns(PROGRAM);
    exporter.exportData();

    JsonObject seeded = seedBackWhatWasExported();

    assertThat(seeded.getString("code"), is("MEG"));
    assertThat(seeded.getString("name"), is("Autres MEG"));
    assertThat(seeded.getString("description"), is("Autres Medicaments"));
    // the one column whose CSV name differs from its JSON name
    assertThat(seeded.getString("periodsSkippable"), is("true"));
  }

  @Test
  public void shouldKeepEveryMappedColumnThroughTheRoundTrip()
      throws IOException, URISyntaxException {
    givenApiReturns(PROGRAM);
    exporter.exportData();

    JsonObject seeded = seedBackWhatWasExported();

    assertThat(seeded.keySet().toString(),
        is("[code, name, description, active, periodsSkippable, showNonFullSupplyTab, "
            + "enableDatePhysicalStockCountCompleted, skipAuthorization]"));
  }

  @Test
  public void shouldNotInventOrDropRowsOnTheWayBack() throws IOException, URISyntaxException {
    givenApiReturns(PROGRAM);
    exporter.exportData();

    seedBackWhatWasExported();

    assertThat(sentBodies.size(), is(1));
  }

  private JsonObject seedBackWhatWasExported() throws IOException, URISyntaxException {
    File source = new File(getClass().getResource("/mappings").toURI());
    Files.copy(new File(source, MAPPING).toPath(),
        new File(exported, MAPPING).toPath(), StandardCopyOption.REPLACE_EXISTING);
    configuration.setProperty("directory", exported.getAbsolutePath());

    server.reset();
    for (BaseCommunicationService service
        : context.getBeansOfType(BaseCommunicationService.class).values()) {
      service.invalidateCache();
    }
    server.expect(ExpectedCount.once(), requestTo(HOST + "/api/programs?access_token=token"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(request -> {
          sentBodies.add(request.getBody().toString());
          return withSuccess("{}", MediaType.APPLICATION_JSON).createResponse(request);
        });
    givenApiReturns("[]");

    seeder.seedData();
    server.verify();

    try (JsonReader reader = Json.createReader(new StringReader(sentBodies.get(0)))) {
      return reader.readObject();
    }
  }

  private void givenApiReturns(String body) {
    server.expect(ExpectedCount.manyTimes(), requestTo(Matchers.startsWith(HOST)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
  }
}
