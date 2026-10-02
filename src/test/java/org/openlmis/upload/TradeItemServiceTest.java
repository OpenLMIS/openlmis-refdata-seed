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

package org.openlmis.upload;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class TradeItemServiceTest {

  private static final String PRODUCT_CODE = "productCode";
  private static final String IDENTIFIERS = "identifiers";
  private static final String TRADE_ITEM = "tradeItem";
  private static final String MANUFACTURER = "manufacturerOfTradeItem";
  private static final String C100 = "C100";
  private static final String TRADE_ITEM_ID = "trade-item-1";

  @Mock
  private OrderableService orderableService;

  private TradeItemService service;

  @Before
  public void setUp() {
    service = spy(new TradeItemService());
    ReflectionTestUtils.setField(service, "orderableService", orderableService);
  }

  @Test
  public void shouldIndexOnlyOrderablesThatAreConnectedToATradeItem() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID), orderable("C200", null));

    service.before();

    assertThat(service.findTradeItemIdByOrderableCode(C100), is(TRADE_ITEM_ID));
    assertThat(service.findTradeItemIdByOrderableCode("C200"), is(nullValue()));
  }

  @Test
  public void shouldBuildTheIndexOnlyOnce() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID));

    service.before();
    service.before();

    verify(orderableService, times(1)).findAll();
  }

  @Test
  public void shouldNotFindAProductCodeThatWasNeverIndexed() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID));
    service.before();

    assertThat(service.findUnique(tradeItem("C999")), is(nullValue()));
  }

  @Test
  public void shouldLookUpAKnownProductCodeByItsTradeItemId() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID));
    service.before();
    JsonObject found = Json.createObjectBuilder().add("id", TRADE_ITEM_ID).build();
    doReturn(found).when(service).findBy("id", TRADE_ITEM_ID);

    assertThat(service.findUnique(tradeItem(C100)), is(found));
  }

  @Test
  public void shouldPutNewTradeItemsAndRefuseASecondOneForTheSameProduct() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID));
    service.before();

    assertThat(service.getCreateMethod(), is(HttpMethod.PUT));
    assertThat(service.createResource("http://host/api/tradeItems",
        tradeItem(C100).toString()), is(false));
  }

  @Test
  public void shouldUpdateOnlyWhenTheManufacturerChanged() {
    assertThat(service.isUpdateNeeded(withManufacturer("Acme"), withManufacturer("Other")),
        is(true));
    assertThat(service.isUpdateNeeded(withManufacturer("Acme"), withManufacturer("Acme")),
        is(false));
  }

  private void givenOrderables(JsonObjectBuilder... orderables) {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for (JsonObjectBuilder orderable : orderables) {
      builder.add(orderable);
    }
    JsonArray all = builder.build();
    when(orderableService.findAll()).thenReturn(all);
  }

  private JsonObjectBuilder orderable(String productCode, String tradeItemId) {
    JsonObjectBuilder identifiers = Json.createObjectBuilder();
    if (tradeItemId != null) {
      identifiers.add(TRADE_ITEM, tradeItemId);
    }
    return Json.createObjectBuilder().add(PRODUCT_CODE, productCode)
        .add(IDENTIFIERS, identifiers);
  }

  private JsonObject tradeItem(String productCode) {
    return Json.createObjectBuilder().add(PRODUCT_CODE, productCode).build();
  }

  private JsonObject withManufacturer(String manufacturer) {
    return Json.createObjectBuilder().add(MANUFACTURER, manufacturer).build();
  }
}
