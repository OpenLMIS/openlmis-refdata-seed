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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.openlmis.converter.Mapping;
import org.openlmis.converter.MappingConverter;
import org.openlmis.export.converter.Deconverter;
import org.openlmis.export.writer.CsvWriter;
import org.openlmis.upload.BaseCommunicationService;
import org.openlmis.upload.Services;
import org.openlmis.utils.AppHelper;
import org.openlmis.utils.SourceFile;

@RunWith(MockitoJUnitRunner.class)
public class DataExporterTest {

  private static final SourceFile SOURCE = SourceFile.PROGRAMS;
  private static final String OUTPUT = "/out";
  private static final String MAPPINGS = "/mappings";
  private static final String CODE = "code";
  private static final List<String> HEADER = singletonList(CODE);
  private static final String CHILD_FILE = "RoleAssignments.csv";

  @Mock
  private Configuration configuration;

  @Mock
  private Deconverter deconverter;

  @Mock
  private CsvWriter writer;

  @Mock
  private MappingConverter mappingConverter;

  @Mock
  private Services services;

  @Mock
  private AppHelper appHelper;

  @Mock
  private ChildCsvCollector childCsvCollector;

  @Mock
  private OriginalCodeResolver originalCodeResolver;

  @Mock
  private SequentialCodeAllocator sequentialCodeAllocator;

  @Mock
  private MasterDataComparisonReporter comparisonReporter;

  @Mock
  private BaseCommunicationService service;

  @InjectMocks
  private DataExporter exporter;

  private final List<Mapping> mappings = singletonList(new Mapping(CODE, CODE, "DIRECT", "", ""));

  @Before
  public void setUp() {
    when(configuration.getOutputDirectory()).thenReturn(OUTPUT);
    when(configuration.getDirectory()).thenReturn(MAPPINGS);
    when(appHelper.createOutputDirectory(OUTPUT)).thenReturn(true);
    when(mappingConverter.getMappingForFile(any(File.class))).thenReturn(emptyList());
    when(services.getService(any(SourceFile.class))).thenReturn(service);
    when(service.findAllForExport()).thenReturn(Json.createArrayBuilder().build());
    when(deconverter.supportsAll(anyList())).thenReturn(true);
    when(deconverter.getHeader(anyList())).thenReturn(HEADER);
  }

  @Test
  public void shouldExportNothingWhenTheOutputDirectoryCannotBeCreated() {
    when(appHelper.createOutputDirectory(OUTPUT)).thenReturn(false);

    exporter.exportData();

    verify(services, never()).getService(any(SourceFile.class));
    verify(writer, never()).write(any(File.class), anyList(), anyList());
  }

  @Test
  public void shouldWriteOneFilePerEntityThatHasAMapping() {
    givenMappingFor(SOURCE);
    when(service.findAllForExport()).thenReturn(entities("F1"));
    when(deconverter.deconvert(any(JsonObject.class), eq(mappings)))
        .thenReturn(row("F1"));

    exporter.exportData();

    verify(writer).write(eq(new File(SOURCE.getFullFileName(OUTPUT))), eq(HEADER),
        eq(singletonList(row("F1"))));
  }

  @Test
  public void shouldSkipAnEntityWithNoMappingFile() {
    exporter.exportData();

    verify(writer, never()).write(any(File.class), anyList(), anyList());
  }

  @Test
  public void shouldSkipAnEntityWhoseMappingTypesAreNotAllSupported() {
    givenMappingFor(SOURCE);
    when(deconverter.supportsAll(mappings)).thenReturn(false);

    exporter.exportData();

    verify(deconverter).unsupportedTypes(mappings);
    verify(writer, never()).write(any(File.class), anyList(), anyList());
  }

  @Test
  public void shouldKeepGoingWhenOneEntityFails() {
    givenMappingFor(SOURCE);
    when(service.findAllForExport()).thenThrow(new IllegalStateException("boom"));

    exporter.exportData();

    verify(writer, never()).write(any(File.class), anyList(), anyList());
    verify(comparisonReporter).report();
  }

  @Test
  public void shouldFlushChildFilesAndReportAfterEveryEntity() {
    when(childCsvCollector.fileNames())
        .thenReturn(new LinkedHashSet<>(singletonList(CHILD_FILE)));
    when(childCsvCollector.header(CHILD_FILE)).thenReturn(HEADER);
    when(childCsvCollector.rows(CHILD_FILE)).thenReturn(singletonList(row("RA-1")));

    exporter.exportData();

    verify(writer).write(eq(new File(OUTPUT, CHILD_FILE)), eq(HEADER), anyList());
    verify(originalCodeResolver).logSummary();
    verify(sequentialCodeAllocator).logSummary();
    verify(comparisonReporter).report();
  }

  private void givenMappingFor(SourceFile source) {
    when(mappingConverter.getMappingForFile(new File(source.getFullMappingFileName(MAPPINGS))))
        .thenReturn(mappings);
  }

  private JsonArray entities(String code) {
    return Json.createArrayBuilder()
        .add(Json.createObjectBuilder().add(CODE, code))
        .build();
  }

  private Map<String, String> row(String code) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(CODE, code);
    return row;
  }
}
