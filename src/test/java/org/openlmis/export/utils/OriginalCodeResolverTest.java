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
public class OriginalCodeResolverTest {

  private static final String FILE = "RoleAssignments.csv";
  private static final String CODE = "code";
  private static final String ROLE_NAME = "roleName";
  private static final String PROGRAM_CODE = "programCode";
  private static final String DIRECTORY = "/master-data";
  private static final String STORE_MANAGER = "STORE_MANAGER";
  private static final String RA_0002 = "RA_0002";
  private static final String RA_0001 = "RA_0001";
  private static final String VIH = "VIH";
  private static final List<String> HEADER = asList(CODE, ROLE_NAME, PROGRAM_CODE);

  @Mock
  private Configuration configuration;

  @Mock
  private GenericReader reader;

  @InjectMocks
  private OriginalCodeResolver resolver;

  @Before
  public void setUp() {
    when(configuration.getOriginalMasterDataDirectory()).thenReturn(DIRECTORY);
    when(reader.readFromFile(any(File.class))).thenReturn(asList(
        originalRow(RA_0001, STORE_MANAGER, VIH),
        originalRow(RA_0002, "STOCK_MANAGER", "")));
  }

  @Test
  public void shouldReuseTheCodeOfAMatchingOriginalRow() {
    assertThat(resolver.findOriginalCode(FILE, HEADER, row(STORE_MANAGER, VIH), CODE), is(RA_0001));
  }

  @Test
  public void shouldTreatBlankAndAbsentColumnsAsEqual() {
    Map<String, String> withoutProgram = new LinkedHashMap<>();
    withoutProgram.put(ROLE_NAME, "STOCK_MANAGER");

    assertThat(resolver.findOriginalCode(FILE, HEADER, withoutProgram, CODE), is(RA_0002));
  }

  @Test
  public void shouldIgnoreColumnsTheExportDoesNotProduce() {
    when(reader.readFromFile(any(File.class))).thenReturn(asList(
        originalRowWithType(RA_0001, STORE_MANAGER, VIH, "supervision")));

    assertThat(resolver.findOriginalCode(FILE, HEADER, row(STORE_MANAGER, VIH), CODE), is(RA_0001));
  }

  @Test
  public void shouldNotMatchOriginalRowsDifferingInADeclaredColumn() {
    when(reader.readFromFile(any(File.class))).thenReturn(asList(
        originalRow(RA_0001, STORE_MANAGER, ""),
        originalRow(RA_0002, STORE_MANAGER, VIH)));

    Map<String, String> withoutProgram = new LinkedHashMap<>();
    withoutProgram.put(ROLE_NAME, STORE_MANAGER);

    assertThat(resolver.findOriginalCode(FILE, HEADER, withoutProgram, CODE), is(RA_0001));
  }

  @Test
  public void shouldReturnNothingWhenThereIsNoOriginalCounterpart() {
    assertThat(resolver.findOriginalCode(FILE, HEADER, row("NEW_ROLE", VIH), CODE),
        is(nullValue()));
  }

  @Test
  public void shouldBeDisabledWhenNoDirectoryIsConfigured() {
    when(configuration.getOriginalMasterDataDirectory()).thenReturn(null);

    assertThat(resolver.isEnabled(), is(false));
    assertThat(resolver.findOriginalCode(FILE, HEADER, row(STORE_MANAGER, VIH), CODE),
        is(nullValue()));
  }

  private Map<String, String> row(String roleName, String programCode) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put(ROLE_NAME, roleName);
    row.put(PROGRAM_CODE, programCode);
    return row;
  }

  private Map<String, String> originalRow(String code, String roleName, String programCode) {
    Map<String, String> row = row(roleName, programCode);
    row.put(CODE, code);
    return row;
  }

  private Map<String, String> originalRowWithType(String code, String roleName, String programCode,
      String type) {
    Map<String, String> row = originalRow(code, roleName, programCode);
    row.put("type", type);
    return row;
  }
}
