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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConfigurationTest {

  private static final String OUTPUT_DIRECTORY = "outputDirectory";
  private static final String UPDATE_ALLOWED = "updateAllowed";
  private static final String AUTO_VERIFY = "autoVerifyEmails";
  private static final String DEFAULT_OUTPUT = "/export";
  private static final String FALSE = "false";

  private final Configuration configuration = new Configuration();

  @Test
  public void shouldReturnTheConfiguredOutputDirectory() {
    configuration.setProperty(OUTPUT_DIRECTORY, "/tmp/out");

    assertThat(configuration.getOutputDirectory(), is("/tmp/out"));
  }

  @Test
  public void shouldFallBackToTheDefaultOutputDirectoryWhenUnsetOrBlank() {
    assertThat(configuration.getOutputDirectory(), is(DEFAULT_OUTPUT));

    configuration.setProperty(OUTPUT_DIRECTORY, "   ");
    assertThat(configuration.getOutputDirectory(), is(DEFAULT_OUTPUT));
  }

  @Test
  public void shouldAllowUpdatesUnlessExplicitlyTurnedOff() {
    assertThat(configuration.isUpdateAllowed(), is(true));

    configuration.setProperty(UPDATE_ALLOWED, "true");
    assertThat(configuration.isUpdateAllowed(), is(true));

    configuration.setProperty(UPDATE_ALLOWED, "no");
    assertThat(configuration.isUpdateAllowed(), is(true));
  }

  @Test
  public void shouldDisallowUpdatesOnlyForFalseInAnyCase() {
    configuration.setProperty(UPDATE_ALLOWED, FALSE);
    assertThat(configuration.isUpdateAllowed(), is(false));

    configuration.setProperty(UPDATE_ALLOWED, "FALSE");
    assertThat(configuration.isUpdateAllowed(), is(false));
  }

  @Test
  public void shouldVerifyEmailsUnlessExplicitlyTurnedOff() {
    assertThat(configuration.autoVerifyEmails(), is(true));

    configuration.setProperty(AUTO_VERIFY, FALSE);
    assertThat(configuration.autoVerifyEmails(), is(false));
  }

  @Test
  public void shouldReadTheOriginalMasterDataDirectoryFromItsExportPrefixedProperty() {
    configuration.setProperty("exportOriginalMasterDataDirectory", "/baseline");

    assertThat(configuration.getOriginalMasterDataDirectory(), is("/baseline"));
  }

  @Test
  public void shouldReturnNothingForPropertiesThatAreNotSet() {
    assertThat(configuration.getOriginalMasterDataDirectory(), is((String) null));
    assertThat(configuration.getDirectory(), is((String) null));
    assertThat(configuration.getMode(), is((String) null));
  }

  @Test
  public void shouldExposeTheConnectionProperties() {
    configuration.setProperty("host", "http://localhost");
    configuration.setProperty("login", "admin");
    configuration.setProperty("password", "secret");
    configuration.setProperty("clientId", "user-client");
    configuration.setProperty("clientSecret", "changeme");

    assertThat(configuration.getHost(), is("http://localhost"));
    assertThat(configuration.getLogin(), is("admin"));
    assertThat(configuration.getPassword(), is("secret"));
    assertThat(configuration.getClientId(), is("user-client"));
    assertThat(configuration.getClientSecret(), is("changeme"));
  }
}
