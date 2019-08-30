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

import lombok.NoArgsConstructor;

import java.util.Properties;

@NoArgsConstructor
public class Configuration extends Properties {

  public String getClientSecret() {
    return getProperty("clientSecret");
  }

  public String getPassword() {
    return getProperty("password");
  }

  public String getLogin() {
    return getProperty("login");
  }

  public String getHost() {
    return getProperty("host");
  }

  public String getClientId() {
    return getProperty("clientId");
  }

  public String getDirectory() {
    return getProperty("directory");
  }

  public String getUpdateAllowed() {
    return getProperty("updateAllowed");
  }

  public String getAutoVerifyEmails() {
    return getProperty("autoVerifyEmails");
  }

  public boolean isUpdateAllowed() {
    return !"false".equalsIgnoreCase(getUpdateAllowed());
  }

  public boolean autoVerifyEmails() {
    return !"false".equalsIgnoreCase(getAutoVerifyEmails());
  }
}
