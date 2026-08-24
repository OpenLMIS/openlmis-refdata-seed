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

import javax.json.JsonArray;
import javax.json.JsonObject;
import org.springframework.stereotype.Service;

/**
 * Reads the flat, all-users role assignment listing. Role assignments are not seeded through this
 * endpoint - they are created as part of the user itself - so this service is only used to recover
 * them on export, where {@link UserService} folds them back into each user.
 */
@Service
public class RoleAssignmentService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/roleAssignments";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    return null;
  }

  @Override
  public JsonArray findAll() {
    RequestParameters parameters = RequestParameters.init()
        .set("page", 0)
        .set("size", 5000000);

    return findAll("", parameters);
  }
}
