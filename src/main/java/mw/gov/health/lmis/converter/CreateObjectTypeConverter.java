package mw.gov.health.lmis.converter;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.upload.Services;

import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

@Component
public class CreateObjectTypeConverter extends BaseTypeConverter {

  @Autowired
  private Services services;

  @Override
  public boolean supports(String type) {
    return "TO_OBJECT".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    List<String> entries = Lists.newArrayList(StringUtils.split(value, ','));
    JsonObjectBuilder builder1 = Json.createObjectBuilder();

    for (String entry : entries) {
      List<String> keyValue = Lists.newArrayList(StringUtils.split(entry, ':'));

      if (keyValue.size() == 2) {
        builder1.add(keyValue.get(0), keyValue.get(1));
      } else {
        logger.warn(
            "Invalid map entry representation: {}. Desired format is \"<key>:<value>\".", entry
        );
      }
    }

    builder.add(mapping.getTo(), builder1.build());
  }

}
