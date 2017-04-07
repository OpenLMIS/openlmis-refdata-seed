package mw.gov.health.lmis;

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

      application.run();
    } catch (IOException ex) {
      LOGGER.error("Configuration file " + CONFIG + " not found, but required to run "
          + "the application.");
    }
  }
}
