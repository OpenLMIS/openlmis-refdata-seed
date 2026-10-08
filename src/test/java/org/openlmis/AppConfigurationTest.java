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

package org.openlmis;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.export.utils.DataExporter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class AppConfigurationTest {

  private static final String MODE = "mode";

  @Mock
  private DataSeeder seeder;

  @Mock
  private DataExporter exporter;

  @Mock
  private DataValidator validator;

  @Mock
  private ApplicationContext context;

  private final Configuration configuration = new Configuration();

  private final AppConfiguration appConfiguration = new AppConfiguration();

  @Test
  public void shouldSeedFirstAndOnlyThenValidateWhenNoModeIsConfigured() throws Exception {
    run();

    InOrder order = inOrder(seeder, validator);
    order.verify(seeder).seedData();
    order.verify(validator).validate();
    verify(exporter, never()).exportData();
  }

  @Test
  public void shouldOnlyExportWhenTheModeIsExport() throws Exception {
    configuration.setProperty(MODE, "EXPORT");

    run();

    verify(exporter).exportData();
    verify(seeder, never()).seedData();
    verify(validator, never()).validate();
  }

  private void run() throws Exception {
    CommandLineRunner runner = appConfiguration
        .commandLineRunner(seeder, exporter, validator, configuration, context);
    runner.run();
  }
}
