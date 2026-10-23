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

package org.openlmis;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.converter.Converter;
import org.openlmis.converter.Mapping;
import org.openlmis.converter.MappingConverter;
import org.openlmis.reader.GenericReader;
import org.openlmis.upload.BaseCommunicationService;
import org.openlmis.upload.Services;
import org.openlmis.utils.AppHelper;
import org.openlmis.utils.SourceFile;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class DataSeederTest {

  private static final SourceFile SOURCE = SourceFile.PROGRAMS;
  private static final String DIRECTORY = "/master-data";
  private static final String EXISTING_ID = "existing-id";
  private static final String CODE = "code";
  private static final String MEG = "MEG";
  private static final String VIH = "VIH";

  @Mock
  private Configuration configuration;

  @Mock
  private GenericReader reader;

  @Mock
  private Converter converter;

  @Mock
  private MappingConverter mappingConverter;

  @Mock
  private AppHelper appHelper;

  @Mock
  private Services services;

  @Mock
  private BaseCommunicationService service;

  @Mock
  private BaseCommunicationService tradeItemService;

  @Mock
  private BaseCommunicationService orderableService;

  @InjectMocks
  private DataSeeder seeder;

  private final JsonObject newObject = Json.createObjectBuilder().add(CODE, "MEG").build();
  private final JsonObject existingObject =
      Json.createObjectBuilder().add("id", EXISTING_ID).build();
  private final List<Mapping> mappings =
      singletonList(new Mapping(CODE, CODE, "DIRECT", "", ""));

  @Before
  public void setUp() {
    when(configuration.getDirectory()).thenReturn(DIRECTORY);
    when(configuration.isUpdateAllowed()).thenReturn(true);
    when(appHelper.inputAndMappingFileExist(any(File.class), any(File.class),
        any(SourceFile.class))).thenReturn(false);
    when(appHelper.inputAndMappingFileExist(
        eq(new File(SOURCE.getFullFileName(DIRECTORY))),
        eq(new File(SOURCE.getFullMappingFileName(DIRECTORY))),
        eq(SOURCE))).thenReturn(true);
    when(appHelper.shouldProcess(any(SourceFile.class), anyList())).thenReturn(true);
    when(mappingConverter.getMappingForFile(any(File.class))).thenReturn(mappings);
    when(reader.readFromFile(any(File.class))).thenReturn(singletonList(row(MEG)));
    when(services.getService(any(SourceFile.class))).thenReturn(service);
    when(converter.convert(anyMapOf(String.class, String.class), eq(mappings)))
        .thenReturn(newObject);
  }

  @Test
  public void shouldCreateAResourceThatDoesNotExistYet() {
    when(service.findUnique(newObject)).thenReturn(null);

    seeder.seedData();

    verify(service).before();
    verify(service).createResource(newObject.toString());
    verify(service).afterEach(newObject);
  }

  @Test
  public void shouldUpdateAnExistingResourceThatNeedsIt() {
    when(service.findUnique(newObject)).thenReturn(existingObject);
    when(service.isUpdateNeeded(newObject, existingObject)).thenReturn(true);

    seeder.seedData();

    verify(service).updateResource(newObject, EXISTING_ID);
    verify(service, never()).createResource(anyString());
  }

  @Test
  public void shouldLeaveAnExistingResourceThatNeedsNoUpdate() {
    when(service.findUnique(newObject)).thenReturn(existingObject);
    when(service.isUpdateNeeded(newObject, existingObject)).thenReturn(false);

    seeder.seedData();

    verify(service, never()).updateResource(any(JsonObject.class), anyString());
    verify(service, never()).createResource(anyString());
    verify(service).afterEach(newObject);
  }

  @Test
  public void shouldNotUpdateAnExistingResourceWhenUpdatesAreDisabled() {
    when(configuration.isUpdateAllowed()).thenReturn(false);
    when(service.findUnique(newObject)).thenReturn(existingObject);

    seeder.seedData();

    verify(service, never()).updateResource(any(JsonObject.class), anyString());
    verify(service, never()).createResource(anyString());
    verify(service, never()).isUpdateNeeded(any(JsonObject.class), any(JsonObject.class));
  }

  @Test
  public void shouldStillCreateMissingResourcesWhenUpdatesAreDisabled() {
    when(configuration.isUpdateAllowed()).thenReturn(false);
    when(service.findUnique(newObject)).thenReturn(null);

    seeder.seedData();

    verify(service).createResource(newObject.toString());
  }

  @Test
  public void shouldRunBeforeOnceAheadOfEveryRowAndAfterEachOnEachOfThem() {
    JsonObject second = Json.createObjectBuilder().add(CODE, VIH).build();
    when(reader.readFromFile(any(File.class))).thenReturn(asList(row(MEG), row(VIH)));
    when(converter.convert(eq(row(MEG)), eq(mappings))).thenReturn(newObject);
    when(converter.convert(eq(row(VIH)), eq(mappings))).thenReturn(second);
    when(service.findUnique(any(JsonObject.class))).thenReturn(null);

    seeder.seedData();

    InOrder order = inOrder(service);
    order.verify(service).before();
    order.verify(service).createResource(newObject.toString());
    order.verify(service).afterEach(newObject);
    order.verify(service).createResource(second.toString());
    order.verify(service).afterEach(second);
    verify(service, times(1)).before();
  }

  @Test
  public void shouldSkipEveryEntityWhoseInputOrMappingFileIsMissing() {
    when(appHelper.inputAndMappingFileExist(any(File.class), any(File.class),
        any(SourceFile.class))).thenReturn(false);

    seeder.seedData();

    verify(services, never()).getService(any(SourceFile.class));
    verify(service, never()).before();
  }

  @Test
  public void shouldSkipAnEntityTheHelperRejects() {
    when(appHelper.shouldProcess(any(SourceFile.class), anyList())).thenReturn(false);

    seeder.seedData();

    verify(reader, never()).readFromFile(any(File.class));
    verify(service, never()).before();
  }

  @Test
  public void shouldSeedTradeItemsBeforeOrderablesSoReferencesComeFirst() {
    givenSeedable(SourceFile.TRADE_ITEMS, tradeItemService);
    givenSeedable(SourceFile.ORDERABLES, orderableService);

    seeder.seedData();

    InOrder order = inOrder(tradeItemService, orderableService);
    order.verify(tradeItemService).createResource(newObject.toString());
    order.verify(orderableService).createResource(newObject.toString());
  }

  private void givenSeedable(SourceFile source, BaseCommunicationService forSource) {
    when(appHelper.inputAndMappingFileExist(
        eq(new File(source.getFullFileName(DIRECTORY))),
        eq(new File(source.getFullMappingFileName(DIRECTORY))),
        eq(source))).thenReturn(true);
    when(services.getService(source)).thenReturn(forSource);
    when(forSource.findUnique(newObject)).thenReturn(null);
  }

  private Map<String, String> row(String code) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(CODE, code);
    return row;
  }
}
