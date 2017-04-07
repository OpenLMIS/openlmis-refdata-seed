package mw.gov.health.lmis.converter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;

public class MappingConverterTest {

  private MappingConverter converter = new MappingConverter();

  @Test
  public void shouldReadMappingFile() throws Exception {
    File file = ResourceUtils.getFile(getClass().getResource("/test_mapping.csv"));
    String filePath = file.getAbsolutePath();

    List<Mapping> mappings = converter.getMappingForFile(filePath);

    assertThat(mappings, Matchers.hasSize(3));

    Mapping one = mappings.get(0);
    assertThat(one.getFrom(), is(equalTo("code")));
    assertThat(one.getTo(), is(equalTo("code")));
    assertThat(one.getType(), is(equalTo("DIRECT")));
    assertThat(one.getEntityName(), is(nullValue()));
    assertThat(one.getDefaultValue(), is(nullValue()));


    Mapping two = mappings.get(1);
    assertThat(two.getFrom(), is(equalTo("facility")));
    assertThat(two.getTo(), is(equalTo("fac")));
    assertThat(two.getType(), is(equalTo("TO_OBJECT_BY_CODE")));
    assertThat(two.getEntityName(), is(equalTo("Facility")));
    assertThat(two.getDefaultValue(), is(nullValue()));

    Mapping three = mappings.get(2);
    assertThat(three.getFrom(), is(equalTo("displayOrder")));
    assertThat(three.getTo(), is(equalTo("order")));
    assertThat(three.getType(), is(equalTo("DIRECT")));
    assertThat(three.getEntityName(), is(nullValue()));
    assertThat(three.getDefaultValue(), is(equalTo("1")));
  }

  @Test
  public void shouldReturnEmptyListIfFileNotExist() throws Exception {
    assertThat(converter.getMappingForFile("abc"), hasSize(0));
  }
}
