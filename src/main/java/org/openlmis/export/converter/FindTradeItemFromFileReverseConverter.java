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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ChildCsvCollector;
import org.openlmis.export.utils.ReferenceResolver;
import org.openlmis.utils.AppHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class FindTradeItemFromFileReverseConverter extends BaseReverseTypeConverter {

  private static final String TRADE_ITEM = "tradeItem";

  @Autowired
  @Lazy
  private Deconverter deconverter;

  @Autowired
  private AppHelper appHelper;

  @Autowired
  private ChildCsvCollector collector;

  @Autowired
  private ReferenceResolver resolver;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "FIND_TRADE_ITEM_FROM_FILE_BY");
  }

  @Override
  public void deconvert(JsonObject source, Mapping mapping, Map<String, String> row) {
    String tradeItemId = getTradeItemId(source, mapping.getTo());
    if (tradeItemId == null) {
      return;
    }

    String productCodeField = getBy(mapping.getType());
    String productCode = getCsvString(source.get(productCodeField));
    if (isBlank(productCode)) {
      return;
    }

    // The child TradeItems row combines the product code (which lives on the parent orderable, not
    // on the trade item) with the referenced trade item's own fields (e.g. manufacturer), then is
    // reversed through the child mapping like any other FROM_FILE child.
    JsonObjectBuilder builder = Json.createObjectBuilder();
    JsonObject tradeItem = resolver.findById("TradeItem", tradeItemId);
    if (tradeItem != null) {
      for (Map.Entry<String, JsonValue> entry : tradeItem.entrySet()) {
        builder.add(entry.getKey(), entry.getValue());
      }
    }
    builder.add(productCodeField, source.get(productCodeField));

    String childFileName = mapping.getEntityName();
    List<Mapping> childMappings = appHelper.readMappings(childFileName);
    List<String> childHeader = deconverter.getHeader(childMappings);
    Map<String, String> childRow = deconverter.deconvert(builder.build(), childMappings);

    collector.add(childFileName, childHeader, childRow);
    if (isBlank(row.get(mapping.getFrom()))) {
      row.put(mapping.getFrom(), productCode);
    }
  }

  private String getTradeItemId(JsonObject source, String identifiersField) {
    if (!source.containsKey(identifiersField) || source.isNull(identifiersField)) {
      return null;
    }

    JsonValue value = source.get(identifiersField);
    if (value.getValueType() != JsonValue.ValueType.OBJECT) {
      return null;
    }

    JsonObject identifiers = (JsonObject) value;
    if (!identifiers.containsKey(TRADE_ITEM) || identifiers.isNull(TRADE_ITEM)) {
      return null;
    }

    JsonValue tradeItem = identifiers.get(TRADE_ITEM);
    if (tradeItem.getValueType() != JsonValue.ValueType.STRING) {
      logger.warn("{}.{} is not an id string ({}); skipping trade item.",
          identifiersField, TRADE_ITEM, tradeItem.getValueType());
      return null;
    }

    return ((JsonString) tradeItem).getString();
  }
}
