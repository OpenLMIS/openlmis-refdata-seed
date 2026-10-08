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
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import org.junit.runner.RunWith;
import org.openlmis.upload.AuthService;
import org.openlmis.upload.BaseCommunicationService;
import org.openlmis.utils.SourceFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Shared end-to-end wiring. {@link AppConfiguration} is excluded because its
 * {@link CommandLineRunner} closes the context on startup; the context is fresh per test because
 * the collector, resolver and allocator keep per-run state.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AbstractEndToEndTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractEndToEndTest {

  protected static final String HOST = "http://olmis.test";
  protected static final String TOKEN = "token";

  @Configuration
  @ComponentScan(basePackages = "org.openlmis",
      excludeFilters = @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          value = {CommandLineRunner.class, AppConfiguration.class}))
  @EnableAutoConfiguration
  static class Config {

    @Bean
    public org.openlmis.Configuration configuration() {
      return new org.openlmis.Configuration();
    }
  }

  @MockBean
  protected AuthService authService;

  @Autowired
  protected ApplicationContext context;

  @Autowired
  protected org.openlmis.Configuration configuration;

  protected MockRestServiceServer server;
  protected File outputDirectory;
  protected File mappings;

  protected void givenAnApiThatIsStubbed() {
    when(authService.obtainAccessToken()).thenReturn(TOKEN);
    configuration.setProperty("host", HOST);

    Collection<BaseCommunicationService> services =
        context.getBeansOfType(BaseCommunicationService.class).values();

    RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils
        .getField(services.iterator().next(), "restTemplate");
    server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    for (BaseCommunicationService service : services) {
      ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
      service.invalidateCache();
    }
  }

  protected void writeMapping(SourceFile source, String body) throws IOException {
    Files.write(new File(mappings, source.getName() + "_mapping.csv").toPath(),
        ("from,to,type,entityName,defaultValue\n" + body).getBytes(StandardCharsets.UTF_8));
  }

  protected String read(String fileName) throws IOException {
    File file = new File(outputDirectory, fileName);
    assertThat(fileName + " was not written", file.exists(), is(true));
    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
        .replace("\r\n", "\n");
  }
}
