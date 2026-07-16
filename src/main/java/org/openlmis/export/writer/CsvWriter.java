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

package org.openlmis.export.writer;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CsvWriter implements Writer {

  // UTF-8 byte order mark (EF BB BF), written first so exports byte-match the seeded CSVs.
  private static final char BOM = '\uFEFF';

  @Override
  public void write(File file, List<String> header, List<Map<String, String>> rows) {
    try (OutputStreamWriter output =
             new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
         CSVWriter writer = new CSVWriter(output)) {
      output.write(BOM);
      output.flush();
      writer.writeNext(header.toArray(new String[0]), false);
      for (Map<String, String> row : rows) {
        writer.writeNext(toLine(row, header), false);
      }
    } catch (IOException ex) {
      // Surface the failure instead of leaving a truncated/empty file that looks like a clean
      // export. The caller (DataExporter) catches this per-file so the remaining files still run.
      throw new IllegalStateException(
          String.format("Failed to write CSV file %s", file.getName()), ex);
    }
  }

  private String[] toLine(Map<String, String> row, List<String> header) {
    return header.stream()
        .map(col -> row.getOrDefault(col, ""))
        .map(v -> v == null ? "" : v)
        .toArray(String[]::new);
  }
}
