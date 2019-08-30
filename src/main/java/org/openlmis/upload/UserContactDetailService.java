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

import static org.openlmis.upload.RequestHelper.createUri;

import java.net.URI;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.json.JsonObject;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class UserContactDetailService extends BaseCommunicationService {

  private static final String VERIFICATIONS_URL = "/verifications";
  
  @Override
  protected String getUrl() {
    return "/api/userContactDetails";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return null;
  }

  @Override
  protected HttpMethod getCreateMethod() {
    return HttpMethod.PUT;
  }

  @Override
  public boolean createResource(String json) {
    JsonObject jsonObject = convertToJsonObject(json);
    String customUrl = getUrl() + "/" + jsonObject.getString("referenceDataUserId");
    return super.createResource(configuration.getHost() + customUrl, json);
  }

  @Override
  public void afterEach(JsonObject userContactDetails) {
    JsonObject emailDetails = userContactDetails.getJsonObject("emailDetails");

    if (emailDetails == null) {
      return;
    }

    boolean autoVerifyEmail = configuration.autoVerifyEmails();

    if (autoVerifyEmail) {
      verifyUserEmail(userContactDetails);
    }
  }

  private void verifyUserEmail(JsonObject userContactDetails) {

    String userId = userContactDetails.getString("referenceDataUserId");

    if (userId == null) {
      logger.info("User id is missing. Stop performing verification");
      return;
    }
    
    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());
    String getTokenUrl = configuration.getHost() + getUrl() + "/" + userId + VERIFICATIONS_URL;
    URI getTokenUri = createUri(getTokenUrl, parameters);
    
    String response = restTemplate
        .getForObject(getTokenUri, String.class);
    
    if (response == null) {
      logger.info("No verification email token was found for user with id {}. " 
          + "Possible the email address was not changed", userId);
      return;
    }
    
    JsonObject emailVerificationToken = this.convertToJsonObject(response);
    
    String token = emailVerificationToken.getString("token");
    
    String verifyUrl = getTokenUrl + "/" + token;
    URI verifyUri = createUri(verifyUrl, parameters);
    
    try {
      restTemplate.getForObject(verifyUri, String.class);
    } catch (HttpClientErrorException httpException) {
      // This situation may occur when the EmailVerificationToken exists for given user id
      // and the emails in the EmailVerificationToken and request DTO are the same. Because of that
      // the token will not be renewed and possible the date will be expired.
      logger.error("Cannot verify email. Possible the token is invalid or token is expired." 
          + "Please contact administrator to remove old tokens from database");
    }
  }
}
