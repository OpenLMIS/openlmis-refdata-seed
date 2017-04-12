package mw.gov.health.lmis.converter;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

@Component
public class CreateObjectTypeConverter extends BaseTypeConverter {

  @Override
  public boolean supports(String type) {
    return "TO_OBJECT".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    List<String> entries = Lists.newArrayList(StringUtils.split(value, ','));
    JsonObjectBuilder inner = Json.createObjectBuilder();

    for (String entry : entries) {
      List<String> keyValue = Lists.newArrayList(StringUtils.split(entry, ':'));

      if (keyValue.size() == 2) {
        inner.add(keyValue.get(0), keyValue.get(1));
      } else {
        logger.warn(
            "Invalid map entry representation: {}. Desired format is \"<key>:<value>\".", entry
        );
      }
    }

    builder.add(mapping.getTo(), inner.build());
  }

}
