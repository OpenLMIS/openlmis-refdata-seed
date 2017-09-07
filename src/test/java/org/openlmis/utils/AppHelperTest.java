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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.Configuration;
import org.openlmis.converter.Mapping;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AppHelperTest {
  private static final SourceFile SOURCE = SourceFile.FACILITIES;
  private static final String TYPE = "TO_ARRAY_FROM_FILE_BY_CODE";

  @Mock
  private File inputFile;

  @Mock
  private File mappingFile;

  @Mock
  private Configuration configuration;

  @Mock
  private Mapping mapping;

  private List<Mapping> mappings;

  @Before
  public void setUp() throws Exception {
    when(configuration.getDirectory()).thenReturn(System.getProperty("java.io.tmpdir"));
    when(mapping.getType()).thenReturn(TYPE);

    mappings = Lists.newArrayList(mapping);
  }

  @Test
  public void shouldProcessIfFilesExists() throws Exception {
    when(inputFile.exists()).thenReturn(true);
    when(mappingFile.exists()).thenReturn(true);

    assertThat(AppHelper.shouldProcess(inputFile, mappingFile, SOURCE), is(true));
  }

  @Test
  public void shouldNotProcessIfOneOfFileNotExist() throws Exception {
    when(inputFile.exists()).thenReturn(false);
    when(mappingFile.exists()).thenReturn(true);

    assertThat(AppHelper.shouldProcess(inputFile, mappingFile, SOURCE), is(false));

    when(inputFile.exists()).thenReturn(true);
    when(mappingFile.exists()).thenReturn(false);

    assertThat(AppHelper.shouldProcess(inputFile, mappingFile, SOURCE), is(false));
  }

  @Test
  public void shouldNotProcessIfBothFilesNotExist() throws Exception {
    when(inputFile.exists()).thenReturn(false);
    when(mappingFile.exists()).thenReturn(false);

    assertThat(AppHelper.shouldProcess(inputFile, mappingFile, SOURCE), is(false));
  }

  @Test
  public void shouldProcessIfMappingIsValid() throws Exception {
    Path inputPath = Paths.get(System.getProperty("java.io.tmpdir"), "test.csv");
    Path mappingPath = Paths.get(System.getProperty("java.io.tmpdir"), "test_mapping.csv");

    Files.deleteIfExists(inputPath);
    Files.deleteIfExists(mappingPath);

    Files.createFile(inputPath);
    Files.createFile(mappingPath);

    when(mapping.getEntityName()).thenReturn("test.csv");

    assertThat(AppHelper.shouldProcess(configuration, SOURCE, mappings), is(true));
  }

  @Test
  public void shouldNotProcessIfMappingIsInvalid() throws Exception {
    when(mapping.getEntityName()).thenReturn("invalid.csv");

    assertThat(AppHelper.shouldProcess(configuration, SOURCE, mappings), is(false));
  }
}

