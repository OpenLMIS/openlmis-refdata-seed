package mw.gov.health.lmis.converter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class SkipTypeConverterTest {
  private SkipTypeConverter converter = new SkipTypeConverter();

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports("SKIP"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(converter.supports("TO_ARRAY_BY_NAME"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_PRODUCT_CODE"), is(false));
    assertThat(converter.supports("TO_ID_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ID_BY_NAME"), is(false));
    assertThat(converter.supports("TO_ID_BY_PRODUCT_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_NAME"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_PRODUCT_CODE"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("code", "", "SKIP", "", "");
    String value = "PC187";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.size(), is(0));
  }

}
