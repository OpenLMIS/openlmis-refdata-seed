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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestOperations;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class TradeItemServiceTest {

  private static final String PRODUCT_CODE = "productCode";
  private static final String IDENTIFIERS = "identifiers";
  private static final String TRADE_ITEM = "tradeItem";
  private static final String MANUFACTURER = "manufacturerOfTradeItem";
  private static final String C100 = "C100";
  private static final String C200 = "C200";
  private static final String C300 = "C300";
  private static final String TRADE_ITEM_ID = "trade-item-1";
  private static final String OTHER_TRADE_ITEM_ID = "trade-item-9";
  private static final String NEW_TRADE_ITEM_ID = "trade-item-2";
  private static final String HOST = "http://host";
  private static final String URL = HOST + "/api/tradeItems";

  @Mock
  private OrderableService orderableService;

  @Mock
  private RestOperations restTemplate;

  @Mock
  private AuthService authService;

  @Mock
  private Configuration configuration;

  private TradeItemService service;

  @Before
  public void setUp() {
    service = spy(new TradeItemService());
    ReflectionTestUtils.setField(service, "orderableService", orderableService);
    ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(service, "authService", authService);
    ReflectionTestUtils.setField(service, "configuration", configuration);
    when(authService.obtainAccessToken()).thenReturn("token");
  }

  @Test
  public void shouldIndexEveryOrderableConnectedToATradeItemAndNoOther() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID), orderable(C300, null),
        orderable(C200, OTHER_TRADE_ITEM_ID));

    service.before();

    assertThat(service.findTradeItemIdByOrderableCode(C100), is(TRADE_ITEM_ID));
    assertThat(service.findTradeItemIdByOrderableCode(C200), is(OTHER_TRADE_ITEM_ID));
    assertThat(service.findTradeItemIdByOrderableCode(C300), is(nullValue()));
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
  public void shouldRefuseASecondTradeItemForAProductThatAlreadyHasOne() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID));
    service.before();

    assertThat(service.createResource(URL, tradeItem(C100).toString()), is(false));

    verifyZeroInteractions(restTemplate);
  }

  @Test
  public void shouldPutANewTradeItemAndCacheTheIdTheApiSendsBack() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID));
    service.before();
    givenTheApiAnswersWith("{\"id\":\"" + NEW_TRADE_ITEM_ID + "\"}");

    assertThat(service.getCreateMethod(), is(HttpMethod.PUT));
    assertThat(service.createResource(URL, tradeItem(C200).toString()), is(true));

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<HttpEntity> body = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplate).exchange(uri.capture(), eq(HttpMethod.PUT), body.capture(),
        eq(String.class));
    assertThat(uri.getValue().toString(), is(URL + "?access_token=token"));
    assertThat(String.valueOf(body.getValue().getBody()), is(tradeItem(C200).toString()));

    assertThat(service.findTradeItemIdByOrderableCode(C200), is(NEW_TRADE_ITEM_ID));
    assertThat(service.findTradeItemIdByOrderableCode(C100), is(TRADE_ITEM_ID));
  }

  @Test
  public void shouldNotCacheATradeItemTheApiSentNoBodyFor() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID));
    service.before();
    givenTheApiAnswersWith(null);

    assertThat(service.createResource(URL, tradeItem(C200).toString()), is(true));

    assertThat(service.findTradeItemIdByOrderableCode(C200), is(nullValue()));
  }

  @Test
  public void shouldUpdateUnderTheTradeItemIdItFoundForTheProductCode() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID));
    service.before();
    when(configuration.getHost()).thenReturn(HOST);

    assertThat(service.updateResource(tradeItem(C100), "ignored-id"), is(true));

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<HttpEntity> body = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplate).put(uri.capture(), body.capture());
    assertThat(uri.getValue().toString(), is(URL + "/?access_token=token"));
    assertThat(String.valueOf(body.getValue().getBody()),
        is("{\"productCode\":\"C100\",\"id\":\"" + TRADE_ITEM_ID + "\"}"));
  }

  @Test
  public void shouldRefuseToUpdateAProductThatHasNoTradeItem() {
    givenOrderables(orderable(C100, TRADE_ITEM_ID));
    service.before();

    assertThat(service.updateResource(tradeItem("C999"), "ignored-id"), is(false));

    verifyZeroInteractions(restTemplate);
  }

  @Test
  public void shouldNeedAnUpdateOnlyWhenTheManufacturerChanged() {
    assertThat(service.isUpdateNeeded(withManufacturer("Acme"), withManufacturer("Other")),
        is(true));
    assertThat(service.isUpdateNeeded(withManufacturer("Acme"), withManufacturer("Acme")),
        is(false));
  }

  private void givenTheApiAnswersWith(String responseBody) {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.PUT), any(HttpEntity.class),
        eq(String.class)))
        .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
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
