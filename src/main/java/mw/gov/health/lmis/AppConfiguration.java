package mw.gov.health.lmis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AppConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfiguration.class);

  /**
   * Here the application starts with spring context.
   */
  @Bean
  public CommandLineRunner commandLineRunner(DataSeeder seeder, ApplicationContext context) {
    return args -> {
      startUp(seeder, context);
    };
  }

  private void startUp(DataSeeder seeder, ApplicationContext context) {
    LOGGER.info("RUNNING");
    seeder.seedData();
    LOGGER.info("Seeding complete. Exiting.");
    SpringApplication.exit(context);
  }
}
