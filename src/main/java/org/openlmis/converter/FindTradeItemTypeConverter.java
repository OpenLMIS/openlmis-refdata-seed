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

import java.util.Optional;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.openlmis.upload.OrderableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindTradeItemTypeConverter extends BaseTypeConverter {

  private static final String PRODUCT_CODE = "productCode";
  private static final String IDENTIFIERS = "identifiers";
  private static final String TRADE_ITEM = "tradeItem";


  @Autowired
  private OrderableService orderableService;

  @Override
  public boolean supports(String type) {
    return "FIND_TRADE_ITEM".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    findTradeItem(value).ifPresent(tradeItemId -> builder.add(mapping.getTo(), tradeItemId));

  }

  private Optional<String> findTradeItem(String productCode) {
    JsonObject orderable = orderableService.findBy(PRODUCT_CODE, productCode);

    if (orderable == null) {
      logger.warn("Can not find product with code {}", productCode);
      return Optional.empty();
    }
    JsonObject identifiers = orderable.getJsonObject(IDENTIFIERS);

    if (identifiers == null) {
      logger.warn("Product with code {} does not have identifiers", productCode);
      return Optional.empty();
    }
    String tradeItemId = identifiers.getString(TRADE_ITEM, null);

    if (tradeItemId == null) {
      logger.warn("Product with code {} is not connected with any trade item", productCode);
      return Optional.empty();
    }

    logger.debug("Found trade item with id: {}", tradeItemId);

    return Optional.of(tradeItemId);
  }
}
