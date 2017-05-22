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

import org.openlmis.upload.BaseCommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.openlmis.upload.Services;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@Component
public class FindObjectTypeConverter extends BaseTypeConverter {

  @Autowired
  private Services services;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_OBJECT_BY");
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    BaseCommunicationService service = services.getService(mapping.getEntityName());
    String by = getBy(mapping.getType());

    JsonObject jsonRepresentation = service.findBy(by, value);

    if (jsonRepresentation != null) {
      builder.add(mapping.getTo(), jsonRepresentation);
    } else {
      logger.warn(
          "The CSV file contained reference to entity {} "
              + "with {} {} but such reference does not exist.",
          mapping.getEntityName(), by, value
      );
    }
  }

}
