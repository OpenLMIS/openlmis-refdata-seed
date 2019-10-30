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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.upload.TradeItemService;
import org.openlmis.utils.AppHelper;

import java.util.Collections;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@RunWith(MockitoJUnitRunner.class)
public class FindTradeItemFromFileTypeConverterTest {

  private static final String FIND_TRADE_ITEM_FROM_FILE
      = "FIND_TRADE_ITEM_FROM_FILE_BY_PRODUCT_CODE";
  private static final String PRODUCT_CODE = "productCode";
  private static final String TRADE_ITEM = "tradeItem";
  private static final String VALUE = "112233";
  private static final String FROM = "from";
  private static final String TO = "to";

  @Mock
  private TradeItemService tradeItemService;

  @Mock
  private AppHelper appHelper;

  @InjectMocks
  private FindTradeItemFromFileTypeConverter converter;

  private String tradeItemId = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    doReturn(Collections.singletonList(Collections.singletonMap(PRODUCT_CODE, VALUE)))
        .when(appHelper).readCsv(anyString());
    doReturn(tradeItemId).when(tradeItemService)
        .findTradeItemIdByOrderableCode(VALUE);
  }

  @Test
  public void shouldSupportTypes() {
    assertThat(converter.supports(FIND_TRADE_ITEM_FROM_FILE), is(true));
  }

  @Test
  public void shouldAddTradeItemIfEntryExistsInCsv() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_TRADE_ITEM_FROM_FILE, "", "");

    converter.convert(builder, mapping, VALUE);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    JsonObject identifiersObject = object.getJsonObject(mapping.getTo());
    assertThat(identifiersObject.getString(TRADE_ITEM), is(tradeItemId));
  }

  @Test
  public void shouldNotAddTradeItemIfEntryDoesNotExistInCsv() {
    doReturn(Collections.emptyList()).when(appHelper).readCsv(anyString());

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_TRADE_ITEM_FROM_FILE, "", "");

    converter.convert(builder, mapping, VALUE);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    JsonObject identifiersObject = object.getJsonObject(mapping.getTo());
    assertThat(identifiersObject.isEmpty(), is(true));
  }

  @Test
  public void shouldNotAddTradeItemIfItDoesNotExistInDb() {
    doReturn(null).when(tradeItemService).findTradeItemIdByOrderableCode(VALUE);

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_TRADE_ITEM_FROM_FILE, "", "");

    converter.convert(builder, mapping, VALUE);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    JsonObject identifiersObject = object.getJsonObject(mapping.getTo());
    assertThat(identifiersObject.isEmpty(), is(true));
  }
}
