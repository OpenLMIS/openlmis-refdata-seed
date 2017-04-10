package mw.gov.health.lmis.converter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;

import com.beust.jcommander.internal.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.ResourceUtils;

import mw.gov.health.lmis.reader.GenericReader;
import mw.gov.health.lmis.reader.Reader;
import mw.gov.health.lmis.upload.Services;

import java.io.File;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

@RunWith(MockitoJUnitRunner.class)
public class ConverterTest {
  private static final String ARRAY = "array";
  private static final String OBJECT = "object";
  private static final String PROGRAM = "program";

  @Mock
  private Services services;

  @Spy
  private Reader reader = new GenericReader();

  @Spy
  private MappingConverter mappingConverter = new MappingConverter();

  @InjectMocks
  private Converter converter;

  @Test
  public void shouldHandleToObjectMappingWithSeveralEntries() throws Exception {
    Map<String, String> input = ImmutableMap.of(OBJECT, "key1:value1,key2:value2,key3:value3");
    List<Mapping> mappings = Lists.newArrayList(
        new Mapping(OBJECT, OBJECT, "TO_OBJECT", "", "")
    );

    String json = converter.convert(input, mappings);

    try(JsonReader jsonReader = Json.createReader(new StringReader(json))) {
      JsonObject object = jsonReader.readObject().getJsonObject(OBJECT);

      assertThat(object.size(), is(equalTo(3)));
      assertThat(object.getJsonString("key1").getString(), is(equalTo("value1")));
      assertThat(object.getJsonString("key2").getString(), is(equalTo("value2")));
      assertThat(object.getJsonString("key3").getString(), is(equalTo("value3")));
    }
  }

  @Test
  public void shouldHandleArrayFromFileByCodeType() throws Exception {
    File file = ResourceUtils.getFile(getClass().getResource("/inner.csv"));

    Map<String, String> input = ImmutableMap.of(ARRAY, "[CODE1,CODE3,CODE5]");
    List<Mapping> mappings = Lists.newArrayList(
        new Mapping(ARRAY, ARRAY, "TO_ARRAY_FROM_FILE_BY_CODE", file.getAbsolutePath(), "")
    );

    String json = converter.convert(input, mappings);

    try(JsonReader jsonReader = Json.createReader(new StringReader(json))) {
      JsonObject object = jsonReader.readObject();

      JsonArray array = object.getJsonArray(ARRAY);
      assertThat(array, hasSize(3));

      JsonObject one = array.getJsonObject(0);
      assertThat(one.getString(PROGRAM), is(equalTo("em")));

      JsonObject two = array.getJsonObject(1);
      assertThat(two.getString(PROGRAM), is(equalTo("malaria")));

      JsonObject three = array.getJsonObject(2);
      assertThat(three.getString(PROGRAM), is(equalTo("tb")));
    }
  }
}
