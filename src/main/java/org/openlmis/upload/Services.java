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

package org.openlmis.upload;

import org.openlmis.utils.SourceFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

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
