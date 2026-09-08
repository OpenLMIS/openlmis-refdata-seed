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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ChildCsvCollectorTest {

  private static final String FILE = "RoleAssignments.csv";
  private static final String CODE = "code";
  private static final String ROLE_NAME = "roleName";
  private static final List<String> HEADER = asList(CODE, ROLE_NAME);
  private static final String RA_1 = "RA-1";

  private final ChildCsvCollector collector = new ChildCsvCollector();

  @Test
  public void shouldOrderRowsByTheirCodeNumerically() {
    collector.add(FILE, HEADER, row("RA-10", "c"));
    collector.add(FILE, HEADER, row("RA-2", "b"));
    collector.add(FILE, HEADER, row(RA_1, "a"));

    assertThat(codes(), contains(RA_1, "RA-2", "RA-10"));
  }

  @Test
  public void shouldOrderCodesWithoutATrailingNumberAlphabetically() {
    collector.add(FILE, HEADER, row("GR-PS-MEG-A", "b"));
    collector.add(FILE, HEADER, row("GR-PS-CAN-S", "a"));

    assertThat(codes(), contains("GR-PS-CAN-S", "GR-PS-MEG-A"));
  }

  @Test
  public void shouldBreakTiesOnTheRemainingColumns() {
    collector.add(FILE, HEADER, row(RA_1, "b"));
    collector.add(FILE, HEADER, row(RA_1, "a"));

    assertThat(collector.rows(FILE), hasSize(2));
    assertThat(names(), contains("a", "b"));
  }

  @Test
  public void shouldDeduplicateIdenticalRows() {
    collector.add(FILE, HEADER, row(RA_1, "a"));
    collector.add(FILE, HEADER, row(RA_1, "a"));

    assertThat(collector.rows(FILE), hasSize(1));
  }

  private List<String> codes() {
    return values(CODE);
  }

  private List<String> names() {
    return values(ROLE_NAME);
  }

  private List<String> values(String column) {
    List<String> values = new ArrayList<>();
    for (Map<String, String> row : collector.rows(FILE)) {
      values.add(row.get(column));
    }
    return values;
  }

  private Map<String, String> row(String code, String roleName) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(CODE, code);
    row.put(ROLE_NAME, roleName);
    return row;
  }
}
