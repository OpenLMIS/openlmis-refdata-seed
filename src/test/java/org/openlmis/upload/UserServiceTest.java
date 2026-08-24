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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  private static final String ROLE_ASSIGNMENTS = "roleAssignments";
  private static final String USER_ID = "userId";
  private static final String ROLE_ID = "roleId";
  private static final String USER_ONE = "user-1";
  private static final String USER_TWO = "user-2";
  private static final String USERNAME = "jdoe";
  private static final String ROLE_A = "role-a";

  @Mock
  private RoleAssignmentService roleAssignmentService;

  private UserService service;

  @Before
  public void setUp() {
    service = spy(new UserService());
    ReflectionTestUtils.setField(service, "roleAssignmentService", roleAssignmentService);
  }

  @Test
  public void shouldGroupRoleAssignmentsOntoTheirUsers() {
    givenUsers(user(USER_ONE, USERNAME), user(USER_TWO, "asmith"));
    givenRoleAssignments(
        assignment(USER_ONE, ROLE_A),
        assignment(USER_TWO, "role-b"),
        assignment(USER_ONE, "role-c"));

    JsonArray exported = service.findAllForExport();

    JsonArray first = exported.getJsonObject(0).getJsonArray(ROLE_ASSIGNMENTS);
    assertThat(first.size(), is(2));
    assertThat(first.getJsonObject(0).getString(ROLE_ID), is(ROLE_A));
    assertThat(first.getJsonObject(1).getString(ROLE_ID), is("role-c"));

    JsonArray second = exported.getJsonObject(1).getJsonArray(ROLE_ASSIGNMENTS);
    assertThat(second.size(), is(1));
    assertThat(second.getJsonObject(0).getString(ROLE_ID), is("role-b"));
  }

  @Test
  public void shouldKeepUserFieldsAndStripUserIdFromAssignments() {
    givenUsers(user(USER_ONE, USERNAME));
    givenRoleAssignments(assignment(USER_ONE, ROLE_A));

    JsonObject exported = service.findAllForExport().getJsonObject(0);

    assertThat(exported.getString("id"), is(USER_ONE));
    assertThat(exported.getString("username"), is(USERNAME));

    JsonObject assignment = exported.getJsonArray(ROLE_ASSIGNMENTS).getJsonObject(0);
    assertThat(assignment.getString(ROLE_ID), is(ROLE_A));
    assertThat(assignment.containsKey(USER_ID), is(false));
  }

  @Test
  public void shouldOverwriteTheEmptyArrayReportedByTheUserListing() {
    givenUsers(Json.createObjectBuilder()
        .add("id", USER_ONE)
        .add("username", USERNAME)
        .add(ROLE_ASSIGNMENTS, Json.createArrayBuilder().build())
        .build());
    givenRoleAssignments(assignment(USER_ONE, ROLE_A));

    JsonObject exported = service.findAllForExport().getJsonObject(0);

    assertThat(exported.getJsonArray(ROLE_ASSIGNMENTS).size(), is(1));
  }

  @Test
  public void shouldLeaveUsersWithoutAssignmentsEmpty() {
    givenUsers(user(USER_ONE, USERNAME));
    givenRoleAssignments(assignment(USER_TWO, "role-b"));

    JsonObject exported = service.findAllForExport().getJsonObject(0);

    assertThat(exported.getJsonArray(ROLE_ASSIGNMENTS).size(), is(0));
  }

  @Test
  public void shouldSkipAssignmentsWithoutUserId() {
    givenUsers(user(USER_ONE, USERNAME));
    givenRoleAssignments(Json.createObjectBuilder().add(ROLE_ID, ROLE_A).build());

    JsonObject exported = service.findAllForExport().getJsonObject(0);

    assertThat(exported.getJsonArray(ROLE_ASSIGNMENTS).size(), is(0));
  }

  private void givenUsers(JsonObject... users) {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for (JsonObject user : users) {
      builder.add(user);
    }
    doReturn(builder.build()).when(service).findAll();
  }

  private void givenRoleAssignments(JsonObject... assignments) {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for (JsonObject assignment : assignments) {
      builder.add(assignment);
    }
    when(roleAssignmentService.findAll()).thenReturn(builder.build());
  }

  private JsonObject user(String id, String username) {
    return Json.createObjectBuilder()
        .add("id", id)
        .add("username", username)
        .build();
  }

  private JsonObject assignment(String userId, String roleId) {
    return Json.createObjectBuilder()
        .add(USER_ID, userId)
        .add(ROLE_ID, roleId)
        .build();
  }
}
