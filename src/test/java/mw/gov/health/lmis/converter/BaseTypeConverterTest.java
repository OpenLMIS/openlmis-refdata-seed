package mw.gov.health.lmis.converter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseTypeConverterTest {

  @Spy
  private BaseTypeConverter converter;

  @Test
  public void shouldReturnNullIfThereIsNoBySection() throws Exception {
    assertThat(converter.getBy("ala"), is(nullValue()));
  }

  @Test
  public void shouldReturnCorrectValueForBySection() throws Exception {
    assertThat(converter.getBy("TO_ID_BY_NAME"), is(equalTo("name")));
    assertThat(converter.getBy("TO_ID_BY_PRODUCT_CODE"), is(equalTo("productCode")));
  }

  @Test
  public void shouldReturnEmptyListIfValueIsBlank() throws Exception {
    assertThat(converter.getArrayValues(null), hasSize(0));
    assertThat(converter.getArrayValues(""), hasSize(0));
    assertThat(converter.getArrayValues("    "), hasSize(0));
    assertThat(converter.getArrayValues("[]"), hasSize(0));
    assertThat(converter.getArrayValues(""), hasSize(0));
    assertThat(converter.getArrayValues("[    ]"), hasSize(0));
  }

  @Test
  public void shouldConvertSingleValueToArray() throws Exception {
    assertThat(converter.getArrayValues("ala"), hasItem("ala"));
  }

  @Test
  public void shouldConvertValueToArray() throws Exception {
    assertThat(converter.getArrayValues("[one,two]"), hasItems("one", "two"));
  }
}
