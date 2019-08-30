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

package org.openlmis.converter;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.util.Optional;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.openlmis.upload.UserContactDetailService;
import org.openlmis.upload.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindEmailVerifiedConverter extends BaseTypeConverter {

  private static final String USERNAME = "username";
  private static final String REFERENCE_DATA_USER_ID = "referenceDataUserId";
  private static final String EMAIL_VERIFIED = "emailVerified";

  @Autowired
  private UserService userService;

  @Autowired
  private UserContactDetailService userContactDetailService;


  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "FIND_EMAIL_VERIFIED");
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    findEmailVerified(value).ifPresent(emailVerified ->
        builder.add(mapping.getTo(), emailVerified));
  }

  private Optional<String> findEmailVerified(String username) {
    JsonObject user = userService.findBy(USERNAME, username);

    if (user == null) {
      logger.warn("Can not find user with username {}", username);
      return Optional.empty();
    }

    String userId = user.getString("id");

    JsonObject userContactDetails = userContactDetailService.findBy(REFERENCE_DATA_USER_ID, userId);

    if (userContactDetails == null) {
      logger.warn("Can not find user contact details with username {}", username);
      return Optional.empty();
    }

    JsonObject emailDetails = userContactDetails.getJsonObject("emailDetails");

    if (emailDetails == null) {
      logger.warn("There is not email details connect to user {}", username);
      return Optional.empty();
    }
    return Optional.of(String.valueOf(emailDetails.getOrDefault(EMAIL_VERIFIED, null)));
  }
}
