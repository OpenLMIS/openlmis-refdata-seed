package mw.gov.health.lmis.upload;

import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;

public final class RequestHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHelper.class);

  private RequestHelper() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a {@link URI} from the given string representation and with the given parameters.
   */
  public static URI createUri(String url, RequestParameters parameters) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance().uri(URI.create(url));

    parameters.forEach(e -> {
      try {
        builder.queryParam(e.getKey(),
            UriUtils.encodeQueryParam(String.valueOf(e.getValue()), Charsets.UTF_8.name()));
      } catch (UnsupportedEncodingException ex) {
        LOGGER.error("UTF-8 encoding not supported.");
      }
    });

    return builder.build(true).toUri();
  }

}
