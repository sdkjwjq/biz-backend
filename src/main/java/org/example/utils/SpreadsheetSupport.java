package org.example.utils;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/** Excel generation must also work on servers without an installed font system. */
public final class SpreadsheetSupport {
    private SpreadsheetSupport() {}

    static { configure(); }

    public static void configure() {
        // POI 5.2.5 otherwise measures a font even when creating a fixed-width
        // streaming sheet. This flag uses its supported default-width fallback.
        System.setProperty("org.apache.poi.ss.ignoreMissingFontSystem", "true");
    }

    public static SXSSFWorkbook streamingWorkbook(int rowWindow) {
        return new SXSSFWorkbook(rowWindow);
    }

    public static void setColumnWidths(Sheet sheet, int... characterWidths) {
        for (int column = 0; column < characterWidths.length; column++) {
            sheet.setColumnWidth(column, characterWidths[column] * 256);
        }
    }
}
