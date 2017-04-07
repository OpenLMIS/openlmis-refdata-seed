package mw.gov.health.lmis.upload;

import static mw.gov.health.lmis.upload.RequestHelper.createUri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

public abstract class BaseCommunicationService {
  private static final String ACCESS_TOKEN = "access_token";

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected RestOperations restTemplate = new RestTemplate();

  @Autowired
  protected AuthService authService;

  @Autowired
  protected Configuration configuration;

  protected abstract String getUrl();


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
        return convertToJsonArray(response.getBody());
      } else {
        JsonObject object = convertToJsonObject(response.getBody());
        return object.getJsonArray("content");
      }
    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
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

    try {
      restTemplate.postForEntity(createUri(url, parameters), body, Object.class);
    } catch (RestClientResponseException ex) {
      logger.error("Can not create resource: {}", ex.getResponseBodyAsString());
      return false;
    } catch (RestClientException ex) {
      logger.error("Can not create resource: {}", ex.getMessage());
      return false;
    }
    return true;
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
}
