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

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.openlmis.converter.Mapping;
import org.openlmis.export.utils.ChildCsvCollector;
import org.openlmis.utils.AppHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Reverses a facility's embedded {@code supportedPrograms} array
 * ({@code TO_ARRAY_FROM_FILE_BY_PROGRAM_CODE}) into the SupportedPrograms child file. A supported
 * program is identified by its (facility, program) pair, so unlike the generic
 * {@link FileArrayReverseConverter} each child row is stamped with the parent facility's code. That
 * keeps the rows per-facility instead of collapsing identical program/attribute tuples across
 * facilities, so the file mirrors the master-data baseline. Every other column stays
 * mapping-driven, so declaring further columns (e.g. startDate/locallyFulfilled) in the child
 * mapping is enough to have them exported.
 */
@Component
public class SupportedProgramReverseConverter extends BaseReverseTypeConverter {

  private static final String TYPE = "TO_ARRAY_FROM_FILE_BY_PROGRAM_CODE";
  private static final String FACILITY_CODE = "facilityCode";
  private static final String CODE = "code";
  private static final String PROGRAM_CODE = "programCode";

  @Autowired
  @Lazy
  private Deconverter deconverter;

  @Autowired
  private AppHelper appHelper;

  @Autowired
  private ChildCsvCollector collector;

  @Override
  public boolean supports(String type) {
    return equalsIgnoreCase(type, TYPE);
  }

  @Override
  public void deconvert(JsonObject source, Mapping mapping, Map<String, String> row) {
    String to = mapping.getTo();
    if (!source.containsKey(to) || source.isNull(to)) {
      return;
    }

    JsonValue value = source.get(to);
    if (value.getValueType() != JsonValue.ValueType.ARRAY) {
      logger.warn("Field {} is not an array ({}); skipping.", to, value.getValueType());
      return;
    }

    String facilityCode = source.getString(CODE, "");
    String childFileName = mapping.getEntityName();
    List<Mapping> childMappings = appHelper.readMappings(childFileName);

    List<String> childHeader = new ArrayList<>();
    childHeader.add(FACILITY_CODE);
    childHeader.addAll(deconverter.getHeader(childMappings));

    List<String> programCodes = new ArrayList<>();
    for (JsonValue element : source.getJsonArray(to)) {
      if (element.getValueType() != JsonValue.ValueType.OBJECT) {
        logger.warn("Element of {} is not an object ({}); skipping element.",
            to, element.getValueType());
        continue;
      }

      Map<String, String> childRow = deconverter.deconvert((JsonObject) element, childMappings);
      childRow.put(FACILITY_CODE, facilityCode);
      collector.add(childFileName, childHeader, childRow);

      String programCode = childRow.get(PROGRAM_CODE);
      if (programCode != null) {
        programCodes.add(programCode);
      }
    }

    if (!programCodes.isEmpty()) {
      row.put(mapping.getFrom(), "[" + String.join(",", programCodes) + "]");
    }
  }
}
