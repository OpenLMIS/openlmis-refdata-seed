package org.openlmis;

import org.openlmis.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DataValidator {

  @Autowired
  private ApplicationContext context;

  public void validate() {
    context
        .getBeansOfType(Validator.class)
        .values()
        .forEach(Validator::validate);
  }

}
