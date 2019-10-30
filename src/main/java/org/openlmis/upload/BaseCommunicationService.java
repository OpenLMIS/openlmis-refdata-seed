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

import org.openlmis.Configuration;
import org.openlmis.utils.JsonObjectComparisonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.net.URI;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseCommunicationService {

  static final String ACCESS_TOKEN = "access_token";
  static final String ID = "id";
  static final String CODE = "code";
  static final String NAME = "name";

  static final String TOTAL_PAGES = "totalPages";
  static final String PAGE = "page";
  static final String CONTENT = "content";

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected RestOperations restTemplate = new RestTemplate();

  @Autowired
  protected AuthService authService;

  @Autowired
  protected Configuration configuration;
  private JsonArray allResources;

  protected abstract String getUrl();

  public abstract JsonObject findUnique(JsonObject object);

  /**
   * A method that is invoked before the seeding of the resources starts.
   * By default it does nothing.
   */
  public void before() {
    // Nothing by default
  }

  /**
   * A method that is invoked after seeding each of the single resource.
   * By default it does nothing.
   * @param object JsonObject which was just seeded
   */
  public void afterEach(JsonObject object) {
    // Nothing by default
  }

  /**
   * Finds the JSON representation of the resource by its code.
   *
   * @param code the value of the field to find
   * @return JsonObject by the code
   */
  public JsonObject findByCode(String code) {
    return findBy(CODE, code);
  }

  /**
   * Finds the JSON representation of the resource by its name.
   *
   * @param name the value of the field to find
   * @return JsonObject by the name
   */
  public JsonObject findByName(String name) {
    return findBy(NAME, name);
  }

  /**
   * Finds the JSON representation of the resource by its field.
   *
   * @param value the value of the field to find
   * @param by the field to look by
   * @return JsonObject by its field value
   */
  public JsonObject findBy(String by, String value) {
    JsonArray array = findAll();
    for (int i = 0; i < array.size(); i++) {
      JsonObject object = array.getJsonObject(i);
      JsonString read = object.getJsonString(by);
      if (value.equalsIgnoreCase(read.getString())) {
        return object;
      }
    }
    return null;
  }

  /**
   * Return one object from service.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return one reference data T objects.
   */
  public JsonObject findOne(String resourceUrl, RequestParameters parameters) {
    String url = configuration.getHost() + getUrl() + resourceUrl;

    RequestParameters params = RequestParameters
        .init()
        .setAll(parameters)
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    try {
      String json = restTemplate.getForEntity(createUri(url, params), String.class).getBody();
      return convertToJsonObject(json);
    } catch (HttpStatusCodeException ex) {
      // rest template will handle 404 as an exception, instead of returning null
      if (HttpStatus.NOT_FOUND == ex.getStatusCode()) {
        logger.warn(
            "{} matching params does not exist. Params: {}",
            Map.class.getSimpleName(), parameters
        );

        return null;
      }

      throw buildDataRetrievalException(ex);
    }
  }

  /**
   * Finds all resources.
   *
   * @return resources
   */
  public JsonArray findAll() {
    return findAll("", RequestParameters.init());
  }

  /**
   * Finds all resources using specified query parameters and URL.
   *
   * @param resourceUrl the relative URL to use for retrieval
   * @param parameters the URL params
   * @return resources
   */
  public JsonArray findAll(String resourceUrl, RequestParameters parameters) {
    if (allResources != null) {
      // Use cached version if available
      return allResources;
    }

    String url = configuration.getHost() + getUrl() + resourceUrl;

    RequestParameters params = RequestParameters
        .init()
        .setAll(parameters)
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    try {
      ResponseEntity<String> response = restTemplate.getForEntity(createUri(url, params),
          String.class);
      JsonStructure structure = convertToJsonStructure(response.getBody());
      if (structure.getValueType() == JsonValue.ValueType.ARRAY) {
        allResources = convertToJsonArray(response.getBody());
      } else {
        JsonObject object = convertToJsonObject(response.getBody());
        allResources = object.getJsonArray(CONTENT);

        for (int i = 1; i < object.getInt(TOTAL_PAGES); i++) {
          params.set(PAGE, i);
          response = restTemplate.getForEntity(createUri(url, params), String.class);
          object = convertToJsonObject(response.getBody());
          JsonArray pagedResources = object.getJsonArray(CONTENT);
          allResources = mergeJsonArrays(allResources, pagedResources);
        }
      }
      return allResources;
    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  /**
   * Searches the instances using the "/search" endpoint and POST HTTP method. It also supports
   * query parameters.
   *
   * @param searchParameters a map of parameters to use while searching
   * @return array of found instances
   */
  public JsonArray search(Map<String, String> searchParameters) {
    String url = configuration.getHost() + getUrl() + "/search";

    RequestParameters params = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(createUri(url, params),
          searchParameters, String.class);
      JsonStructure structure = convertToJsonStructure(response.getBody());
      if (structure.getValueType() == JsonValue.ValueType.ARRAY) {
        allResources = convertToJsonArray(response.getBody());
      } else {
        JsonObject object = convertToJsonObject(response.getBody());
        allResources = object.getJsonArray("content");
      }
      return allResources;
    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  /**
   * Attempts to update a resource in OpenLMIS.
   *
   * @param jsonObject JSON object representation of the resource to update
   * @param id the UUID of the resource that will be updated
   * @return whether the attempt was successful
   */
  public boolean updateResource(JsonObject jsonObject, String id) {
    return updateResource(jsonObject, id, true);
  }

  /**
   * Attempts to update a resource in OpenLMIS.
   *
   * @param jsonObject JSON object representation of the resource to update
   * @param id the UUID of the resource that will be updated
   * @param addIdToObject determines if id will be added to json object.
   * @return whether the attempt was successful
   */
  boolean updateResource(JsonObject jsonObject, String id, boolean addIdToObject) {
    String url = buildUpdateUrl(configuration.getHost() + getUrl(), id);

    if (addIdToObject) {
      jsonObject = addIdToObject(jsonObject, id);
    }

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    HttpEntity<String> body = new HttpEntity<>(jsonObject.toString(), headers);
    URI uri = createUri(url, parameters);

    try {
      logger.debug("PUT {}", uri);
      logger.debug(body.getBody());
      restTemplate.put(uri, body);
    } catch (RestClientResponseException ex) {
      logger.error("Can not update resource: {}", ex.getResponseBodyAsString());
      return false;
    } catch (RestClientException ex) {
      logger.error("Can not update resource: {}", ex.getMessage());
      return false;
    }

    invalidateCache();
    return true;
  }

  /**
   * Attempts to delete a resource in OpenLMIS.
   *
   * @param id the UUID of the resource that will be deleted
   * @return whether the attempt was successful
   */
  public boolean deleteResource(String id) {
    String url = buildDeleteUrl(configuration.getHost() + getUrl(), id);

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    URI uri = createUri(url, parameters);

    try {
      logger.debug("DELETE {}", uri);
      restTemplate.delete(uri);
    } catch (RestClientResponseException ex) {
      logger.error("Can not delete resource: {}", ex.getResponseBodyAsString());
      return false;
    } catch (RestClientException ex) {
      logger.error("Can not delete resource: {}", ex.getMessage());
      return false;
    }

    invalidateCache();
    return true;
  }

  /**
   * Attempts to create a new resource in OpenLMIS.
   *
   * @param json JSON representation of the resource to create
   * @return whether the attempt was successful
   */
  public boolean createResource(String json) {
    return createResource(configuration.getHost() + getUrl(), json);
  }

  /**
   * Attempts to create a new resource in OpenLMIS if URL structure differs from the casual one.
   *
   * @param url custom URL for creating resource endpoint
   * @param json JSON representation of the resource to create
   * @return whether the attempt was successful
   */
  public boolean createResource(String url, String json) {
    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    HttpEntity<String> body = new HttpEntity<>(json, headers);
    URI uri = createUri(url, parameters);

    try {
      if (getCreateMethod() == HttpMethod.POST) {
        logger.debug("POST {}", uri);
        logger.debug(body.getBody());
        restTemplate.postForEntity(uri, body, Object.class);
      } else if (getCreateMethod() == HttpMethod.PUT) {
        logger.debug("PUT {}", uri);
        logger.debug(body.getBody());
        restTemplate.put(uri, body);
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

    invalidateCache();
    return true;
  }

  /**
   * Checks if the update of existing entry is needed. By default, it is allowed to update all
   * entries. However, this behavior should be changed eg. in case of versioning entries.
   * Overwriting this method allows implementing custom logic of comparing existing entry with an
   * entry passed in an input file in order to prevent updates on each run of seedtool.
   *
   * @param newObject JSON which was created based on input data (CSV) and mappings.
   * @param existingObject JSON which represents object fetched from OLMIS.
   * @return a flag which determines if the update is needed.
   */
  public boolean isUpdateNeeded(JsonObject newObject, JsonObject existingObject) {
    return !JsonObjectComparisonUtils.equals(newObject, existingObject);
  }

  /**
   * HTTP method that is used to create the resource. Specific services can override to whatever is
   * appropriate.
   *
   * @return HTTP method.
   */
  protected HttpMethod getCreateMethod() {
    return HttpMethod.POST;
  }

  protected String buildUpdateUrl(String base, String id) {
    return base + "/" + id;
  }

  protected String buildDeleteUrl(String base, String id) {
    return base + "/" + id;
  }

  protected JsonObject convertToJsonObject(String body) {
    try (JsonReader reader = Json.createReader(new StringReader(body))) {
      return reader.readObject();
    }
  }

  private JsonObject addIdToObject(JsonObject jsonObject, String id) {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    for (Map.Entry<String, JsonValue> entry : jsonObject.entrySet()) {
      builder.add(entry.getKey(), entry.getValue());
    }
    builder.add(ID, id);

    return builder.build();
  }

  private DataRetrievalException buildDataRetrievalException(HttpStatusCodeException ex) {
    return new DataRetrievalException(Map.class.getSimpleName(),
        ex.getStatusCode(),
        ex.getResponseBodyAsString());
  }

  private JsonArray mergeJsonArrays(JsonArray allResources, JsonArray pagedResources) {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for (JsonValue value : allResources) {
      builder.add(value);
    }
    for (JsonValue value : pagedResources) {
      builder.add(value);
    }

    return builder.build();
  }

  private JsonStructure convertToJsonStructure(String body) {
    try (JsonReader reader = Json.createReader(new StringReader(body))) {
      return reader.read();
    }
  }

  private JsonArray convertToJsonArray(String body) {
    try (JsonReader reader = Json.createReader(new StringReader(body))) {
      return reader.readArray();
    }
  }

  public void invalidateCache() {
    allResources = null;
  }
}
