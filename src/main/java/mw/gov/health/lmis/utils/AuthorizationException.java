package mw.gov.health.lmis.utils;

public class AuthorizationException extends RuntimeException {

  private static final long serialVersionUID = -3267093470762329450L;

  public AuthorizationException(String message, Throwable cause) {
    super(message, cause);
  }
}
