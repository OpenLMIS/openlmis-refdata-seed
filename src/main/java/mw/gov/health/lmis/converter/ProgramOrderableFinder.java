package mw.gov.health.lmis.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mw.gov.health.lmis.upload.OrderableService;
import mw.gov.health.lmis.upload.ProgramService;

import java.util.Optional;
import java.util.stream.IntStream;

import javax.json.JsonArray;
import javax.json.JsonObject;

@Component
public class ProgramOrderableFinder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProgramOrderableFinder.class);

  private static final String CODE = "code";
  private static final String PRODUCT_CODE = "productCode";
  private static final String ID = "id";
  private static final String PROGRAMS = "programs";
  private static final String PROGRAM_ID = "programId";

  @Autowired
  private ProgramService programService;

  @Autowired
  private OrderableService orderableService;

  Optional<JsonObject> find(String productCode, String programCode) {
    JsonObject program = programService.findBy(CODE, programCode);

    if (null == program) {
      LOGGER.warn("Can't find program with code: {}", programCode);
      return Optional.empty();
    }

    String programId = program.getString(ID);

    JsonObject product = orderableService.findBy(PRODUCT_CODE, productCode);

    if (null == product) {
      LOGGER.warn("Can't find product with code: {}", productCode);
      return Optional.empty();
    }

    JsonArray programOrderables = product.getJsonArray(PROGRAMS);
    Optional<JsonObject> programOrderable = IntStream
        .range(0, programOrderables.size())
        .mapToObj(programOrderables::getJsonObject)
        .filter(json -> programId.equals(json.getString(PROGRAM_ID)))
        .findFirst();

    if (!programOrderable.isPresent()) {
      LOGGER.warn(
          "Can't find program orderable with those values: {}/{}", productCode, programCode
      );
    }

    return programOrderable;
  }

}
