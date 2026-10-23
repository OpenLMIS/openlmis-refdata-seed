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

package org.openlmis.export.writer;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

@SuppressWarnings("PMD.TooManyMethods")
public class CsvWriterTest {

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String CLINIC = "Clinic";
  private static final String F1 = "F1";
  private static final List<String> HEADER = asList(CODE, NAME);
  private static final String FIRST_ROW = "F1,Clinic";
  private static final String EMPTY_NAME = "F1,";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Rule
  public ExpectedException expected = ExpectedException.none();

  private final CsvWriter writer = new CsvWriter();

  @Test
  public void shouldStartTheFileWithAByteOrderMark() throws IOException {
    assertThat(write(row(F1, CLINIC)), startsWith("\uFEFFcode,name"));
  }

  @Test
  public void shouldWriteValuesInHeaderOrderRegardlessOfRowOrder() throws IOException {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(NAME, CLINIC);
    row.put(CODE, F1);

    assertThat(line(write(row), 1), is(FIRST_ROW));
  }

  @Test
  public void shouldWriteAnEmptyValueForAColumnTheRowDoesNotHave() throws IOException {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(CODE, F1);

    assertThat(line(write(row), 1), is(EMPTY_NAME));
  }

  @Test
  public void shouldWriteAnEmptyValueForANullValue() throws IOException {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(CODE, F1);
    row.put(NAME, null);

    assertThat(line(write(row), 1), is(EMPTY_NAME));
  }

  @Test
  public void shouldWriteEveryRow() throws IOException {
    String written = write(row(F1, CLINIC), row("F2", "Depot"));

    assertThat(line(written, 1), is(FIRST_ROW));
    assertThat(line(written, 2), is("F2,Depot"));
  }

  @Test
  public void shouldQuoteValuesThatContainTheSeparator() throws IOException {
    assertThat(line(write(row(F1, "Clinic, Central")), 1), is("F1,\"Clinic, Central\""));
  }

  @Test
  public void shouldReplaceTheFileAnEarlierExportLeftBehindRatherThanAppendToIt()
      throws IOException {
    File file = folder.newFile();
    writer.write(file, HEADER, singletonList(row(F1, CLINIC)));

    writer.write(file, HEADER, singletonList(row("F2", "Depot")));

    assertThat(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
        .replace("\r\n", "\n"), is("\uFEFFcode,name\nF2,Depot\n"));
  }

  @Test
  public void shouldFailLoudlyWhenTheFileCannotBeWritten() throws IOException {
    File directory = folder.newFolder("not-a-file");

    expected.expect(IllegalStateException.class);
    expected.expectMessage(directory.getName());

    writer.write(directory, HEADER, singletonList(row(F1, CLINIC)));
  }

  @SafeVarargs
  private final String write(Map<String, String>... rows) throws IOException {
    File file = folder.newFile();
    writer.write(file, HEADER, asList(rows));
    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
  }

  private String line(String written, int index) {
    return written.split("\n")[index].trim();
  }

  private Map<String, String> row(String code, String name) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(CODE, code);
    row.put(NAME, name);
    return row;
  }
}
