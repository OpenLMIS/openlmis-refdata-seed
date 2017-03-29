package mw.gov.health.lmis;

import com.beust.jcommander.Parameter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Arguments {

  @Parameter(
      names = {"--host", "-h"},
      description = "OpenLMIS instance address"
  )
  private String host;

  @Parameter(
      names = {"--login", "-l"},
      description = "OpenLMIS user login"
  )
  private String login;

  @Parameter(
      names = {"--password", "-p"},
      description = "OpenLMIS user password"
  )
  private String password;

  @Parameter(
      names = {"--dir", "-d"},
      description = "Directory containing ref data files"
  )
  private String directory;

}
