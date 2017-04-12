package mw.gov.health.lmis.converter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableMap;

import com.beust.jcommander.internal.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import mw.gov.health.lmis.Configuration;
import mw.gov.health.lmis.reader.Reader;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@RunWith(MockitoJUnitRunner.class)
public class FileArrayTypeConverterTest {

  @Mock
  private Configuration configuration;

  @Mock
  private Reader reader;

  @Mock
  private MappingConverter mappingConverter;

  @Mock
  private Converter converter;

  @InjectMocks
  private FileArrayTypeConverter typeConverter;

  @Before
  public void setUp() throws Exception {
    List<Map<String, String>> innerData = Lists.newArrayList(
        ImmutableMap.of("code", "one"),
        ImmutableMap.of("code", "two"),
        ImmutableMap.of("code", "three"),
        ImmutableMap.of("code", "four"),
        ImmutableMap.of("code", "five")
    );

    List<Mapping> innerMapping = Lists.newArrayList(
        new Mapping("code", "code", "DIRECT", "", "")
    );

    doReturn("").when(configuration).getDirectory();
    doReturn(innerData).when(reader).readFromFile(anyString());
    doReturn(innerMapping).when(mappingConverter).getMappingForFile(anyString());
    doAnswer(invocation -> {
      JsonObjectBuilder inner = Json.createObjectBuilder();
      Mapping mapping = innerMapping.get(0);
      inner.add(
          mapping.getTo(),
          invocation.getArgumentAt(0, Map.class).get(mapping.getFrom()).toString()
      );

      return inner.build().toString();
    }).when(converter).convert(anyMap(), anyList());
  }

  @Test
  public void shouldSupportTypes() throws Exception {
    assertThat(typeConverter.supports("TO_ARRAY_FROM_FILE_BY"), is(true));
    assertThat(typeConverter.supports("TO_ARRAY_FROM_FILE_BY_NAME"), is(true));
    assertThat(typeConverter.supports("TO_ARRAY_FROM_FILE_BY_CODE"), is(true));
    assertThat(typeConverter.supports("TO_ARRAY_FROM_FILE_BY_PRODUCT_CODE"), is(true));
  }

  @Test
  public void shouldNotSupportOtherTypes() throws Exception {
    assertThat(typeConverter.supports("TO_OBJECT"), is(false));
    assertThat(typeConverter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(typeConverter.supports("TO_ARRAY_BY_NAME"), is(false));
  }

  @Test
  public void shouldConvert() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("code", "code", "TO_ARRAY_FROM_FILE_BY_CODE", "inner.csv", "");
    String value = "[one,five]";

    typeConverter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));

    JsonArray array = object.getJsonArray(mapping.getTo());

    assertThat(array, hasSize(2));
    assertThat(array.getJsonObject(0).getString("code"), is(equalTo("one")));
    assertThat(array.getJsonObject(1).getString("code"), is(equalTo("five")));
  }

  @Test
  public void shouldCreateEmptyListIfThereIsNoMatchingItems() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Mapping mapping = new Mapping("code", "code", "TO_ARRAY_FROM_FILE_BY_CODE", "inner.csv", "");
    String value = "[six]";

    typeConverter.convert(builder, mapping, value);

    JsonObject object = builder.build();
    assertThat(object.containsKey(mapping.getTo()), is(true));

    JsonArray array = object.getJsonArray(mapping.getTo());
    assertThat(array, hasSize(0));
  }

}
