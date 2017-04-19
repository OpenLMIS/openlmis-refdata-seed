package mw.gov.health.lmis.upload;

import static mw.gov.health.lmis.upload.RequestHelper.createUri;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import mw.gov.health.lmis.Configuration;
import mw.gov.health.lmis.utils.AuthorizationException;

import java.util.Map;

@Service
public class AuthService {

  public static final String ACCESS_TOKEN = "access_token";

  @Autowired
  private Configuration configuration;

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Retrieves access token from the auth service.
   *
   * @return token
   */
  public String obtainAccessToken() {
    String plainCreds = configuration.getClientId() + ":" + configuration.getClientSecret();
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + base64Creds);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("username", configuration.getLogin());
    form.add("password", configuration.getPassword());

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
    RequestParameters params = RequestParameters
        .init()
        .set("grant_type", "password");

    try {
      ResponseEntity<?> response = restTemplate.exchange(
          createUri(configuration.getHost() + "/api/oauth/token", params), HttpMethod.POST, request,
          Object.class
      );
      return ((Map<String, String>) response.getBody()).get(ACCESS_TOKEN);
    } catch (RestClientException ex) {
      throw new AuthorizationException("Cannot obtain access token using the provided credentials. "
          + "Please verify they are correct.", ex);
    }
  }

  void setRestTemplate(RestOperations restTemplate) {
    this.restTemplate = restTemplate;
  }
}
