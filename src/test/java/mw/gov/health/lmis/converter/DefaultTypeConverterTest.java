package mw.gov.health.lmis.converter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class DefaultTypeConverterTest {
  private DefaultTypeConverter converter = new DefaultTypeConverter();

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports("USE_DEFAULT"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(converter.supports("TO_OBJECT"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("code", "code", "USE_DEFAULT", "", "11235813");

    converter.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.getString(mapping.getTo()), is(equalTo(mapping.getDefaultValue())));
  }

  @Test
  public void shouldSkipNullValue() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("code", "code", "USE_DEFAULT", "", null);

    converter.convert(builder, mapping, null);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }

}
