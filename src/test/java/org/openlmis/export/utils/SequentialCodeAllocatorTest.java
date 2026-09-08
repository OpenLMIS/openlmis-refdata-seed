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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.openlmis.reader.GenericReader;

@RunWith(MockitoJUnitRunner.class)
public class SequentialCodeAllocatorTest {

  private static final String FILE = "RoleAssignments.csv";
  private static final String CODE = "code";
  private static final String ROLE_NAME = "roleName";
  private static final List<String> HEADER = asList(CODE, ROLE_NAME);
  private static final String DIRECTORY = "/master-data";
  private static final String STORE_MANAGER = "STORE_MANAGER";

  @Mock
  private Configuration configuration;

  @Mock
  private GenericReader reader;

  @InjectMocks
  private SequentialCodeAllocator allocator;

  @Before
  public void setUp() {
    when(configuration.getOriginalMasterDataDirectory()).thenReturn(DIRECTORY);
  }

  @Test
  public void shouldContinueTheSequenceOfTheOriginalMasterData() {
    givenOriginalCodes("RA-1", "RA-934", "RA-7");

    assertThat(allocator.allocate(FILE, HEADER, row(STORE_MANAGER), CODE), is("RA-935"));
    assertThat(allocator.allocate(FILE, HEADER, row("STOCK_MANAGER"), CODE), is("RA-936"));
  }

  @Test
  public void shouldReturnTheSameCodeForTheSameRow() {
    givenOriginalCodes("RA-934");

    assertThat(allocator.allocate(FILE, HEADER, row(STORE_MANAGER), CODE), is("RA-935"));
    assertThat(allocator.allocate(FILE, HEADER, row(STORE_MANAGER), CODE), is("RA-935"));
  }

  @Test
  public void shouldKeepTheZeroPaddingOfTheOriginalMasterData() {
    givenOriginalCodes("RA-0007", "RA-0934");

    assertThat(allocator.allocate(FILE, HEADER, row(STORE_MANAGER), CODE), is("RA-0935"));
  }

  @Test
  public void shouldAllocateNothingWhenTheOriginalCodesAreNotSequential() {
    givenOriginalCodes("GR-PS-MEG-A", "GR-PS-CAN-S");

    assertThat(allocator.allocate(FILE, HEADER, row(STORE_MANAGER), CODE), is(nullValue()));
  }

  @Test
  public void shouldAllocateNothingWhenTheOriginalCodesMixPrefixes() {
    givenOriginalCodes("RA-1", "RB-2");

    assertThat(allocator.allocate(FILE, HEADER, row(STORE_MANAGER), CODE), is(nullValue()));
  }

  @Test
  public void shouldAllocateNothingWhenNoDirectoryIsConfigured() {
    when(configuration.getOriginalMasterDataDirectory()).thenReturn(null);

    assertThat(allocator.isEnabled(), is(false));
    assertThat(allocator.allocate(FILE, HEADER, row(STORE_MANAGER), CODE), is(nullValue()));
  }

  private void givenOriginalCodes(String... codes) {
    List<Map<String, String>> rows = new java.util.ArrayList<>();
    for (String code : codes) {
      Map<String, String> row = new LinkedHashMap<>();
      row.put(CODE, code);
      row.put(ROLE_NAME, code);
      rows.add(row);
    }
    when(reader.readFromFile(any(File.class))).thenReturn(rows);
  }

  private Map<String, String> row(String roleName) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(ROLE_NAME, roleName);
    return row;
  }
}
