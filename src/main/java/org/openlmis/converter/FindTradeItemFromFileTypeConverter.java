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

import org.openlmis.upload.TradeItemService;
import org.openlmis.utils.AppHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

@Component
public class FindTradeItemFromFileTypeConverter extends BaseTypeConverter {

  private static final String TRADE_ITEM = "tradeItem";

  @Autowired
  private TradeItemService tradeItemService;

  @Autowired
  private AppHelper appHelper;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "FIND_TRADE_ITEM_FROM_FILE_BY");
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String orderableCode) {
    tradeItemService.before();

    String by = getBy(mapping.getType());
    String entityFileName = mapping.getEntityName();

    boolean tradeItemIsAssociated = appHelper.readCsv(entityFileName).stream()
        .anyMatch(map -> orderableCode.equals(map.get(by)));

    JsonObjectBuilder identifiers = Json.createObjectBuilder();

    if (tradeItemIsAssociated) {
      String existingId = getTradeItemId(orderableCode);
      if (existingId != null) {
        identifiers.add(TRADE_ITEM, existingId);
      }
    }

    builder.add(mapping.getTo(), identifiers);
  }

  private String getTradeItemId(String orderableCode) {
    String existingId = tradeItemService.findTradeItemIdByOrderableCode(orderableCode);
    if (existingId == null) {
      logger.warn("Can't find trade item associated with product code: {}. "
          + "Creating without identifier...", orderableCode);
    }
    return existingId;
  }
}
