package mw.gov.health.lmis.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import mw.gov.health.lmis.utils.SourceFile;

@Service
public class Services {

  @Autowired
  private ApplicationContext applicationContext;

  /**
   * Retrieves service by the human readable name.
   *
   * @param name name of the service
   * @return the corresponding service
   */
  public BaseCommunicationService getService(String name) {
    String safeName = name.replace(" ", "");
    String serviceName = String.format(
        "%s%sService", Character.toLowerCase(safeName.charAt(0)), safeName.substring(1)
    );
    return applicationContext.getBean(serviceName, BaseCommunicationService.class);
  }

  /**
   * Retrieves service by the source file.
   *
   * @param source source file
   * @return the corresponding service
   */
  public BaseCommunicationService getService(SourceFile source) {
    return getService(source.getSingularName());
  }
}
