package mw.gov.health.lmis.upload;

import static mw.gov.health.lmis.upload.RequestHelper.createUri;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class CommodityTypeService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/commodityTypes";
  }

  @Override
  public boolean createResource(String json) {
    String url = configuration.getHost() + getUrl();

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> body = new HttpEntity<>(json, headers);

    try {
      restTemplate.put(createUri(url, parameters), body);
    } catch (RestClientResponseException ex) {
      logger.error("Can not create resource: {}", ex.getResponseBodyAsString());
      return false;
    } catch (RestClientException ex) {
      logger.error("Can not create resource: {}", ex.getMessage());
      return false;
    }

    return true;
  }

}
