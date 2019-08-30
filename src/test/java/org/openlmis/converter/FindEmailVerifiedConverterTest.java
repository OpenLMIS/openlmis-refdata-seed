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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.upload.UserContactDetailService;
import org.openlmis.upload.UserService;

@RunWith(MockitoJUnitRunner.class)
public class FindEmailVerifiedConverterTest {

  private static final String USERNAME = "username";
  private static final String REFERENCE_DATA_USER_ID = "referenceDataUserId";
  private static final String EMAIL_VERIFIED = "emailVerified";
  private static final String FIND_EMAIL_VERIFIED = "FIND_EMAIL_VERIFIED";
  private static final String USERNAME_VALUE = "testusername";
  private static final String FROM = "from";
  private static final String TO = "to";
  
  @Mock
  private UserService userService;
  
  @Mock
  private UserContactDetailService userContactDetailService;

  @InjectMocks
  private FindEmailVerifiedConverter converter;
  
  private UUID userUuid = UUID.randomUUID();

  @Before
  public void setUp() {
    JsonObjectBuilder userObjectBuilder = Json.createObjectBuilder();
    userObjectBuilder.add("id", userUuid.toString());
    
    
    when(userService.findBy(USERNAME, USERNAME_VALUE))
          .thenReturn(userObjectBuilder.build());

    JsonObjectBuilder userContactDetailsBuilder = Json.createObjectBuilder();
    JsonObjectBuilder emailDetailsBuilder = Json.createObjectBuilder();
    emailDetailsBuilder.add("email", "test@mail.com");
    emailDetailsBuilder.add(EMAIL_VERIFIED, true);

    userContactDetailsBuilder.add("emailDetails", emailDetailsBuilder.build());
    
    when(userContactDetailService.findBy(REFERENCE_DATA_USER_ID, userUuid.toString()))
        .thenReturn(userContactDetailsBuilder.build());
  }

  @Test
  public void shouldConvert() throws Exception {

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_EMAIL_VERIFIED, "", "");

    converter.convert(builder, mapping, USERNAME_VALUE);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    assertThat(object.getString(TO), is(String.valueOf(true)));
  }


}
