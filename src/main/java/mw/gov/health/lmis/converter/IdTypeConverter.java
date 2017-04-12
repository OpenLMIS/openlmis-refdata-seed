package mw.gov.health.lmis.converter;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.upload.BaseCommunicationService;
import mw.gov.health.lmis.upload.Services;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@Component
class IdTypeConverter extends BaseTypeConverter {
  private static final String ID = "id";

  @Autowired
  private Services services;

  @Override
  public boolean supports(String type) {
    return startsWithIgnoreCase(type, "TO_ID_BY");
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    BaseCommunicationService service = services.getService(mapping.getEntityName());
    String by = getBy(mapping.getType());

    JsonObject jsonRepresentation = service.findBy(by, value);

    if (null == jsonRepresentation) {
      logger.warn(
          "The CSV file contained reference to entity {} "
              + "with {} {} but such reference does not exist.",
          mapping.getEntityName(), by, value
      );
    } else {
      builder.add(mapping.getTo(), jsonRepresentation.getString(ID));
    }
  }

}
