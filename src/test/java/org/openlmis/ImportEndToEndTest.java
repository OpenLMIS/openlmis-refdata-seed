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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;

/** Seeds from real mapping files and a CSV on disk, out to the HTTP call the API would receive. */
public class ImportEndToEndTest extends AbstractEndToEndTest {

  private static final String PROGRAMS_URL = HOST + "/api/programs?access_token=token";
  private static final String PROGRAMS_CSV =
      "code,name,description,active,periodSkippable,showNonFullSupplyTab,"
          + "enableDatePhysicalStockCountCompleted,skipAuthorization\n"
          + "MEG,Autres MEG,Autres Medicaments,True,True,False,False,False\n";
  private static final String EXISTING =
      "[{\"id\":\"prog-1\",\"code\":\"MEG\",\"name\":\"Autres MEG\"}]";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Autowired
  private DataSeeder seeder;

  private final List<String> sentBodies = new ArrayList<>();

  @Before
  public void setUp() throws IOException, URISyntaxException {
    File directory = folder.newFolder("master-data");
    File mappings = new File(getClass().getResource("/mappings").toURI());
    for (File mapping : mappings.listFiles()) {
      Files.copy(mapping.toPath(), new File(directory, mapping.getName()).toPath(),
          StandardCopyOption.REPLACE_EXISTING);
    }
    Files.write(new File(directory, "Programs.csv").toPath(),
        PROGRAMS_CSV.getBytes(StandardCharsets.UTF_8));

    configuration.setProperty("directory", directory.getAbsolutePath());
    configuration.setProperty("updateAllowed", "true");

    sentBodies.clear();
    givenAnApiThatIsStubbed();
  }

  @Test
  public void shouldPostACsvRowThatDoesNotExistYetAsJson() {
    expectWrite(PROGRAMS_URL, HttpMethod.POST);
    givenEverythingElseReturns("[]");

    seeder.seedData();

    server.verify();
    assertThat(sentBodies.get(0), containsString("\"code\":\"MEG\""));
    assertThat(sentBodies.get(0), containsString("\"name\":\"Autres MEG\""));
    assertThat(sentBodies.get(0), containsString("\"periodsSkippable\":\"true\""));
  }

  /** The CSV is untyped, so booleans cross as quoted strings and the API coerces them. */
  @Test
  public void shouldSendBooleanColumnsAsQuotedStrings() {
    expectWrite(PROGRAMS_URL, HttpMethod.POST);
    givenEverythingElseReturns("[]");

    seeder.seedData();

    assertThat(sentBodies.get(0), containsString("\"active\":\"true\""));
    assertThat(sentBodies.get(0), containsString("\"skipAuthorization\":\"false\""));
  }

  @Test
  public void shouldPutAnExistingRowUnderItsOwnId() {
    expectWrite(HOST + "/api/programs/prog-1?access_token=token", HttpMethod.PUT);
    givenEverythingElseReturns(EXISTING);

    seeder.seedData();

    server.verify();
    assertThat(sentBodies.get(0), containsString("\"id\":\"prog-1\""));
  }

  @Test
  public void shouldLeaveAnExistingRowAloneWhenUpdatesAreDisabled() {
    configuration.setProperty("updateAllowed", "false");
    givenEverythingElseReturns(EXISTING);

    seeder.seedData();

    server.verify();
    assertThat(sentBodies.isEmpty(), is(true));
  }

  private void expectWrite(String url, HttpMethod httpMethod) {
    server.expect(ExpectedCount.once(), requestTo(url))
        .andExpect(method(httpMethod))
        .andRespond(request -> {
          sentBodies.add(request.getBody().toString());
          return withSuccess("{}", MediaType.APPLICATION_JSON).createResponse(request);
        });
  }

  private void givenEverythingElseReturns(String body) {
    server.expect(ExpectedCount.manyTimes(), requestTo(Matchers.startsWith(HOST)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
  }

}
