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

import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseCommunicationService {

  private static final String ROLE_ASSIGNMENTS = "roleAssignments";
  private static final String USER_ID = "userId";

  @Autowired
  private RoleAssignmentService roleAssignmentService;

  @Override
  protected String getUrl() {
    return "/api/users";
  }

  @Override
  public HttpMethod getCreateMethod() {
    return HttpMethod.PUT;
  }

  @Override
  public String buildUpdateUrl(String base, String id) {
    return base;
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return findBy("username", object.getString("username"));
  }

  /**
   * Returns every user with its role assignments filled in.
   *
   * @return the users to export, each carrying its own role assignments
   */
  @Override
  public JsonArray findAllForExport() {
    Map<String, JsonArrayBuilder> assignmentsByUser = groupRoleAssignmentsByUser();
    JsonArrayBuilder users = Json.createArrayBuilder();

    for (JsonValue value : findAll()) {
      if (value.getValueType() != JsonValue.ValueType.OBJECT) {
        continue;
      }

      JsonObject user = (JsonObject) value;
      JsonArrayBuilder assignments = user.containsKey(ID) && !user.isNull(ID)
          ? assignmentsByUser.get(user.getString(ID))
          : null;

      users.add(withRoleAssignments(user, assignments));
    }

    return users.build();
  }

  private Map<String, JsonArrayBuilder> groupRoleAssignmentsByUser() {
    Map<String, JsonArrayBuilder> grouped = new HashMap<>();

    for (JsonValue value : roleAssignmentService.findAll()) {
      if (value.getValueType() != JsonValue.ValueType.OBJECT) {
        continue;
      }

      JsonObject assignment = (JsonObject) value;
      if (!assignment.containsKey(USER_ID) || assignment.isNull(USER_ID)) {
        logger.warn("Role assignment without a userId; skipping: {}", assignment);
        continue;
      }

      grouped
          .computeIfAbsent(assignment.getString(USER_ID), userId -> Json.createArrayBuilder())
          .add(withoutKey(assignment, USER_ID));
    }

    return grouped;
  }

  /**
   * Replaces the user's {@code roleAssignments} with the given ones, leaving every other field as
   * the API returned it. Users with no assignments keep an empty array.
   */
  private JsonObject withRoleAssignments(JsonObject user, JsonArrayBuilder assignments) {
    JsonObjectBuilder builder = copyExcept(user, ROLE_ASSIGNMENTS);
    builder.add(ROLE_ASSIGNMENTS,
        assignments != null ? assignments.build() : Json.createArrayBuilder().build());

    return builder.build();
  }

  private JsonObject withoutKey(JsonObject object, String key) {
    return copyExcept(object, key).build();
  }

  private JsonObjectBuilder copyExcept(JsonObject object, String key) {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
      if (!key.equals(entry.getKey())) {
        builder.add(entry.getKey(), entry.getValue());
      }
    }

    return builder;
  }
}
