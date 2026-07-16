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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ChildCsvCollector;
import org.openlmis.export.utils.ReferenceResolver;
import org.openlmis.utils.AppHelper;

@RunWith(MockitoJUnitRunner.class)
public class FindTradeItemFromFileReverseConverterTest {

  private static final String PRODUCT_CODE = "productCode";
  private static final String IDENTIFIERS = "identifiers";
  private static final String MANUFACTURER = "manufacturerOfTradeItem";
  private static final String FILE = "TradeItems.csv";
  private static final String TYPE = "FIND_TRADE_ITEM_FROM_FILE_BY_PRODUCT_CODE";
  private static final String PRODUCT_CODE_VALUE = "P100";

  @Mock
  private Deconverter deconverter;

  @Mock
  private AppHelper appHelper;

  @Mock
  private ChildCsvCollector collector;

  @Mock
  private ReferenceResolver resolver;

  @InjectMocks
  private FindTradeItemFromFileReverseConverter converter;

  @Test
  public void shouldSupportFindTradeItemFromFileTypesOnly() {
    assertThat(converter.supports(TYPE), is(true));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_CODE"), is(false));
    assertThat(converter.supports("FIND_TRADE_ITEM"), is(false));
  }

  @Test
  public void shouldBuildChildRowFromTradeItemAndProductCode() {
    final JsonObject source = Json.createObjectBuilder()
        .add(PRODUCT_CODE, PRODUCT_CODE_VALUE)
        .add(IDENTIFIERS, Json.createObjectBuilder().add("tradeItem", "ti-1"))
        .build();
    when(resolver.findById("TradeItem", "ti-1"))
        .thenReturn(Json.createObjectBuilder().add(MANUFACTURER, "ACME").build());

    List<Mapping> childMappings = asList(
        new Mapping(PRODUCT_CODE, "value", "TO_ID_BY_PRODUCT_CODE", "TradeItem", ""),
        new Mapping(MANUFACTURER, MANUFACTURER, "DIRECT", "", ""));
    when(appHelper.readMappings(FILE)).thenReturn(childMappings);
    when(deconverter.getHeader(childMappings)).thenReturn(asList(PRODUCT_CODE, MANUFACTURER));

    Map<String, String> childRow = new LinkedHashMap<>();
    childRow.put(PRODUCT_CODE, PRODUCT_CODE_VALUE);
    childRow.put(MANUFACTURER, "ACME");
    when(deconverter.deconvert(any(JsonObject.class), eq(childMappings))).thenReturn(childRow);

    Mapping mapping = new Mapping(PRODUCT_CODE, IDENTIFIERS, TYPE, FILE, "");
    Map<String, String> parentRow = new LinkedHashMap<>();
    converter.deconvert(source, mapping, parentRow);

    verify(collector).add(eq(FILE), eq(asList(PRODUCT_CODE, MANUFACTURER)), eq(childRow));
    assertThat(parentRow.get(PRODUCT_CODE), is(PRODUCT_CODE_VALUE));
  }

  @Test
  public void shouldSkipWhenThereIsNoTradeItemIdentifier() {
    final JsonObject source = Json.createObjectBuilder()
        .add(PRODUCT_CODE, PRODUCT_CODE_VALUE)
        .add(IDENTIFIERS, Json.createObjectBuilder())
        .build();
    Mapping mapping = new Mapping(PRODUCT_CODE, IDENTIFIERS, TYPE, FILE, "");

    Map<String, String> parentRow = new LinkedHashMap<>();
    converter.deconvert(source, mapping, parentRow);

    verify(collector, never()).add(anyString(), anyList(), anyMap());
    assertThat(parentRow.isEmpty(), is(true));
  }
}
