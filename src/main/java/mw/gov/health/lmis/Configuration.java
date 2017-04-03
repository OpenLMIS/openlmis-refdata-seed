package mw.gov.health.lmis;

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
}
