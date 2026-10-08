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

package org.openlmis.export.utils;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.upload.BaseCommunicationService;
import org.openlmis.upload.Services;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceResolverTest {

  private static final String PROGRAM = "Program";
  private static final String CODE = "code";
  private static final String ID = "id";
  private static final String MEG = "MEG";
  private static final String MEG_ID = "11111111-1111-1111-1111-111111111111";
  private static final String VIH = "VIH";
  private static final String VIH_ID = "33333333-3333-3333-3333-333333333333";
  private static final String FACILITY = "Facility";
  private static final String FACILITY_ID = "22222222-2222-2222-2222-222222222222";

  @Mock
  private Services services;

  @Mock
  private BaseCommunicationService service;

  @Mock
  private BaseCommunicationService facilityService;

  @InjectMocks
  private ReferenceResolver resolver;

  @Before
  public void setUp() {
    when(services.getService(PROGRAM)).thenReturn(service);
  }

  @Test
  public void shouldResolveEveryIndexedIdToItsOwnObject() {
    when(service.findAll()).thenReturn(Json.createArrayBuilder()
        .add(entityBuilder(MEG_ID, MEG))
        .add(entityBuilder(VIH_ID, VIH))
        .build());

    assertThat(resolver.findById(PROGRAM, MEG_ID).getString(CODE), is(MEG));
    assertThat(resolver.findById(PROGRAM, VIH_ID).getString(CODE), is(VIH));
  }

  @Test
  public void shouldBuildTheIndexOncePerEntity() {
    givenEntities(entity(MEG_ID, MEG));
    when(services.getService(FACILITY)).thenReturn(facilityService);
    when(facilityService.findAll()).thenReturn(entity(FACILITY_ID, "ENTC001"));

    resolver.findById(PROGRAM, MEG_ID);
    resolver.findById(PROGRAM, MEG_ID);
    resolver.findById(FACILITY, FACILITY_ID);
    resolver.findById(FACILITY, FACILITY_ID);

    verify(service, times(1)).findAll();
    verify(facilityService, times(1)).findAll();
    assertThat(resolver.findById(FACILITY, FACILITY_ID).getString(CODE), is("ENTC001"));
    assertThat(resolver.findById(FACILITY, MEG_ID), is(nullValue()));
  }

  @Test
  public void shouldKeepIndexingPastEntriesThatAreNotObjectsOrHaveNoId() {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    builder.add("a bare string");
    builder.add(Json.createObjectBuilder().add(CODE, "no id here"));
    builder.add(Json.createObjectBuilder().addNull(ID).add(CODE, "null id"));
    builder.add(entityBuilder(MEG_ID, MEG));
    when(service.findAll()).thenReturn(builder.build());

    assertThat(resolver.findById(PROGRAM, MEG_ID).getString(CODE), is(MEG));
  }

  private void givenEntities(JsonArray entities) {
    when(service.findAll()).thenReturn(entities);
  }

  private JsonArray entity(String id, String code) {
    return Json.createArrayBuilder().add(entityBuilder(id, code)).build();
  }

  private JsonObjectBuilder entityBuilder(String id, String code) {
    return Json.createObjectBuilder().add(ID, id).add(CODE, code);
  }
}
