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

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;


import org.openlmis.Configuration;
import org.openlmis.utils.AuthorizationException;

import java.util.Map;

@Service
public class AuthService {

  public static final String ACCESS_TOKEN = "access_token";

  @Autowired
  private Configuration configuration;

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Retrieves access token from the auth service.
   *
   * @return token
   */
  public String obtainAccessToken() {
    String plainCreds = configuration.getClientId() + ":" + configuration.getClientSecret();
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + base64Creds);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("username", configuration.getLogin());
    form.add("password", configuration.getPassword());

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
    RequestParameters params = RequestParameters
        .init()
        .set("grant_type", "password");

    try {
      return ((Map<String, String>)runWithTokenRetry(() -> restTemplate.exchange(
          RequestHelper.createUri(configuration.getHost() + "/api/oauth/token", params),
          HttpMethod.POST,
          request,
          Object.class)).getBody()).get(ACCESS_TOKEN);
    } catch (RestClientException ex) {
      throw new AuthorizationException("Cannot obtain access token using the provided credentials. "
          + "Please verify they are correct.", ex);
    }
  }

  protected <P> ResponseEntity<P> runWithTokenRetry(HttpTask<P> task) {
    try {
      return task.run();
    } catch (HttpStatusCodeException ex) {
      return task.run();
    }
  }

  @FunctionalInterface
  protected interface HttpTask<T> {

    ResponseEntity<T> run();

  }

  void setRestTemplate(RestOperations restTemplate) {
    this.restTemplate = restTemplate;
  }
}
