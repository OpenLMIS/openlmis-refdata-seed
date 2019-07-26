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
import org.openlmis.upload.OrderableService;

@RunWith(MockitoJUnitRunner.class)
public class FindTradeItemTypeConverterTest {

  private static final String FIND_TRADE_ITEM = "FIND_TRADE_ITEM";
  private static final String PRODUCT_CODE = "productCode";
  private static final String VALUE = "123344";
  private static final String IDENTIFIERS = "identifiers";
  private static final String TRADE_ITEM = "tradeItem";
  private static final String FROM = "from";
  private static final String TO = "to";

  @Mock
  private OrderableService orderableService;

  @InjectMocks
  private FindTradeItemTypeConverter converter;

  @Mock
  private JsonObject identifiers;

  @Mock
  private JsonObject orderable;

  private String tradeItemId = UUID.randomUUID().toString();

  @Before
  public void setUp() throws Exception {
    doReturn(identifiers).when(orderable).getJsonObject(IDENTIFIERS);
    doReturn(tradeItemId).when(identifiers).getString(TRADE_ITEM, null);
  }

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports(FIND_TRADE_ITEM), is(true));
  }

  @Test
  public void shouldConvert() throws Exception {
    doReturn(orderable).when(orderableService).findBy(PRODUCT_CODE, VALUE);

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_TRADE_ITEM, "", "");

    converter.convert(builder, mapping, VALUE);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));
    assertThat(object.getString(mapping.getTo()), is(tradeItemId));
  }

  @Test
  public void shouldNotSetValueIfOrderableCannotBeFound() {
    doReturn(null).when(orderableService).findBy(PRODUCT_CODE, VALUE);

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_TRADE_ITEM, "", "");

    converter.convert(builder, mapping, VALUE);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

  @Test
  public void shouldNotSetValueIfOrderableIdentifiersAreNull() {
    doReturn(orderable).when(orderableService).findBy(PRODUCT_CODE, VALUE);
    doReturn(null).when(orderable).getJsonObject(IDENTIFIERS);

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_TRADE_ITEM, "", "");

    converter.convert(builder, mapping, VALUE);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

  @Test
  public void shouldNotSetValueIfOrderableNotHaveTradeItem() {
    doReturn(orderable).when(orderableService).findBy(PRODUCT_CODE, VALUE);
    doReturn(null).when(identifiers).getString(TRADE_ITEM, null);

    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping(FROM, TO, FIND_TRADE_ITEM, "", "");

    converter.convert(builder, mapping, VALUE);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }


}
