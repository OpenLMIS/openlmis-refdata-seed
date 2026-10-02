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

package org.openlmis.upload;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.openlmis.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestOperations;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class BaseCommunicationServiceTest {

  private static final String HOST = "http://localhost";
  private static final String TOKEN = "token";
  private static final String PROGRAMS = "[{\"code\":\"MEG\",\"name\":\"Autres MEG\"}]";
  private static final String CODE = "code";
  private static final String MEG = "MEG";
  private static final String ID = "abc";

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Mock
  private RestOperations restTemplate;

  @Mock
  private AuthService authService;

  @Mock
  private Configuration configuration;

  private TestService service;

  @Before
  public void setUp() {
    service = new TestService();
    ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(service, "authService", authService);
    ReflectionTestUtils.setField(service, "configuration", configuration);
    when(configuration.getHost()).thenReturn(HOST);
    when(authService.obtainAccessToken()).thenReturn(TOKEN);
  }

  @Test
  public void shouldReturnAnArrayResponseAsIs() {
    givenResponses(PROGRAMS);

    assertThat(service.findAll().size(), is(1));
  }

  @Test
  public void shouldMergeEveryPageOfAPagedResponse() {
    givenResponses(page("[{\"code\":\"MEG\"}]", 3), page("[{\"code\":\"VIH\"}]", 3),
        page("[{\"code\":\"CAN\"}]", 3));

    JsonArray all = service.findAll();

    assertThat(all.size(), is(3));
    assertThat(all.getJsonObject(2).getString(CODE), is("CAN"));
    verify(restTemplate, times(3)).getForEntity(any(URI.class), eq(String.class));
  }

  @Test
  public void shouldFetchOnlyOncePerRunAndRefetchAfterTheCacheIsInvalidated() {
    givenResponses(PROGRAMS);

    service.findAll();
    service.findAll();
    verify(restTemplate, times(1)).getForEntity(any(URI.class), eq(String.class));

    service.invalidateCache();
    service.findAll();
    verify(restTemplate, times(2)).getForEntity(any(URI.class), eq(String.class));
  }

  @Test
  public void shouldExportTheSameResourcesItSeedsFrom() {
    givenResponses(PROGRAMS);

    assertThat(service.findAllForExport(), is(service.findAll()));
  }

  @Test
  public void shouldFindByAFieldIgnoringCase() {
    givenResponses(PROGRAMS);

    assertThat(service.findByCode("meg").getString(CODE), is(MEG));
    assertThat(service.findByName("autres meg"), is(notNullValue()));
  }

  @Test
  public void shouldReturnNothingWhenNoResourceMatches() {
    givenResponses(PROGRAMS);

    assertThat(service.findByCode("NOPE"), is(nullValue()));
  }

  @Test
  public void shouldTreatNotFoundAsNoResultRatherThanAFailure() {
    when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    assertThat(service.findOne("", RequestParameters.init()), is(nullValue()));
  }

  @Test
  public void shouldFailOnAnyOtherErrorStatus() {
    when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

    expected.expect(DataRetrievalException.class);

    service.findOne("", RequestParameters.init());
  }

  @Test
  public void shouldPostANewResourceAndDropTheStaleCache() {
    givenResponses(PROGRAMS);
    service.findAll();

    assertThat(service.createResource("{}"), is(true));

    verify(restTemplate).postForEntity(any(URI.class), any(HttpEntity.class), eq(Object.class));
    service.findAll();
    verify(restTemplate, times(2)).getForEntity(any(URI.class), eq(String.class));
  }

  @Test
  public void shouldReportAFailedCreateRatherThanThrow() {
    when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(Object.class)))
        .thenThrow(new ResourceAccessException("refused"));

    assertThat(service.createResource("{}"), is(false));
  }

  @Test
  public void shouldPutTheUpdatedResourceUnderItsOwnId() {
    assertThat(service.updateResource(Json.createObjectBuilder().add(CODE, MEG).build(), ID),
        is(true));

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(restTemplate).put(uri.capture(), any(HttpEntity.class));
    assertThat(uri.getValue().toString(),
        is(HOST + "/api/programs/abc?access_token=token"));
  }

  @Test
  public void shouldAddTheIdIntoTheBodyItPuts() {
    service.updateResource(Json.createObjectBuilder().add(CODE, MEG).build(), ID);

    assertThat(capturedBody(), is("{\"code\":\"MEG\",\"id\":\"abc\"}"));
  }

  @Test
  public void shouldLeaveTheBodyAloneWhenAServiceAsksItTo() {
    service.updateWithoutId(Json.createObjectBuilder().add(CODE, MEG).build(), ID);

    assertThat(capturedBody(), is("{\"code\":\"MEG\"}"));
  }

  @Test
  public void shouldReportAFailedUpdateRatherThanThrow() {
    doThrow(new ResourceAccessException("refused"))
        .when(restTemplate).put(any(URI.class), any(HttpEntity.class));

    assertThat(service.updateResource(Json.createObjectBuilder().build(), ID), is(false));
  }

  @Test
  public void shouldUpdateEveryExistingResourceUnlessAServiceSaysOtherwise() {
    assertThat(service.isUpdateNeeded(null, null), is(true));
  }

  @Test
  public void shouldCreateWithPostAndAddressASingleResourceByItsId() {
    assertThat(service.createMethod(), is(HttpMethod.POST));
    assertThat(service.updateUrl("http://host/api/programs", ID),
        is("http://host/api/programs/abc"));
    assertThat(service.deleteUrl("http://host/api/programs", ID),
        is("http://host/api/programs/abc"));
  }

  private void givenResponses(String first, String... rest) {
    OngoingStubbing<ResponseEntity<String>> stub =
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>(first, HttpStatus.OK));
    for (String body : rest) {
      stub = stub.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }
  }

  private String capturedBody() {
    ArgumentCaptor<HttpEntity> body = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplate).put(any(URI.class), body.capture());
    return String.valueOf(body.getValue().getBody());
  }

  private String page(String content, int totalPages) {
    return String.format("{\"content\":%s,\"totalPages\":%d}", content, totalPages);
  }

  private static class TestService extends BaseCommunicationService {

    @Override
    protected String getUrl() {
      return "/api/programs";
    }

    @Override
    public JsonObject findUnique(JsonObject object) {
      return null;
    }

    boolean updateWithoutId(JsonObject object, String id) {
      return updateResource(object, id, false);
    }

    HttpMethod createMethod() {
      return getCreateMethod();
    }

    String updateUrl(String base, String id) {
      return buildUpdateUrl(base, id);
    }

    String deleteUrl(String base, String id) {
      return buildDeleteUrl(base, id);
    }
  }
}
