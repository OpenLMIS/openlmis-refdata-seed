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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.runner.RunWith;
import org.openlmis.upload.AuthService;
import org.openlmis.upload.BaseCommunicationService;
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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
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

  protected void givenAnApiThatIsStubbed() {
    when(authService.obtainAccessToken()).thenReturn(TOKEN);
    configuration.setProperty("host", HOST);

    RestTemplate restTemplate = new RestTemplate();
    server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    for (Map.Entry<String, BaseCommunicationService> entry
        : context.getBeansOfType(BaseCommunicationService.class).entrySet()) {
      ReflectionTestUtils.setField(entry.getValue(), "restTemplate", restTemplate);
      entry.getValue().invalidateCache();
    }
  }

  protected void givenEveryReadReturns(String body) {
    server.expect(ExpectedCount.manyTimes(), requestTo(Matchers.startsWith(HOST)))
        .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
  }
}
