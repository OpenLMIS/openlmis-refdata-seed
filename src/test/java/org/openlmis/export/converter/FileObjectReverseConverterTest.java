/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.export.converter;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ChildCsvCollector;
import org.openlmis.utils.AppHelper;

@RunWith(MockitoJUnitRunner.class)
public class FileObjectReverseConverterTest {

  private static final String EMAIL = "email";
  private static final String USERNAME = "username";
  private static final String EMAIL_FILE = "EmailDetails.csv";
  private static final String EMAIL_TYPE = "TO_OBJECT_FROM_FILE_BY_EMAIL";
  private static final String EMAIL_DETAILS = "emailDetails";
  private static final String EMAIL_VALUE = "a@b.com";

  @Mock
  private Deconverter deconverter;

  @Mock
  private AppHelper appHelper;

  @Mock
  private ChildCsvCollector collector;

  @InjectMocks
  private FileObjectReverseConverter converter;

  @Test
  public void shouldSupportToObjectFromFileByTypesOnly() {
    assertThat(converter.supports(EMAIL_TYPE), is(true));
    assertThat(converter.supports("TO_OBJECT_BY_CODE"), is(false));
    assertThat(converter.supports("TO_OBJECT"), is(false));
  }

  @Test
  public void shouldCollectChildRowAndSetParentJoinValue() {
    final JsonObject source = Json.createObjectBuilder()
        .add(EMAIL_DETAILS, Json.createObjectBuilder().add(EMAIL, EMAIL_VALUE))
        .build();
    List<Mapping> childMappings = asList(new Mapping(EMAIL, EMAIL, "DIRECT", "", ""));
    when(appHelper.readMappings(EMAIL_FILE)).thenReturn(childMappings);
    when(deconverter.getHeader(childMappings)).thenReturn(asList(EMAIL));

    Map<String, String> childRow = new LinkedHashMap<>();
    childRow.put(EMAIL, EMAIL_VALUE);
    when(deconverter.deconvert(any(JsonObject.class), eq(childMappings))).thenReturn(childRow);

    Mapping mapping = new Mapping(EMAIL, EMAIL_DETAILS, EMAIL_TYPE, EMAIL_FILE, "");
    Map<String, String> parentRow = new LinkedHashMap<>();
    converter.deconvert(source, mapping, parentRow);

    verify(collector).add(eq(EMAIL_FILE), eq(asList(EMAIL)), eq(childRow));
    assertThat(parentRow.get(EMAIL), is(EMAIL_VALUE));
  }

  @Test
  public void shouldSkipWhenChildProducesNoJoinValue() {
    // models the Node/OrganizationNode duality: the wrong-side mapping resolves to nothing
    final JsonObject source = Json.createObjectBuilder()
        .add("node", Json.createObjectBuilder().add("id", "x").add("refDataFacility", true))
        .build();
    List<Mapping> childMappings =
        asList(new Mapping("referenceName", "referenceId", "TO_ID_BY_NAME", "Organization", ""));
    when(appHelper.readMappings("OrganizationNodes.csv")).thenReturn(childMappings);
    when(deconverter.getHeader(childMappings)).thenReturn(asList("referenceName"));
    when(deconverter.deconvert(any(JsonObject.class), eq(childMappings)))
        .thenReturn(new LinkedHashMap<String, String>());

    Mapping mapping = new Mapping("organizationName", "node",
        "TO_OBJECT_FROM_FILE_BY_REFERENCE_NAME", "OrganizationNodes.csv", "");
    Map<String, String> parentRow = new LinkedHashMap<>();
    converter.deconvert(source, mapping, parentRow);

    verify(collector, never()).add(anyString(), anyList(), anyMap());
    assertThat(parentRow.isEmpty(), is(true));
  }

  @Test
  public void shouldInheritBlankChildColumnsFromTheParentRow() {
    final JsonObject source = Json.createObjectBuilder()
        .add(EMAIL_DETAILS, Json.createObjectBuilder().add(EMAIL, EMAIL_VALUE))
        .build();
    List<Mapping> childMappings = asList(
        new Mapping(EMAIL, EMAIL, "DIRECT", "", ""),
        new Mapping(USERNAME, USERNAME, "DIRECT", "", ""));
    when(appHelper.readMappings(EMAIL_FILE)).thenReturn(childMappings);
    when(deconverter.getHeader(childMappings)).thenReturn(asList(EMAIL, USERNAME));

    Map<String, String> childRow = new LinkedHashMap<>();
    childRow.put(EMAIL, EMAIL_VALUE);
    when(deconverter.deconvert(any(JsonObject.class), eq(childMappings))).thenReturn(childRow);

    Mapping mapping = new Mapping(EMAIL, EMAIL_DETAILS, EMAIL_TYPE, EMAIL_FILE, "");
    Map<String, String> parentRow = new LinkedHashMap<>();
    parentRow.put(USERNAME, "admin");
    converter.deconvert(source, mapping, parentRow);

    assertThat(childRow.get(USERNAME), is("admin"));
  }
}
