package mw.gov.health.lmis.converter;

import static org.hamcrest.Matchers.hasSize;
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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@RunWith(MockitoJUnitRunner.class)
public class StandardArrayTypeConverterTest {

  @Mock
  private Services services;

  @Mock
  private BaseCommunicationService service;

  @Mock
  private JsonObject mockJson1;

  @Mock
  private JsonObject mockJson2;

  @Mock
  private JsonObject mockJson3;

  @InjectMocks
  private StandardArrayTypeConverter converter;

  @Before
  public void setUp() throws Exception {
    doReturn(mockJson1).when(service).findBy("code", "CODE01");
    doReturn(mockJson2).when(service).findBy("code", "CODE02");
    doReturn(mockJson3).when(service).findBy("code", "CODE03");

    doReturn(service).when(services).getService("EntityName");
  }

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(converter.supports("TO_ARRAY_BY"), is(true));
    assertThat(converter.supports("TO_ARRAY_BY_CODE"), is(true));
    assertThat(converter.supports("TO_ARRAY_BY_NAME"), is(true));
    assertThat(converter.supports("TO_ARRAY_BY_PRODUCT_CODE"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
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
    Mapping mapping = new Mapping("code", "code", "TO_ARRAY_BY_CODE", "EntityName", "");
    String value = "[CODE01,CODE03]";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));

    JsonArray array = object.getJsonArray(mapping.getTo());
    assertThat(array, hasSize(2));
    assertThat(array.getJsonObject(0), is(mockJson1));
    assertThat(array.getJsonObject(1), is(mockJson3));
  }

  @Test
  public void shouldHandleNullObject() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("code", "code", "TO_ARRAY_BY_CODE", "EntityName", "");
    String value = "[CODE02,CODE03,CODE05]";

    converter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));

    JsonArray array = object.getJsonArray(mapping.getTo());

    assertThat(array, hasSize(2));
    assertThat(array.getJsonObject(0), is(mockJson2));
    assertThat(array.getJsonObject(1), is(mockJson3));
  }

}
