package mw.gov.health.lmis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AppConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfiguration.class);

  /**
   * Here the application starts with spring context.
   */
  @Bean
  public CommandLineRunner commandLineRunner(DataSeeder seeder) {
    return args -> {
      startUp(seeder);
    };
  }

  private void startUp(DataSeeder seeder) {
    LOGGER.info("RUNNING");
    seeder.seedData();

  }
}
