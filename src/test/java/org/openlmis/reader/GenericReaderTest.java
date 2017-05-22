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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class GenericReaderTest {

  private Reader reader = new GenericReader();

  @Test
  public void shouldReadFromFile() throws Exception {
    File file = ResourceUtils.getFile(getClass().getResource("/test.csv"));
    String filePath = file.getAbsolutePath();

    List<Map<String, String>> csvs = reader.readFromFile(filePath);

    assertThat(csvs, hasSize(3));

    Map<String, String> firstLine = csvs.get(0);
    assertThat(firstLine.get("one"), is(equalTo("1")));
    assertThat(firstLine.get("two"), is(equalTo("2")));
    assertThat(firstLine.get("three"), is(equalTo("3")));

    Map<String, String> secondLine = csvs.get(1);
    assertThat(secondLine.get("one"), is(equalTo("ala")));
    assertThat(secondLine.get("two"), is(equalTo("has")));
    assertThat(secondLine.get("three"), is(equalTo("cat")));

    Map<String, String> thirdLine = csvs.get(2);
    assertThat(thirdLine.get("one"), is(equalTo("2016-01-01")));
    assertThat(thirdLine.get("two"), is(equalTo("2017-05-05")));
    assertThat(thirdLine.get("three"), is(equalTo("2018-09-09")));
  }

  @Test
  public void shouldReturnEmptyListIfFileNotExist() throws Exception {
    assertThat(reader.readFromFile("abc"), hasSize(0));
  }

}
