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

import static java.nio.charset.StandardCharsets.UTF_8;

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
            UriUtils.encodeQueryParam(String.valueOf(e.getValue()), UTF_8.name()));
      } catch (UnsupportedEncodingException ex) {
        LOGGER.error("UTF-8 encoding not supported.", ex);
      }
    });

    return builder.build(true).toUri();
  }

}
