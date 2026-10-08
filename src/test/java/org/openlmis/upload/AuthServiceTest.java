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
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.openlmis.utils.AuthorizationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceTest {

  private static final String HOST = "http://olmis.test";
  private static final String TOKEN = "a-token";

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Mock
  private RestOperations restTemplate;

  @Mock
  private Configuration configuration;

  private AuthService service;

  @Before
  public void setUp() {
    service = new AuthService();
    ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(service, "configuration", configuration);
    when(configuration.getHost()).thenReturn(HOST);
    when(configuration.getClientId()).thenReturn("user-client");
    when(configuration.getClientSecret()).thenReturn("changeme");
    when(configuration.getLogin()).thenReturn("admin");
    when(configuration.getPassword()).thenReturn("secret");
  }

  @Test
  public void shouldReturnTheAccessTokenFromTheResponse() {
    givenToken();

    assertThat(service.obtainAccessToken(), is(TOKEN));
  }

  @Test
  public void shouldAskTheAuthEndpointForAPasswordGrant() {
    givenToken();

    service.obtainAccessToken();

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(restTemplate).exchange(uri.capture(), eq(HttpMethod.POST), any(HttpEntity.class),
        eq(Object.class));
    assertThat(uri.getValue().toString(),
        is(HOST + "/api/oauth/token?grant_type=password"));
  }

  @Test
  public void shouldSendTheClientCredentialsAsBasicAuthAndTheLoginAsAForm() {
    givenToken();

    service.obtainAccessToken();

    ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), request.capture(),
        eq(Object.class));
    assertThat(request.getValue().getHeaders().getFirst("Authorization"),
        is("Basic dXNlci1jbGllbnQ6Y2hhbmdlbWU="));
    assertThat(request.getValue().getBody().toString(),
        is("{username=[admin], password=[secret]}"));
  }

  @Test
  public void shouldFailWithAClearMessageWhenTheCredentialsAreRejected() {
    givenTheAuthEndpointFailsWith(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

    expected.expect(AuthorizationException.class);
    expected.expectMessage("Cannot obtain access token");

    service.obtainAccessToken();
  }

  private void givenTheAuthEndpointFailsWith(RuntimeException failure) {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
        eq(Object.class))).thenThrow(failure);
  }

  private void givenToken() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
        eq(Object.class)))
        .thenReturn(new ResponseEntity<Object>(
            ImmutableMap.of("access_token", TOKEN), HttpStatus.OK));
  }
}
