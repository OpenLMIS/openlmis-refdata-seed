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

import static org.openlmis.upload.RequestHelper.createUri;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@Service
public class TradeItemService extends BaseCommunicationService {

  private static final String PRODUCT_CODE = "productCode";
  private static final String IDENTIFIERS = "identifiers";
  private static final String TRADE_ITEM = "tradeItem";

  @Autowired
  private OrderableService orderableService;

  private Map<String, String> cachedTradeItemIdByOrderableCode;

  /**
   * Finds trade item id based on product code.
   * @param orderableCode the value of product code
   * @return String trade item id if it exits in cache, Null otherwise.
   */
  public String findTradeItemIdByOrderableCode(String orderableCode) {
    return cachedTradeItemIdByOrderableCode.getOrDefault(orderableCode, null);
  }

  @Override
  public void before() {
    if (cachedTradeItemIdByOrderableCode != null) {
      // Use cached version if available
      return;
    }

    JsonArray orderables = this.orderableService.findAll();
    cachedTradeItemIdByOrderableCode = IntStream.range(0, orderables.size())
        .mapToObj(orderables::getJsonObject)
        .filter(this::isConnectedWithTradeItem)
        .collect(Collectors.toMap(
            json -> json.getString(PRODUCT_CODE),
            json -> json.getJsonObject(IDENTIFIERS).getString(TRADE_ITEM)));
  }


  @Override
  protected String getUrl() {
    return "/api/tradeItems";
  }

  @Override
  public JsonObject findUnique(JsonObject object) {
    String productCode = object.getString(PRODUCT_CODE);

    String tradeItemId = cachedTradeItemIdByOrderableCode.get(productCode);

    if (tradeItemId == null) {
      logger.debug("Trade item with product code: {} not found.", productCode);
      return null;
    }
    return findBy(ID, tradeItemId);
  }

  @Override
  public HttpMethod getCreateMethod() {
    return HttpMethod.PUT;
  }

  @Override
  public boolean createResource(String url, String json) {
    JsonObject tradeItem = this.convertToJsonObject(json);
    String orderableCode = tradeItem.getString(PRODUCT_CODE);

    if (cachedTradeItemIdByOrderableCode.containsKey(orderableCode)) {
      logger.error(
          "Cannot create trade item with product code {}. This product is already connected "
              + "with trade item: {}", orderableCode,
          cachedTradeItemIdByOrderableCode.get(orderableCode));
      return false;
    }

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    HttpEntity<String> body = new HttpEntity<>(json, headers);
    URI uri = createUri(url, parameters);
    String response = null;
    try {
      if (getCreateMethod() == HttpMethod.POST) {
        logger.debug("POST {}", uri);
        logger.debug(body.getBody());
        restTemplate.postForEntity(uri, body, Object.class);
      } else if (getCreateMethod() == HttpMethod.PUT) {
        logger.debug("PUT {}", uri);
        logger.debug(body.getBody());
        response = restTemplate.exchange(uri, HttpMethod.PUT, body, String.class).getBody();
      } else {
        logger.error("Unsupported HTTP method provided: {}", getCreateMethod().name());
      }
    } catch (RestClientResponseException ex) {
      logger.error("Can not create resource: {}", ex.getResponseBodyAsString());
      return false;
    } catch (RestClientException ex) {
      logger.error("Can not create resource: {}", ex.getMessage());
      return false;
    }
    cacheTradeItem(json, response);
    return true;
  }

  @Override
  public boolean updateResource(JsonObject jsonObject, String id) {
    String productCode = jsonObject.getString(PRODUCT_CODE);

    String tradeItemId = findTradeItemIdByOrderableCode(productCode);

    if (tradeItemId == null) {
      logger.error("Can't find an trade item for product with code {}.", productCode);
      return false;
    }
    JsonObjectBuilder builder = Json.createObjectBuilder();
    jsonObject.forEach(builder::add);
    builder.add(ID, tradeItemId);
    
    return super.updateResource(builder.build(), "", false);
  }

  @Override
  public boolean deleteResource(String id) {
    logger.warn("Removing trade items is not supported. "
        + "Updating orderable must be implemented first.");
    return false;
  }

  private void cacheTradeItem(String json, String responseBody) {
    if (responseBody == null) {
      logger.error("Can not cache trade item, response is null");
      return;
    }
    JsonObject responseJson = this.convertToJsonObject(responseBody);
    JsonObject jsonObject = this.convertToJsonObject(json);

    String orderableCode = jsonObject.getString(PRODUCT_CODE);
    String tradeItemId = responseJson.getString(ID);

    logger.debug("Saving trade item with id {} associated with orderable code: {}", tradeItemId,
        orderableCode);
    cachedTradeItemIdByOrderableCode.put(orderableCode, tradeItemId);
  }

  private boolean isConnectedWithTradeItem(JsonObject json) {
    return json.getJsonObject(IDENTIFIERS).containsKey(TRADE_ITEM);
  }

}
