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

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import org.openlmis.upload.TradeItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindCachedTradeItemTypeConverter extends BaseTypeConverter {

  private static final String TRADE_ITEM = "tradeItem";

  @Autowired
  private TradeItemService tradeItemService;

  @Override
  public boolean supports(String type) {
    return "FIND_CACHED_TRADE_ITEM".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {

    String tradeItemId = tradeItemService.findCachedTradeItemIdByOrderableCode(value);

    if (tradeItemId == null) {
      logger.warn("Can't find cached trade item associated with product code: {}", value);
      return;
    }

    JsonObjectBuilder tradeItemJsonBuilder = Json.createObjectBuilder();

    tradeItemJsonBuilder.add(TRADE_ITEM, tradeItemId);
    builder.add(mapping.getTo(), tradeItemJsonBuilder.build());
  }
}
