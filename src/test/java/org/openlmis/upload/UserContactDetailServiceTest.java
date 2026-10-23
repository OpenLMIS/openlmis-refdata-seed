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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;

@RunWith(MockitoJUnitRunner.class)
public class UserContactDetailServiceTest {

  private static final String HOST = "http://olmis.test";
  private static final String USER_ID = "user-1";
  private static final String TOKEN_URL =
      HOST + "/api/userContactDetails/user-1/verifications";

  @Mock
  private RestOperations restTemplate;

  @Mock
  private AuthService authService;

  @Mock
  private Configuration configuration;

  private UserContactDetailService service;

  @Before
  public void setUp() {
    service = new UserContactDetailService();
    ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(service, "authService", authService);
    ReflectionTestUtils.setField(service, "configuration", configuration);
    when(configuration.getHost()).thenReturn(HOST);
    when(authService.obtainAccessToken()).thenReturn("token");
    when(configuration.autoVerifyEmails()).thenReturn(true);
  }

  @Test
  public void shouldAddressTheNewContactDetailsByTheirUserId() {
    service.createResource(details(USER_ID, false).toString());

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(restTemplate).put(uri.capture(), any(Object.class));
    assertThat(uri.getValue().toString(),
        is(HOST + "/api/userContactDetails/user-1?access_token=token"));
  }

  @Test
  public void shouldNotVerifyAnEmailThatWasNotSupplied() {
    service.afterEach(details(USER_ID, false));

    verify(restTemplate, never()).getForObject(any(URI.class), eq(String.class));
  }

  @Test
  public void shouldNotVerifyEmailsWhenTheRunAsksItNotTo() {
    when(configuration.autoVerifyEmails()).thenReturn(false);

    service.afterEach(details(USER_ID, true));

    verify(restTemplate, never()).getForObject(any(URI.class), eq(String.class));
  }

  @Test
  public void shouldFollowTheVerificationTokenToVerifyTheEmail() {
    when(restTemplate.getForObject(any(URI.class), eq(String.class)))
        .thenReturn("{\"token\":\"abc\"}", "");

    service.afterEach(details(USER_ID, true));

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(restTemplate, times(2))
        .getForObject(uri.capture(), eq(String.class));
    assertThat(uri.getAllValues().get(1).toString(),
        is(TOKEN_URL + "/abc?access_token=token"));
  }

  @Test
  public void shouldStopWhenThereIsNoVerificationTokenToFollow() {
    when(restTemplate.getForObject(any(URI.class), eq(String.class))).thenReturn(null);

    service.afterEach(details(USER_ID, true));

    verify(restTemplate, times(1))
        .getForObject(any(URI.class), eq(String.class));
  }

  @Test
  public void shouldCarryOnWhenTheTokenIsRejected() {
    when(restTemplate.getForObject(any(URI.class), eq(String.class)))
        .thenReturn("{\"token\":\"abc\"}")
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    service.afterEach(details(USER_ID, true));

    verify(restTemplate, times(2)).getForObject(any(URI.class), eq(String.class));
  }

  private JsonObject details(String userId, boolean withEmail) {
    JsonObjectBuilder builder = Json.createObjectBuilder()
        .add("referenceDataUserId", userId);
    if (withEmail) {
      builder.add("emailDetails", Json.createObjectBuilder().add("email", "a@b.test"));
    }
    return builder.build();
  }
}
