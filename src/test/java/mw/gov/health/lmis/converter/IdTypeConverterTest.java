package mw.gov.health.lmis.converter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import mw.gov.health.lmis.upload.BaseCommunicationService;
import mw.gov.health.lmis.upload.Services;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@RunWith(MockitoJUnitRunner.class)
public class IdTypeConverterTest {

  @Mock
  private Services services;

  @Mock
  private BaseCommunicationService service;

  @Mock
  private JsonObject mockJson;

  @InjectMocks
  private IdTypeConverter converter;

  @Before
  public void setUp() throws Exception {
    doReturn(UUID.randomUUID().toString()).when(mockJson).getString("id");

    doReturn(mockJson).when(service).findBy("productCode", "PC187");
    doReturn(service).when(services).getService("Entity");
  }

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports("TO_ID_BY"), is(true));
    assertThat(converter.supports("TO_ID_BY_NAME"), is(true));
    assertThat(converter.supports("TO_ID_BY_CODE"), is(true));
    assertThat(converter.supports("TO_ID_BY_PRODUCT_CODE"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_CODE"), is(false));
    assertThat(converter.supports("TO_ARRAY_FROM_FILE_BY_PRODUCT_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT"), is(false));
    assertThat(converter.supports("TO_ARRAY_BY_NAME"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_NAME"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT_BY_PRODUCT_CODE"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("productCode", "product", "TO_ID_BY_PRODUCT_CODE", "Entity", "");
    String value = "PC187";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.getString(mapping.getTo()), is(mockJson.getString("id")));
  }

  @Test
  public void shouldHandleNullObject() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("productCode", "product", "TO_ID_BY_PRODUCT_CODE", "Entity", "");
    String value = "PC987";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(false));
  }
}
