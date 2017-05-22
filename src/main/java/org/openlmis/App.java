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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

import java.io.FileReader;
import java.io.IOException;

public class App {
  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
  private static final String CONFIG = "config.properties";

  /**
   * Application startup method.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(AppConfiguration.class);
    application.setBannerMode(Banner.Mode.LOG);
    Configuration configuration = new Configuration();
    try {
      configuration.load(new FileReader(CONFIG));

      application.addInitializers(
          cxt -> cxt
              .getBeanFactory()
              .registerSingleton(Configuration.class.getCanonicalName(), configuration)
      );

      application.setWebEnvironment(false);
      application.run();
    } catch (IOException ex) {
      LOGGER.error("Configuration file " + CONFIG + " not found, but required to run "
          + "the application.");
    }
  }
}
