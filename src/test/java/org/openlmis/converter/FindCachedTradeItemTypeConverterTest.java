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
import static org.mockito.Mockito.doReturn;

import java.util.UUID;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.upload.TradeItemService;

@RunWith(MockitoJUnitRunner.class)
public class FindCachedTradeItemTypeConverterTest {

  private static final String FIND_CACHED_TRADE_ITEM = "FIND_CACHED_TRADE_ITEM";
  private static final String TRADE_ITEM = "tradeItem";
  private static final String VALUE = "112233";
  private static final String FROM = "from";
  private static final String TO = "to";


  @Mock
  private TradeItemService tradeItemService;

  @InjectMocks
  private FindCachedTradeItemTypeConverter converter;

  private String tradeItemId = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    doReturn(tradeItemId).when(tradeItemService)
        .findCachedTradeItemIdByOrderableCode(VALUE);
  }

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports(FIND_CACHED_TRADE_ITEM), is(true));
  }

  @Test
  public void shouldConvert() throws Exception {

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_CACHED_TRADE_ITEM, "", "");

    converter.convert(builder, mapping, VALUE);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    JsonObject identifiersObject = object.getJsonObject(mapping.getTo());
    assertThat(identifiersObject.getString(TRADE_ITEM), is(tradeItemId));
  }

  @Test
  public void shouldNotSetValueIfOrderableCodeNotInCache() {

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_CACHED_TRADE_ITEM, "", "");

    converter.convert(builder, mapping, "NOT_VALID_PRODUCT_CODE");

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }
}
