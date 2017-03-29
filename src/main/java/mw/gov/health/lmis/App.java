package mw.gov.health.lmis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

public class App {

  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  /**
   * Application startup method.
   * @param args command line arguments
   */
  public static void main(String[] args) {
    Arguments arguments = new Arguments();

    JCommander commander = new JCommander();
    commander.setProgramName("Malawi Reference Data Seed");
    commander.addObject(arguments);

    try {
      commander.parse(args);

      SpringApplication application = new SpringApplication(AppConfiguration.class);
      application.setBannerMode(Banner.Mode.LOG);
      application.addInitializers(
          cxt -> cxt
              .getBeanFactory()
              .registerSingleton(Arguments.class.getCanonicalName(), arguments)
      );

      application.run();
    } catch (ParameterException exp) {
      LOGGER.error(exp.getMessage());
      commander.usage();
    }
  }
}
