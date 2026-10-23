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
import java.util.ArrayList;
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

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class MasterDataComparisonReporterTest {

  private static final String FILE = "Facilities.csv";
  private static final String OTHER_FILE = "Programs.csv";
  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String CLINIC = "Clinic";
  private static final String F1 = "F1";
  private static final String PRODUCT_CODE = "productCode";
  private static final String DEPOT = "Depot";
  private static final String TYPE = "type";
  private static final String WAREHOUSE = "warehouse";

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
    // asymmetric on purpose: symmetrical counts cannot tell New from Removed
    given(FILE,
        asList(row(CODE, F1, CLINIC), row(CODE, "F2", DEPOT), row(CODE, "F3", "Store")),
        asList(row(CODE, F1, CLINIC), row(CODE, "F2", "Warehouse"), row(CODE, "F4", "Post"),
            row(CODE, "F5", "Hub")));

    assertThat(lineFor("File"), is("File Identical Modified New Removed"));
    assertThat(lineFor(FILE), is("Facilities.csv 1 1 2 1"));
  }

  @Test
  public void shouldReportARowInNameOrderForEveryFileThatHasACounterpart() throws IOException {
    givenExportedWithNoCounterpart("AuthUsers.csv");
    given(OTHER_FILE, asList(row(CODE, "MEG", "Autres MEG")),
        asList(row(CODE, "MEG", "Autres MEG"), row(CODE, "VIH", "Programme VIH")));
    given(FILE, asList(row(CODE, F1, CLINIC)), asList(row(CODE, F1, DEPOT)));

    assertThat(reportRows(), is(asList(
        "File Identical Modified New Removed",
        "Facilities.csv 0 1 0 0",
        "Programs.csv 1 0 1 0")));
  }

  @Test
  public void shouldCompareEverySharedColumnNotJustTheFirstOne() throws IOException {
    given(FILE,
        asList(rowWithType(F1, CLINIC, WAREHOUSE), rowWithType("F2", DEPOT, WAREHOUSE)),
        asList(rowWithType(F1, CLINIC, "store"), rowWithType("F2", DEPOT, WAREHOUSE)));

    assertThat(lineFor(FILE), is("Facilities.csv 1 1 0 0"));
  }

  @Test
  public void shouldWidenTheKeyWhenEitherSideRepeatsIt() throws IOException {
    given(FILE,
        asList(row(CODE, F1, CLINIC), row(CODE, "F2", DEPOT)),
        asList(row(CODE, F1, DEPOT), row(CODE, F1, CLINIC)));
    given(OTHER_FILE,
        asList(row(CODE, F1, CLINIC), row(CODE, F1, DEPOT)),
        asList(row(CODE, "F2", CLINIC), row(CODE, F1, CLINIC)));

    assertThat(lineFor(FILE), is("Facilities.csv 1 0 1 1"));
    assertThat(lineFor(OTHER_FILE), is("Programs.csv 1 0 1 1"));
  }

  @Test
  public void shouldWidenTheKeyUntilItIdentifiesARowOnBothSides() throws IOException {
    Map<String, String> changed = row(CODE, F1, DEPOT);
    changed.put(PRODUCT_CODE, "P2");
    Map<String, String> original = row(CODE, F1, DEPOT);
    original.put(PRODUCT_CODE, "P1");

    given(FILE,
        asList(row(CODE, F1, CLINIC), original),
        asList(row(CODE, F1, CLINIC), changed));

    assertThat(lineFor(FILE), is("Facilities.csv 1 1 0 0"));
  }

  @Test
  public void shouldFallBackToAllValuesWhenNoColumnCanIdentifyARow() throws IOException {
    given(FILE,
        asList(row(CODE, "", CLINIC), row(CODE, "", DEPOT)),
        asList(row(CODE, "", CLINIC), row(CODE, "", "Post")));

    assertThat(lineFor(FILE), is("Facilities.csv 1 0 1 1"));
  }

  @Test
  public void shouldCompareNumbersByValueRatherThanText() throws IOException {
    given(FILE,
        asList(row(CODE, F1, "0"), row(CODE, "F2", "3")),
        asList(row(CODE, F1, "0.00"), row(CODE, "F2", "3.0")));

    assertThat(lineFor(FILE), is("Facilities.csv 2 0 0 0"));
  }

  @Test
  public void shouldCompareValuesIgnoringTheWhitespaceAroundThem() throws IOException {
    given(FILE,
        asList(row(CODE, F1, "Depot ZS Gombe"), row(CODE, "F2", CLINIC)),
        asList(row(CODE, F1, "Depot ZS Gombe "), row(CODE, " F2 ", CLINIC)));

    assertThat(lineFor(FILE), is("Facilities.csv 2 0 0 0"));
  }

  @Test
  public void shouldCompareOnlyTheColumnsBothSidesDeclare() throws IOException {
    Map<String, String> original = row(CODE, F1, CLINIC);
    original.put(TYPE, WAREHOUSE);
    Map<String, String> exported = row(CODE, F1, CLINIC);
    exported.put("extraData", "public");

    given(FILE, asList(original), asList(exported));

    assertThat(lineFor(FILE), is("Facilities.csv 1 0 0 0"));
  }

  @Test
  public void shouldReportNothingWhenThereIsNothingToCompare() throws IOException {
    new File(outputDirectory, FILE).createNewFile();
    assertThat(reporter.report(), is(nullValue()));

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

  private void givenExportedWithNoCounterpart(String fileName) throws IOException {
    new File(outputDirectory, fileName).createNewFile();
  }

  private List<String> reportRows() {
    List<String> rows = new ArrayList<>();
    for (String line : reporter.report().split(System.lineSeparator())) {
      rows.add(line.trim().replaceAll(" +", " "));
    }
    return rows;
  }

  private String lineFor(String fileName) {
    for (String line : reporter.report().split(System.lineSeparator())) {
      if (line.startsWith(fileName)) {
        return line.trim().replaceAll(" +", " ");
      }
    }
    return null;
  }




  private Map<String, String> row(String identityColumn, String identity, String name) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(identityColumn, identity);
    row.put(NAME, name);
    return row;
  }

  private Map<String, String> rowWithType(String code, String name, String type) {
    Map<String, String> row = row(CODE, code, name);
    row.put(TYPE, type);
    return row;
  }
}
