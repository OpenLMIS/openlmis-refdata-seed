package mw.gov.health.lmis.upload;

import static mw.gov.health.lmis.upload.RequestHelper.createUri;

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

import mw.gov.health.lmis.Configuration;

import java.io.StringReader;
import java.net.URI;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
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

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected RestOperations restTemplate = new RestTemplate();

  @Autowired
  protected AuthService authService;

  @Autowired
  protected Configuration configuration;

  protected abstract String getUrl();

  public abstract JsonObject findUnique(JsonObject object);

  private JsonArray allResources;

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
      if (value.equals(read.getString())) {
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
    String url = buildUpdateUrl(configuration.getHost() + getUrl(), id);
    jsonObject = addIdToObject(jsonObject, id);

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> body = new HttpEntity<>(jsonObject.toString(), headers);
    URI uri = createUri(url, parameters);

    try {
      logger.info("PUT {}", uri);
      logger.info(body.getBody());
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
   * Attempts to create a new resource in OpenLMIS.
   *
   * @param json JSON representation of the resource to create
   * @return whether the attempt was successful
   */
  public boolean createResource(String json) {
    String url = configuration.getHost() + getUrl();

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> body = new HttpEntity<>(json, headers);
    URI uri = createUri(url, parameters);

    try {
      if (getCreateMethod() == HttpMethod.POST) {
        logger.info("POST {}", uri);
        logger.info(body.getBody());
        restTemplate.postForEntity(uri, body, Object.class);
      } else if (getCreateMethod() == HttpMethod.PUT) {
        logger.info("PUT {}", uri);
        logger.info(body.getBody());
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
   * HTTP method that is used to create the resource.
   * Specific services can override to whatever is appropriate.
   * @return HTTP method.
   */
  public HttpMethod getCreateMethod() {
    return HttpMethod.POST;
  }

  protected String buildUpdateUrl(String base, String id) {
    return base + "/" + id;
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

  private JsonObject convertToJsonObject(String body) {
    try (JsonReader reader = Json.createReader(new StringReader(body))) {
      return reader.readObject();
    }
  }

  private void invalidateCache() {
    allResources = null;
  }
}
