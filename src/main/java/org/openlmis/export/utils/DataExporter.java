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

package org.openlmis.export.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.json.JsonArray;
import org.openlmis.Configuration;
import org.openlmis.converter.Mapping;
import org.openlmis.converter.MappingConverter;
import org.openlmis.export.converter.Deconverter;
import org.openlmis.export.writer.CsvWriter;
import org.openlmis.upload.BaseCommunicationService;
import org.openlmis.upload.Services;
import org.openlmis.utils.AppHelper;
import org.openlmis.utils.SourceFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataExporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataExporter.class);

  @Autowired
  Configuration configuration;

  @Autowired
  private Deconverter deconverter;

  @Autowired
  private CsvWriter writer;

  @Autowired
  private MappingConverter mappingConverter;

  @Autowired
  private Services services;

  @Autowired
  private AppHelper appHelper;

  @Autowired
  private ChildCsvCollector childCsvCollector;

  @Autowired
  private OriginalCodeResolver originalCodeResolver;

  @Autowired
  private SequentialCodeAllocator sequentialCodeAllocator;

  @Autowired
  private MasterDataComparisonReporter comparisonReporter;

  /**
   * Exports master data from a running OLMIS instance into CSV files, one per source entity,
   * written to the configured output directory.
   */
  public void exportData() {
    if (!appHelper.createOutputDirectory(configuration.getOutputDirectory())) {
      LOGGER.info("Could not open or create output directory under {}",
          configuration.getOutputDirectory());
      return;
    }

    Arrays
        .stream(SourceFile.values())
        .forEach(this::exportDataFor);

    flushChildFiles();
    originalCodeResolver.logSummary();
    sequentialCodeAllocator.logSummary();
    comparisonReporter.report();
  }

  /**
   * Writes out the secondary ("child") CSV files accumulated while reversing {@code *_FROM_FILE_*}
   * mappings. These are not {@link SourceFile} entities, so they are only produced here.
   */
  private void flushChildFiles() {
    for (String childFileName : childCsvCollector.fileNames()) {
      File outputFile = new File(configuration.getOutputDirectory(), childFileName);
      LOGGER.info(" == Exporting child file {} == ", childFileName);
      try {
        writer.write(outputFile, childCsvCollector.header(childFileName),
            new ArrayList<>(childCsvCollector.rows(childFileName)));
      } catch (RuntimeException ex) {
        LOGGER.error("Failed to write child file {}; continuing.", childFileName, ex);
      }
    }
  }

  private void exportDataFor(SourceFile source) {
    String outputFileName = source.getFullFileName(configuration.getOutputDirectory());
    String mappingFileName = source.getFullMappingFileName(configuration.getDirectory());

    LOGGER.info(" == Exporting {} == ", source.getName());
    LOGGER.info("Outputting to file: {}", outputFileName);
    LOGGER.info("Outputting mapping file: {}", mappingFileName);

    final File outputFile = new File(outputFileName);
    File mappingFile = new File(mappingFileName);

    List<Mapping> mappings = mappingConverter.getMappingForFile(mappingFile);
    if (mappings.isEmpty()) {
      LOGGER.info("Skipping {}: no mapping file", source.getName());
      return;
    }
    if (!deconverter.supportsAll(mappings)) {
      LOGGER.info("Skipping {}: unsupported types {}", source.getName(),
          deconverter.unsupportedTypes(mappings));
      return;
    }
    List<String> header = deconverter.getHeader(mappings);

    BaseCommunicationService service = services.getService(source);

    try {
      List<Map<String, String>> csvRows = new ArrayList<>();
      JsonArray all = service.findAllForExport();
      for (int i = 0; i < all.size(); i++) {
        csvRows.add(deconverter.deconvert(all.getJsonObject(i), mappings));
      }
      writer.write(outputFile, header, csvRows);
    } catch (RuntimeException ex) {
      LOGGER.error("Failed to export {}; no output file was written for it. Fix the underlying "
              + "data/mapping and re-run the export, then re-upload the corrected file.",
          source.getName(), ex);
    }
  }
}
