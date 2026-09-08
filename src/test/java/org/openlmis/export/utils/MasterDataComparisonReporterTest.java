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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.openlmis.reader.GenericReader;

@RunWith(MockitoJUnitRunner.class)
public class MasterDataComparisonReporterTest {

  private static final String FILE = "Facilities.csv";
  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String CLINIC = "Clinic";
  private static final String F1 = "F1";
  private static final String PRODUCT_CODE = "productCode";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Mock
  private Configuration configuration;

  @Mock
  private GenericReader reader;

  @InjectMocks
  private MasterDataComparisonReporter reporter;

  private File originalDirectory;
  private File outputDirectory;

  @Before
  public void setUp() throws IOException {
    originalDirectory = folder.newFolder("original");
    outputDirectory = folder.newFolder("output");
    when(configuration.getOriginalMasterDataDirectory())
        .thenReturn(originalDirectory.getAbsolutePath());
    when(configuration.getOutputDirectory()).thenReturn(outputDirectory.getAbsolutePath());
  }

  @Test
  public void shouldCountUnchangedChangedAddedAndRemovedRows() throws IOException {
    given(FILE,
        asList(row(F1, CLINIC), row("F2", "Depot"), row("F3", "Store")),
        asList(row(F1, CLINIC), row("F2", "Warehouse"), row("F4", "Post")));

    assertThat(lineFor(FILE), is("Facilities.csv 1 1 1 1"));
  }

  @Test
  public void shouldIdentifyRowsByAllSharedValuesWhenCodesAreNotUnique() throws IOException {
    given(FILE,
        asList(row(F1, CLINIC), row(F1, "Depot")),
        asList(row(F1, CLINIC)));

    assertThat(lineFor(FILE), is("Facilities.csv 1 0 0 1"));
  }

  @Test
  public void shouldIdentifyRowsByTheLeadingUniqueColumnWhenThereIsNoCode() throws IOException {
    given(FILE,
        asList(row(PRODUCT_CODE, "P1", CLINIC), row(PRODUCT_CODE, "P2", "Depot")),
        asList(row(PRODUCT_CODE, "P1", "Renamed"), row(PRODUCT_CODE, "P3", "Post")));

    assertThat(lineFor(FILE), is("Facilities.csv 0 1 1 1"));
  }

  @Test
  public void shouldCompareOnlyTheColumnsBothSidesDeclare() throws IOException {
    Map<String, String> original = row(F1, CLINIC);
    original.put("type", "warehouse");

    given(FILE, asList(original), asList(row(F1, CLINIC)));

    assertThat(lineFor(FILE), is("Facilities.csv 1 0 0 0"));
  }

  @Test
  public void shouldReportNothingWhenNoExportedFileHasACounterpart() throws IOException {
    new File(outputDirectory, FILE).createNewFile();

    assertThat(reporter.report(), is(nullValue()));
  }

  @Test
  public void shouldReportNothingWhenNoDirectoryIsConfigured() {
    when(configuration.getOriginalMasterDataDirectory()).thenReturn(null);

    assertThat(reporter.report(), is(nullValue()));
  }

  private void given(String fileName, List<Map<String, String>> original,
      List<Map<String, String>> exported) throws IOException {
    File originalFile = new File(originalDirectory, fileName);
    File exportedFile = new File(outputDirectory, fileName);
    originalFile.createNewFile();
    exportedFile.createNewFile();
    when(reader.readFromFile(originalFile)).thenReturn(original);
    when(reader.readFromFile(exportedFile)).thenReturn(exported);
  }

  private String lineFor(String fileName) {
    for (String line : reporter.report().split(System.lineSeparator())) {
      if (line.startsWith(fileName)) {
        return line.trim().replaceAll(" +", " ");
      }
    }
    return null;
  }



  private Map<String, String> row(String identity, String name) {
    return row(CODE, identity, name);
  }

  private Map<String, String> row(String identityColumn, String identity, String name) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(identityColumn, identity);
    row.put(NAME, name);
    return row;
  }
}
