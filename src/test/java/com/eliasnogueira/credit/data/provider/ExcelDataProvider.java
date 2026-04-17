/*
 * MIT License
 *
 * Copyright (c) 2020 Elias Nogueira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.eliasnogueira.credit.data.provider;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ExcelDataProvider {

    private ExcelDataProvider() {
    }

    @DataProvider(name = "excelData")
    public static Object[][] excelData(Method testMethod) {
        ExcelSheet excelSheet = testMethod.getAnnotation(ExcelSheet.class);
        if (excelSheet == null) {
            throw new IllegalArgumentException("Missing @ExcelSheet annotation on method: " + testMethod.getName());
        }

        return readSheet(excelSheet.filePath(), excelSheet.sheetName(), excelSheet.skipHeader());
    }

    public static Object[][] readSheet(String filePath, String sheetName, boolean skipHeader) {
        Path excelPath = Path.of(filePath);
        if (!excelPath.isAbsolute()) {
            excelPath = Path.of(System.getProperty("user.dir")).resolve(excelPath);
        }
        excelPath = excelPath.normalize();

        if (!Files.exists(excelPath)) {
            throw new IllegalArgumentException("Excel file not found: " + excelPath);
        }

        try (InputStream inputStream = Files.newInputStream(excelPath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            return toDataProviderData(sheet, skipHeader);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read Excel file: " + excelPath, exception);
        }
    }

    private static Object[][] toDataProviderData(Sheet sheet, boolean skipHeader) {
        int startRow = skipHeader ? 1 : 0;
        if (sheet.getPhysicalNumberOfRows() <= startRow) {
            return new Object[0][0];
        }

        DataFormatter dataFormatter = new DataFormatter();
        int maxColumns = resolveMaxColumns(sheet);
        List<Object[]> rows = new ArrayList<>();

        for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || isEmptyRow(row, maxColumns, dataFormatter)) {
                continue;
            }

            Object[] rowData = new Object[maxColumns];
            for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                rowData[cellIndex] = dataFormatter.formatCellValue(cell);
            }
            rows.add(rowData);
        }

        return rows.toArray(new Object[0][]);
    }

    private static int resolveMaxColumns(Sheet sheet) {
        int maxColumns = 0;
        for (Row row : sheet) {
            if (row != null) {
                maxColumns = Math.max(maxColumns, row.getLastCellNum());
            }
        }

        if (maxColumns <= 0) {
            throw new IllegalArgumentException("Sheet does not contain any columns");
        }

        return maxColumns;
    }

    private static boolean isEmptyRow(Row row, int maxColumns, DataFormatter formatter) {
        for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
            Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (!formatter.formatCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }
}
