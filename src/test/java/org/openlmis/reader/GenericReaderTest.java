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

package org.openlmis.reader;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openlmis.export.writer.CsvWriter;
import org.springframework.util.ResourceUtils;

public class GenericReaderTest {

  private static final String ONE = "one";
  private static final String TWO = "two";
  private static final String THREE = "three";
  private static final String MEG = "MEG";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private Reader reader = new GenericReader();

  @Test
  public void shouldReadFromFile() throws Exception {
    File file = ResourceUtils.getFile(getClass().getResource("/test.csv"));

    List<Map<String, String>> csvs = reader.readFromFile(file);

    assertThat(csvs, hasSize(3));

    Map<String, String> firstLine = csvs.get(0);
    assertThat(firstLine.get(ONE), is(equalTo("1")));
    assertThat(firstLine.get(TWO), is(equalTo("2")));
    assertThat(firstLine.get(THREE), is(equalTo("3")));

    Map<String, String> secondLine = csvs.get(1);
    assertThat(secondLine.get(ONE), is(equalTo("ala")));
    assertThat(secondLine.get(TWO), is(equalTo("has")));
    assertThat(secondLine.get(THREE), is(equalTo("cat")));

    Map<String, String> thirdLine = csvs.get(2);
    assertThat(thirdLine.get(ONE), is(equalTo("2016-01-01")));
    assertThat(thirdLine.get(TWO), is(equalTo("2017-05-05")));
    assertThat(thirdLine.get(THREE), is(equalTo("2018-09-09")));
  }

  @Test
  public void shouldReturnEmptyListIfFileNotExist() throws Exception {
    assertThat(reader.readFromFile(new File("abc")), hasSize(0));
  }

  @Test
  public void shouldReturnEmptyListForADirectory() throws Exception {
    assertThat(reader.readFromFile(folder.newFolder("a-directory")), hasSize(0));
  }

  @Test
  public void shouldReadBackWhatTheExporterWrote() throws Exception {
    File file = written(row(MEG, "Autres MEG"));

    Map<String, String> read = reader.readFromFile(file).get(0);

    assertThat(read.get(ONE), is(equalTo(MEG)));
    assertThat(read.get(TWO), is(equalTo("Autres MEG")));
  }

  @Test
  public void shouldReadAValueContainingTheSeparator() throws Exception {
    File file = written(row(MEG, "Clinic, Central"));

    assertThat(reader.readFromFile(file).get(0).get(TWO), is(equalTo("Clinic, Central")));
  }

  @Test
  public void shouldReadAColumnWithNoValueAsAnEmptyStringRatherThanNull() throws Exception {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(ONE, MEG);
    File file = written(row);

    Map<String, String> read = reader.readFromFile(file).get(0);

    assertThat(read.get(ONE), is(equalTo(MEG)));
    assertThat(read.get(TWO), is(equalTo("")));
  }

  private File written(Map<String, String> row) throws IOException {
    File file = folder.newFile();
    new CsvWriter().write(file, asList(ONE, TWO), singletonList(row));
    return file;
  }

  private Map<String, String> row(String one, String two) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(ONE, one);
    row.put(TWO, two);
    return row;
  }
}
