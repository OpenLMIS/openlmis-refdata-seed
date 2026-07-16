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

package org.openlmis.export.converter;

import java.util.Map;
import javax.json.JsonObject;
import org.openlmis.converter.Mapping;
import org.springframework.stereotype.Component;

@Component
public class SkipReverseConverter extends BaseReverseTypeConverter {
  @Override
  public boolean supports(String type) {
    return "SKIP".equalsIgnoreCase(type) || "USE_DEFAULT".equalsIgnoreCase(type);
  }

  @Override
  public void deconvert(JsonObject source, Mapping mapping, Map<String, String> row) {
    // nothing to do
  }
}
