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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class FacilityTypeApprovedProductServiceTest {

  private static final String FACILITY_TYPE = "facilityType";
  private static final String PROGRAM = "program";
  private static final String ORDERABLE = "orderable";
  private static final String TYPE_ID = "type-1";
  private static final String PROGRAM_ID = "program-1";
  private static final String ORDERABLE_ID = "orderable-1";

  @Mock
  private OrderableService orderableService;

  @Mock
  private FacilityTypeService facilityTypeService;

  @Mock
  private Configuration configuration;

  private FacilityTypeApprovedProductService service;

  @Before
  public void setUp() {
    service = spy(new FacilityTypeApprovedProductService());
    ReflectionTestUtils.setField(service, "orderableService", orderableService);
    ReflectionTestUtils.setField(service, "facilityTypeService", facilityTypeService);
    ReflectionTestUtils.setField(service, "configuration", configuration);
    when(configuration.isUpdateAllowed()).thenReturn(true);
    when(facilityTypeService.findAll()).thenReturn(Json.createArrayBuilder()
        .add(Json.createObjectBuilder().add("code", "warehouse"))
        .build());
  }

  @Test
  public void shouldAlwaysDropTheOrderableCacheSoProductsAreFresh() {
    when(configuration.isUpdateAllowed()).thenReturn(false);

    service.before();

    verify(orderableService).invalidateCache();
    verify(facilityTypeService, never()).findAll();
  }

  @Test
  public void shouldLoadExistingProductsPerFacilityTypeWhenUpdatesAreAllowed() {
    doReturn(Json.createArrayBuilder().add(ftap()).build())
        .when(service).search(anyMapOf(String.class, Object.class));

    service.before();

    verify(facilityTypeService).findAll();
    assertThat(service.findUnique(candidate()), is(ftap()));
  }

  @Test
  public void shouldMatchOnAllThreeOfFacilityTypeProgramAndOrderable() {
    doReturn(Json.createArrayBuilder().add(ftap()).build())
        .when(service).search(anyMapOf(String.class, Object.class));
    service.before();

    assertThat(service.findUnique(candidateWith(PROGRAM, "other-program")), is(nullValue()));
    assertThat(service.findUnique(candidateWith(ORDERABLE, "other-orderable")), is(nullValue()));
    assertThat(service.findUnique(candidateWith(FACILITY_TYPE, "other-type")), is(nullValue()));
  }

  @Test
  public void shouldNotMatchACandidateMissingAnyOfTheThreeReferences() {
    doReturn(Json.createArrayBuilder().add(ftap()).build())
        .when(service).search(anyMapOf(String.class, Object.class));
    service.before();

    JsonObject withoutProgram = Json.createObjectBuilder()
        .add(FACILITY_TYPE, reference(TYPE_ID))
        .add(ORDERABLE, reference(ORDERABLE_ID))
        .build();

    assertThat(service.findUnique(withoutProgram), is(nullValue()));
  }

  @Test
  public void shouldNotMatchAnythingBeforeTheExistingProductsAreLoaded() {
    assertThat(service.findUnique(candidate()), is(nullValue()));
  }

  private JsonObject ftap() {
    return Json.createObjectBuilder()
        .add(FACILITY_TYPE, reference(TYPE_ID))
        .add(PROGRAM, reference(PROGRAM_ID))
        .add(ORDERABLE, reference(ORDERABLE_ID))
        .build();
  }

  private JsonObject candidate() {
    return ftap();
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
}
