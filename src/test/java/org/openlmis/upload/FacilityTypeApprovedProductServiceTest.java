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
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestOperations;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class FacilityTypeApprovedProductServiceTest {

  private static final String FACILITY_TYPE = "facilityType";
  private static final String PROGRAM = "program";
  private static final String ORDERABLE = "orderable";
  private static final String TYPE_ID = "type-1";
  private static final String PROGRAM_ID = "program-1";
  private static final String ORDERABLE_ID = "orderable-1";
  private static final String WAREHOUSE = "warehouse";
  private static final String HEALTH_CENTRE = "health_centre";
  private static final String HOST = "http://olmis.test";
  private static final String FTAP_ID = "ftap-1";

  @Mock
  private OrderableService orderableService;

  @Mock
  private FacilityTypeService facilityTypeService;

  @Mock
  private Configuration configuration;

  @Mock
  private RestOperations restTemplate;

  @Mock
  private AuthService authService;

  private FacilityTypeApprovedProductService service;

  @Before
  public void setUp() {
    service = spy(new FacilityTypeApprovedProductService());
    ReflectionTestUtils.setField(service, "orderableService", orderableService);
    ReflectionTestUtils.setField(service, "facilityTypeService", facilityTypeService);
    ReflectionTestUtils.setField(service, "configuration", configuration);
    when(configuration.isUpdateAllowed()).thenReturn(true);
    when(facilityTypeService.findAll()).thenReturn(Json.createArrayBuilder()
        .add(Json.createObjectBuilder().add("code", WAREHOUSE))
        .add(Json.createObjectBuilder().add("code", HEALTH_CENTRE))
        .build());
  }

  @Test
  public void shouldDropTheOrderableCacheEvenWhenUpdatesAreDisabled() {
    when(configuration.isUpdateAllowed()).thenReturn(false);

    service.before();

    verify(orderableService).invalidateCache();
    verify(facilityTypeService, never()).findAll();
  }

  @Test
  public void shouldLoadExistingProductsPerFacilityTypeWhenUpdatesAreAllowed() {
    givenEachFacilityTypeReturnsItsOwnProduct();

    service.before();

    ArgumentCaptor<Map> searches = ArgumentCaptor.forClass(Map.class);
    verify(orderableService).invalidateCache();
    verify(service, times(2)).search(searches.capture());
    assertThat(searches.getAllValues().get(0).get("facilityTypeCodes"),
        is(singletonList(WAREHOUSE)));
    assertThat(searches.getAllValues().get(1).get("facilityTypeCodes"),
        is(singletonList(HEALTH_CENTRE)));
    assertThat(service.findUnique(ftapOf(WAREHOUSE)), is(existingFtapOf(WAREHOUSE)));
    assertThat(service.findUnique(ftapOf(HEALTH_CENTRE)), is(existingFtapOf(HEALTH_CENTRE)));
  }

  @Test
  public void shouldMatchOnTheIdOfAllThreeReferencesWhateverElseTheyCarry() {
    doReturn(Json.createArrayBuilder().add(existingFtap()).build())
        .when(service).search(anyMapOf(String.class, Object.class));
    service.before();

    assertThat(service.findUnique(candidate()), is(existingFtap()));
    assertThat(service.findUnique(candidateWith(PROGRAM, "other-program")), is(nullValue()));
    assertThat(service.findUnique(candidateWith(ORDERABLE, "other-orderable")), is(nullValue()));
    assertThat(service.findUnique(candidateWith(FACILITY_TYPE, "other-type")), is(nullValue()));
  }

  @Test
  public void shouldNotMatchACandidateMissingAnyOfTheThreeReferences() {
    doReturn(Json.createArrayBuilder().add(ftap()).build())
        .when(service).search(anyMapOf(String.class, Object.class));
    service.before();

    assertThat(service.findUnique(candidateWithout(FACILITY_TYPE)), is(nullValue()));
    assertThat(service.findUnique(candidateWithout(PROGRAM)), is(nullValue()));
    assertThat(service.findUnique(candidateWithout(ORDERABLE)), is(nullValue()));
  }

  @Test
  public void shouldNeedAnUpdateOnlyForAProductThatDiffers() {
    assertThat(service.isUpdateNeeded(ftap(), ftap()), is(false));

    JsonObject withADifferentStockLevel = Json.createObjectBuilder()
        .add(FACILITY_TYPE, reference(TYPE_ID))
        .add(PROGRAM, reference(PROGRAM_ID))
        .add(ORDERABLE, reference(ORDERABLE_ID))
        .add("maxPeriodsOfStock", "3")
        .build();

    assertThat(service.isUpdateNeeded(withADifferentStockLevel, ftap()), is(true));
  }

  @Test
  public void shouldPutTheProductUnderItsOwnIdWithTheEmptyVersionMetadataTheApiNeeds() {
    ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(service, "authService", authService);
    ReflectionTestUtils.setField(service, BaseCommunicationService.class, "configuration",
        configuration, Configuration.class);
    when(configuration.getHost()).thenReturn(HOST);
    when(authService.obtainAccessToken()).thenReturn("token");

    assertThat(service.updateResource(ftap(), FTAP_ID), is(true));

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<HttpEntity> body = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplate).put(uri.capture(), body.capture());

    assertThat(uri.getValue().toString(),
        is(HOST + "/api/facilityTypeApprovedProducts/ftap-1?access_token=token"));

    JsonObject expected = Json.createObjectBuilder()
        .add(FACILITY_TYPE, reference(TYPE_ID))
        .add(PROGRAM, reference(PROGRAM_ID))
        .add(ORDERABLE, reference(ORDERABLE_ID))
        .add("meta", Json.createObjectBuilder())
        .add("id", FTAP_ID)
        .build();
    assertThat(read(String.valueOf(body.getValue().getBody())), is(expected));
  }

  private JsonObject read(String json) {
    try (JsonReader reader = Json.createReader(new StringReader(json))) {
      return reader.readObject();
    }
  }

  private void givenEachFacilityTypeReturnsItsOwnProduct() {
    doAnswer(invocation -> {
      Map<?, ?> searchParameters = (Map<?, ?>) invocation.getArguments()[0];
      List<?> codes = (List<?>) searchParameters.get("facilityTypeCodes");
      return Json.createArrayBuilder().add(existingFtapOf(String.valueOf(codes.get(0)))).build();
    }).when(service).search(anyMapOf(String.class, Object.class));
  }

  private JsonObject ftapOf(String facilityTypeCode) {
    return Json.createObjectBuilder()
        .add(FACILITY_TYPE, reference("type-" + facilityTypeCode))
        .add(PROGRAM, reference(PROGRAM_ID))
        .add(ORDERABLE, reference(ORDERABLE_ID))
        .build();
  }

  private JsonObject ftap() {
    return Json.createObjectBuilder()
        .add(FACILITY_TYPE, reference(TYPE_ID))
        .add(PROGRAM, reference(PROGRAM_ID))
        .add(ORDERABLE, reference(ORDERABLE_ID))
        .build();
  }

  private JsonObject existingFtap() {
    return Json.createObjectBuilder()
        .add(FACILITY_TYPE, apiReference(TYPE_ID))
        .add(PROGRAM, apiReference(PROGRAM_ID))
        .add(ORDERABLE, apiReference(ORDERABLE_ID))
        .build();
  }

  private JsonObject existingFtapOf(String facilityTypeCode) {
    return Json.createObjectBuilder()
        .add(FACILITY_TYPE, apiReference("type-" + facilityTypeCode))
        .add(PROGRAM, apiReference(PROGRAM_ID))
        .add(ORDERABLE, apiReference(ORDERABLE_ID))
        .build();
  }

  private JsonObject candidate() {
    return ftap();
  }

  private JsonObject candidateWithout(String parameter) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    if (!FACILITY_TYPE.equals(parameter)) {
      builder.add(FACILITY_TYPE, reference(TYPE_ID));
    }
    if (!PROGRAM.equals(parameter)) {
      builder.add(PROGRAM, reference(PROGRAM_ID));
    }
    if (!ORDERABLE.equals(parameter)) {
      builder.add(ORDERABLE, reference(ORDERABLE_ID));
    }
    return builder.build();
  }

  private JsonObject candidateWith(String parameter, String id) {
    JsonObjectBuilder builder = Json.createObjectBuilder()
        .add(FACILITY_TYPE, reference(FACILITY_TYPE.equals(parameter) ? id : TYPE_ID))
        .add(PROGRAM, reference(PROGRAM.equals(parameter) ? id : PROGRAM_ID))
        .add(ORDERABLE, reference(ORDERABLE.equals(parameter) ? id : ORDERABLE_ID));
    return builder.build();
  }

  private JsonObjectBuilder reference(String id) {
    return Json.createObjectBuilder().add("id", id);
  }

  private JsonObjectBuilder apiReference(String id) {
    return Json.createObjectBuilder().add("id", id).add("href", HOST + "/" + id);
  }
}
