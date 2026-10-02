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

  @Mock
  private Services services;

  @Mock
  private BaseCommunicationService service;

  @InjectMocks
  private ReferenceResolver resolver;

  @Before
  public void setUp() {
    when(services.getService(PROGRAM)).thenReturn(service);
  }

  @Test
  public void shouldResolveAnIdToItsFullObject() {
    givenEntities(entity(MEG_ID, MEG));

    assertThat(resolver.findById(PROGRAM, MEG_ID).getString(CODE), is(MEG));
  }

  @Test
  public void shouldReturnNothingForAnIdThatDoesNotExist() {
    givenEntities(entity(MEG_ID, MEG));

    assertThat(resolver.findById(PROGRAM, "no-such-id"), is(nullValue()));
  }

  @Test
  public void shouldBuildTheIndexOncePerEntity() {
    givenEntities(entity(MEG_ID, MEG));

    resolver.findById(PROGRAM, MEG_ID);
    resolver.findById(PROGRAM, MEG_ID);

    verify(service, times(1)).findAll();
  }

  @Test
  public void shouldIgnoreEntriesThatAreNotObjectsOrHaveNoId() {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    builder.add("a bare string");
    builder.add(Json.createObjectBuilder().add(CODE, "no id here"));
    builder.add(Json.createObjectBuilder().addNull(ID).add(CODE, "null id"));
    builder.add(entityBuilder(MEG_ID, MEG));
    when(service.findAll()).thenReturn(builder.build());

    assertThat(resolver.findById(PROGRAM, MEG_ID).getString(CODE), is(MEG));
  }

  @Test
  public void shouldReturnNothingWhenTheEntityHasNoResourcesAtAll() {
    when(service.findAll()).thenReturn(Json.createArrayBuilder().build());

    assertThat(resolver.findById(PROGRAM, MEG_ID), is(nullValue()));
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
