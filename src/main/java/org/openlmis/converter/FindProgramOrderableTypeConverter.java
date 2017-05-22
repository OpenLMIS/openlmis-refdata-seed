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

package org.openlmis.converter;

import org.openlmis.upload.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.openlmis.upload.OrderableService;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@Component
public class FindProgramOrderableTypeConverter extends BaseTypeConverter {
  private static final String CODE = "code";
  private static final String PRODUCT_CODE = "productCode";
  private static final String ID = "id";
  private static final String PROGRAMS = "programs";
  private static final String PROGRAM_ID = "programId";

  @Autowired
  private ProgramService programService;

  @Autowired
  private OrderableService orderableService;

  @Override
  public boolean supports(String type) {
    return "FIND_PROGRAM_ORDERABLE".equalsIgnoreCase(type);
  }

  @Override
  public void convert(JsonObjectBuilder builder, Mapping mapping, String value) {
    List<String> array = getArrayValues(value);
    find(array.get(0), array.get(1))
        .ifPresent(json -> builder.add(mapping.getTo(), json));
  }

  private Optional<JsonObject> find(String productCode, String programCode) {
    JsonObject program = programService.findBy(CODE, programCode);

    if (null == program) {
      logger.warn("Can't find program with code: {}", programCode);
      return Optional.empty();
    }

    String programId = program.getString(ID);

    JsonObject product = orderableService.findBy(PRODUCT_CODE, productCode);

    if (null == product) {
      logger.warn("Can't find product with code: {}", productCode);
      return Optional.empty();
    }

    JsonArray programOrderables = product.getJsonArray(PROGRAMS);
    Optional<JsonObject> programOrderable = IntStream
        .range(0, programOrderables.size())
        .mapToObj(programOrderables::getJsonObject)
        .filter(json -> programId.equals(json.getString(PROGRAM_ID)))
        .findFirst();

    if (!programOrderable.isPresent()) {
      logger.warn(
          "Can't find program orderable with those values: {}/{}", productCode, programCode
      );
    }

    return programOrderable;
  }
}
