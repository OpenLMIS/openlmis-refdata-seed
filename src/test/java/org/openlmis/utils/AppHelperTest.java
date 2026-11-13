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

package org.openlmis.utils;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.openlmis.converter.Mapping;
import org.openlmis.converter.MappingConverter;
import org.openlmis.reader.Reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class AppHelperTest {
  private static final SourceFile SOURCE = SourceFile.FACILITIES;
  private static final String DIRECT_TYPE = "DIRECT";
  private static final String FROM_FILE_TYPE = "TO_ARRAY_FROM_FILE_BY_CODE";
  private static final String FROM = "programCode";
  private static final String FACILITY_CODE = "facilityCode";
  private static final String FACILITIES_CSV = "Facilities.csv";
  private static final String FACILITIES_MAPPING_CSV = "Facilities_mapping.csv";
  private static final String SUPPORTED_PROGRAMS_CSV = "SupportedPrograms.csv";
  private static final String CSV_EXTENSION = ".csv";
  private static final String TEST_CSV_FILE = "test.csv";
  private static final String TEST_MAPPING_FILE = "test_mapping.csv";
  private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

  @Mock
  private File inputFile;

  @Mock
  private File mappingFile;

  @Mock
  private Configuration configuration;

  @Mock
  private Mapping mapping;

  @Mock
  private MappingConverter mappingConverter;

  @Mock
  private Reader reader;

  @InjectMocks
  private AppHelper appHelper;

  private List<Mapping> mappings;

  private List<Map<String, String>> csv = Collections
      .singletonList(Collections.singletonMap("fieldName", "value"));

  @Before
  public void setUp() {
    when(configuration.getDirectory()).thenReturn(System.getProperty(JAVA_IO_TMPDIR));
    when(mapping.getFrom()).thenReturn(FROM);
    when(mapping.getType()).thenReturn(FROM_FILE_TYPE);

    mappings = Lists.newArrayList(mapping);
  }

  @Test
  public void shouldProcessIfFilesExists() {
    when(inputFile.exists()).thenReturn(true);
    when(mappingFile.exists()).thenReturn(true);

    assertThat(appHelper.inputAndMappingFileExist(inputFile, mappingFile, SOURCE), is(true));
  }

  @Test
  public void shouldNotProcessIfOneOfFileNotExist() {
    when(inputFile.exists()).thenReturn(false);
    when(mappingFile.exists()).thenReturn(true);

    assertThat(appHelper.inputAndMappingFileExist(inputFile, mappingFile, SOURCE), is(false));

    when(inputFile.exists()).thenReturn(true);
    when(mappingFile.exists()).thenReturn(false);

    assertThat(appHelper.inputAndMappingFileExist(inputFile, mappingFile, SOURCE), is(false));
  }

  @Test
  public void shouldNotProcessIfBothFilesNotExist() throws Exception {
    when(inputFile.exists()).thenReturn(false);
    when(mappingFile.exists()).thenReturn(false);

    assertThat(appHelper.inputAndMappingFileExist(inputFile, mappingFile, SOURCE), is(false));
  }

  @Test
  public void shouldProcessIfMappingIsValid() throws Exception {
    Path inputPath = Paths.get(System.getProperty(JAVA_IO_TMPDIR), TEST_CSV_FILE);
    Path mappingPath = Paths.get(System.getProperty(JAVA_IO_TMPDIR), TEST_MAPPING_FILE);

    Files.deleteIfExists(inputPath);
    Files.deleteIfExists(mappingPath);

    Files.createFile(inputPath);
    Files.createFile(mappingPath);

    when(mapping.getEntityName()).thenReturn(TEST_CSV_FILE);

    assertThat(appHelper.shouldProcess(SOURCE, mappings), is(true));
  }

  @Test
  public void shouldNotProcessIfMappingIsInvalid() throws Exception {
    when(mapping.getEntityName()).thenReturn("invalid.csv");

    assertThat(appHelper.shouldProcess(SOURCE, mappings), is(false));
  }

  @Test
  public void shouldProcessIfFromIsWrittenInLowerCamelCase() {
    when(mapping.getType()).thenReturn(DIRECT_TYPE);

    assertThat(appHelper.shouldProcess(SOURCE, mappings), is(true));
  }

  @Test
  public void shouldNotProcessIfFromIsNotWrittenInUpperCamelCase() {
    when(mapping.getType()).thenReturn(DIRECT_TYPE);
    when(mapping.getFrom()).thenReturn("UpperCamelCase");

    assertThat(appHelper.shouldProcess(SOURCE, mappings), is(false));
  }

  @Test
  public void shouldNotProcessIfFromPhraseOfDependingMappingsIsNotWrittenInUpperCamelCase()
      throws Exception {
    Path inputPath = Paths.get(System.getProperty(JAVA_IO_TMPDIR), TEST_CSV_FILE);
    Path mappingPath = Paths.get(System.getProperty(JAVA_IO_TMPDIR), TEST_MAPPING_FILE);

    Files.deleteIfExists(inputPath);
    Files.deleteIfExists(mappingPath);

    Files.createFile(inputPath);
    Files.createFile(mappingPath);

    Mapping dependedOnMapping = mock(Mapping.class);
    when(dependedOnMapping.getType()).thenReturn(DIRECT_TYPE);
    when(dependedOnMapping.getFrom()).thenReturn("UpperCamelCase");

    when(mapping.getEntityName()).thenReturn(TEST_CSV_FILE);
    when(mappingConverter.getMappingForFile(any()))
        .thenReturn(Collections.singletonList(dependedOnMapping));

    assertThat(appHelper.shouldProcess(SOURCE, mappings), is(false));
  }

  @Test
  public void shouldProcessIfFromPhraseOfDependingMappingsIsWrittenInCamelCase()
      throws Exception {
    Path inputPath = Paths.get(System.getProperty(JAVA_IO_TMPDIR), TEST_CSV_FILE);
    Path mappingPath = Paths.get(System.getProperty(JAVA_IO_TMPDIR), TEST_MAPPING_FILE);

    Files.deleteIfExists(inputPath);
    Files.deleteIfExists(mappingPath);

    Files.createFile(inputPath);
    Files.createFile(mappingPath);

    Mapping dependedOnMapping = mock(Mapping.class);
    when(dependedOnMapping.getType()).thenReturn(DIRECT_TYPE);
    when(dependedOnMapping.getFrom()).thenReturn("camelCase");

    when(mapping.getEntityName()).thenReturn(TEST_CSV_FILE);
    when(mappingConverter.getMappingForFile(any()))
        .thenReturn(Collections.singletonList(dependedOnMapping));

    assertThat(appHelper.shouldProcess(SOURCE, mappings), is(true));
  }

  @Test
  public void shouldReadMappingsByEntityName() {
    ArgumentCaptor<File> fileCapture = ArgumentCaptor.forClass(File.class);
    String entityFileName = SOURCE.getName() + CSV_EXTENSION;
    String expectedPath = SOURCE.getFullMappingFileName(configuration.getDirectory());

    when(mappingConverter.getMappingForFile(fileCapture.capture())).thenReturn(mappings);

    assertThat(appHelper.readMappings(entityFileName), is(mappings));
    assertThat(fileCapture.getValue().getAbsolutePath(), is(expectedPath));
  }

  @Test
  public void shouldReadCashedMappingsByEntityName() {
    ArgumentCaptor<File> fileCapture = ArgumentCaptor.forClass(File.class);
    String entityFileName = SOURCE.getName() + CSV_EXTENSION;

    when(mappingConverter.getMappingForFile(fileCapture.capture())).thenReturn(mappings);

    assertThat(appHelper.readMappings(entityFileName), is(mappings));
    assertThat(appHelper.readMappings(entityFileName), is(mappings));
    verify(mappingConverter, times(1)).getMappingForFile(any());
  }

  @Test
  public void shouldReadCsvByEntityName() {
    ArgumentCaptor<File> fileCapture = ArgumentCaptor.forClass(File.class);
    String entityFileName = SOURCE.getName() + CSV_EXTENSION;
    String expectedPath = SOURCE.getFullFileName(configuration.getDirectory());

    when(reader.readFromFile(fileCapture.capture())).thenReturn(csv);

    assertThat(appHelper.readCsv(entityFileName), is(csv));
    assertThat(fileCapture.getValue().getAbsolutePath(), is(expectedPath));
  }

  @Test
  public void shouldReadCashedCsvByEntityName() {
    ArgumentCaptor<File> fileCapture = ArgumentCaptor.forClass(File.class);
    String entityFileName = SOURCE.getName() + CSV_EXTENSION;

    when(reader.readFromFile(fileCapture.capture())).thenReturn(csv);

    assertThat(appHelper.readCsv(entityFileName), is(csv));
    assertThat(appHelper.readCsv(entityFileName), is(csv));
    verify(reader, times(1)).readFromFile(any());
  }

  @Test
  public void shouldReturnUnmappedColumns() {
    List<String> unmapped = appHelper.warnUnmappedColumns(SUPPORTED_PROGRAMS_CSV,
        Collections.singletonList(supportedProgramRow()), Lists.newArrayList(
            new Mapping(FROM, "code", DIRECT_TYPE, "", ""),
            new Mapping("active", "supportActive", DIRECT_TYPE, "", "")));

    assertThat(unmapped, contains(FACILITY_CODE, "startDate", "locallyFulfilled"));
  }

  @Test
  public void shouldTreatSkipMappedColumnAsMapped() {
    List<String> unmapped = appHelper.warnUnmappedColumns(SUPPORTED_PROGRAMS_CSV,
        Collections.singletonList(supportedProgramRow()), Lists.newArrayList(
            new Mapping(FACILITY_CODE, "", "SKIP", "", ""),
            new Mapping(FROM, "code", DIRECT_TYPE, "", ""),
            new Mapping("active", "supportActive", DIRECT_TYPE, "", "")));

    assertThat(unmapped, contains("startDate", "locallyFulfilled"));
  }

  @Test
  public void shouldNotWarnForHeaderOnlyFile() {
    List<String> unmapped = appHelper.warnUnmappedColumns(SUPPORTED_PROGRAMS_CSV,
        Collections.emptyList(), Collections.emptyList());

    assertThat(unmapped, is(empty()));
  }

  @Test
  public void shouldReportCsvNeitherSeededNorReferenced() throws IOException {
    givenInputDirectory(SUPPORTED_PROGRAMS_CSV, "RequisitionGroups.csv",
        "RequisitionGroups_mapping.csv");

    assertThat(appHelper.findUnreadInputFiles(), contains(SUPPORTED_PROGRAMS_CSV));
  }

  @Test
  public void shouldNotReportChildFileReferencedBySeededFile() throws IOException {
    Path directory = givenInputDirectory(FACILITIES_CSV, FACILITIES_MAPPING_CSV,
        SUPPORTED_PROGRAMS_CSV);
    givenFileReference(directory, FACILITIES_MAPPING_CSV, SUPPORTED_PROGRAMS_CSV);

    assertThat(appHelper.findUnreadInputFiles(), is(empty()));
  }

  @Test
  public void shouldReportChildFileWhenItsParentFileIsMissing() throws IOException {
    Path directory = givenInputDirectory(FACILITIES_MAPPING_CSV, SUPPORTED_PROGRAMS_CSV,
        "SupportedPrograms_mapping.csv");
    givenFileReference(directory, FACILITIES_MAPPING_CSV, SUPPORTED_PROGRAMS_CSV);

    assertThat(appHelper.findUnreadInputFiles(), contains(SUPPORTED_PROGRAMS_CSV));
  }

  @Test
  public void shouldNotReportFileReferencedByChildFile() throws IOException {
    Path directory = givenInputDirectory(FACILITIES_CSV, FACILITIES_MAPPING_CSV,
        SUPPORTED_PROGRAMS_CSV, "SupportedPrograms_mapping.csv", "ProgramDetails.csv");
    givenFileReference(directory, FACILITIES_MAPPING_CSV, SUPPORTED_PROGRAMS_CSV);
    givenFileReference(directory, "SupportedPrograms_mapping.csv", "ProgramDetails.csv");

    assertThat(appHelper.findUnreadInputFiles(), is(empty()));
  }

  @Test
  public void shouldNotReportMappingFiles() throws IOException {
    givenInputDirectory("Orphan_mapping.csv");

    assertThat(appHelper.findUnreadInputFiles(), is(empty()));
  }

  private Map<String, String> supportedProgramRow() {
    return ImmutableMap.of(FACILITY_CODE, "HC02", FROM, "HIV", "active", "TRUE",
        "startDate", "2026-09-01", "locallyFulfilled", "FALSE");
  }

  private Path givenInputDirectory(String... fileNames) throws IOException {
    Path directory = Files.createTempDirectory("seed-input");
    for (String fileName : fileNames) {
      Files.createFile(directory.resolve(fileName));
    }
    when(configuration.getDirectory()).thenReturn(directory.toString());
    return directory;
  }

  private void givenFileReference(Path directory, String mappingFileName,
      String referencedFileName) {
    when(mappingConverter.getMappingForFile(directory.resolve(mappingFileName).toFile()))
        .thenReturn(Collections.singletonList(new Mapping("children", "children",
            "TO_ARRAY_FROM_FILE_BY_CODE", referencedFileName, "")));
  }
}
