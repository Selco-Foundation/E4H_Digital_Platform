package com.translator.service;

import com.translator.dto.TranslationItemDto;
import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.exception.ExcelProcessingException;
import com.translator.provider.TranslationProviderType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Fills in empty cells across every destination-language column on an
 * uploaded spreadsheet. The first column is always the source words,
 * regardless of its header text. Every other column with a non-blank
 * header is treated as a destination-language column named after that
 * language (e.g. a column headed "Hindi" holds Hindi translations);
 * already-filled cells are left untouched. If {@code destinationLanguage}
 * is supplied and no column for it exists yet, one is appended and filled.
 * Reads/writes both Excel (.xlsx/.xls) and CSV through a common row-grid
 * so the enrichment logic is format-agnostic. Actual translation is
 * delegated to {@link TranslationService}, keeping provider selection
 * provider-agnostic.
 */
@Service
public class ExcelTranslationService {

    private static final Logger log = LoggerFactory.getLogger(ExcelTranslationService.class);
    private static final int SOURCE_COLUMN_INDEX = 0;

    private final TranslationService translationService;

    public ExcelTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public TranslatedSpreadsheet translateWorkbook(
            MultipartFile file,
            String sourceLanguage,
            String destinationLanguage,
            TranslationProviderType provider) {

        if (file == null || file.isEmpty()) {
            throw new ExcelProcessingException("Uploaded file is empty");
        }

        SpreadsheetFormat format = SpreadsheetFormat.fromFilename(file.getOriginalFilename());
        List<List<String>> grid = readGrid(file, format);

        if (grid.isEmpty()) {
            throw new ExcelProcessingException("The uploaded sheet has no header row");
        }

        List<String> headerRow = grid.get(0);

        if (destinationLanguage != null && !destinationLanguage.isBlank()) {
            ensureDestinationColumnExists(headerRow, destinationLanguage);
        }

        for (int columnIndex : findDestinationColumnIndices(headerRow)) {
            String columnLanguage = headerRow.get(columnIndex).trim();
            fillDestinationColumn(grid, columnIndex, sourceLanguage, columnLanguage, provider);
        }

        byte[] content = writeGrid(grid, format);
        String downloadFilename = buildDownloadFilename(file.getOriginalFilename(), format);

        return new TranslatedSpreadsheet(content, downloadFilename, format.contentType());
    }

    private void ensureDestinationColumnExists(List<String> headerRow, String destinationLanguage) {
        String capitalized = LanguageNames.capitalize(destinationLanguage);

        for (int columnIndex = 0; columnIndex < headerRow.size(); columnIndex++) {
            boolean isMatch = columnIndex != SOURCE_COLUMN_INDEX
                    && capitalized.equalsIgnoreCase(headerRow.get(columnIndex).trim());
            if (isMatch) {
                return;
            }
        }

        setCell(headerRow, headerRow.size(), capitalized);
    }

    private List<Integer> findDestinationColumnIndices(List<String> headerRow) {
        List<Integer> columnIndices = new ArrayList<>();

        for (int columnIndex = 0; columnIndex < headerRow.size(); columnIndex++) {
            boolean isDestinationColumn = columnIndex != SOURCE_COLUMN_INDEX
                    && !headerRow.get(columnIndex).trim().isEmpty();
            if (isDestinationColumn) {
                columnIndices.add(columnIndex);
            }
        }

        return columnIndices;
    }

    private void fillDestinationColumn(
            List<List<String>> grid,
            int columnIndex,
            String sourceLanguage,
            String destinationLanguage,
            TranslationProviderType provider) {

        List<Integer> rowIndices = new ArrayList<>();
        List<String> words = new ArrayList<>();

        for (int rowIndex = 1; rowIndex < grid.size(); rowIndex++) {
            List<String> row = grid.get(rowIndex);
            String sourceWord = cellAt(row, SOURCE_COLUMN_INDEX).trim();
            String existingValue = cellAt(row, columnIndex).trim();

            if (!sourceWord.isEmpty() && existingValue.isEmpty()) {
                rowIndices.add(rowIndex);
                words.add(sourceWord);
            }
        }

        if (words.isEmpty()) {
            return;
        }

        log.info(
                "Translating {} word(s) for column '{}': '{}' -> '{}'",
                words.size(), destinationLanguage, sourceLanguage, destinationLanguage
        );

        TranslationRequest request = new TranslationRequest(sourceLanguage, destinationLanguage, provider, words);
        TranslationResponse response = translationService.translate(request);

        writeTranslations(grid, columnIndex, rowIndices, response.translations());
    }

    private void writeTranslations(
            List<List<String>> grid,
            int destinationColumnIndex,
            List<Integer> rowIndices,
            List<TranslationItemDto> translations) {

        if (translations.size() != rowIndices.size()) {
            throw new ExcelProcessingException(
                    "Translation result count (%d) did not match the number of words to translate (%d)"
                            .formatted(translations.size(), rowIndices.size()));
        }

        for (int i = 0; i < rowIndices.size(); i++) {
            setCell(grid.get(rowIndices.get(i)), destinationColumnIndex, translations.get(i).translated());
        }
    }

    private List<List<String>> readGrid(MultipartFile file, SpreadsheetFormat format) {
        try {
            return switch (format) {
                case CSV -> readCsvGrid(file);
                case EXCEL -> readExcelGrid(file);
            };
        } catch (IOException e) {
            throw new ExcelProcessingException("Failed to read the uploaded file", e);
        }
    }

    private List<List<String>> readCsvGrid(MultipartFile file) throws IOException {
        List<List<String>> grid = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {

            for (CSVRecord record : parser) {
                List<String> row = new ArrayList<>();
                record.forEach(row::add);
                grid.add(row);
            }
        }

        return grid;
    }

    private List<List<String>> readExcelGrid(MultipartFile file) throws IOException {
        List<List<String>> grid = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                List<String> rowValues = new ArrayList<>();

                if (row != null) {
                    for (int columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
                        rowValues.add(cellText(row.getCell(columnIndex)));
                    }
                }

                grid.add(rowValues);
            }
        }

        return grid;
    }

    private String cellText(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private String cellAt(List<String> row, int columnIndex) {
        return columnIndex < row.size() ? row.get(columnIndex) : "";
    }

    private void setCell(List<String> row, int columnIndex, String value) {
        while (row.size() <= columnIndex) {
            row.add("");
        }
        row.set(columnIndex, value);
    }

    private byte[] writeGrid(List<List<String>> grid, SpreadsheetFormat format) {
        return switch (format) {
            case CSV -> writeCsvGrid(grid);
            case EXCEL -> writeExcelGrid(grid);
        };
    }

    private byte[] writeCsvGrid(List<List<String>> grid) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            for (List<String> row : grid) {
                printer.printRecord(row);
            }

            printer.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ExcelProcessingException("Failed to write the translated CSV file", e);
        }
    }

    private byte[] writeExcelGrid(List<List<String>> grid) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            for (int rowIndex = 0; rowIndex < grid.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex);
                List<String> rowValues = grid.get(rowIndex);

                for (int columnIndex = 0; columnIndex < rowValues.size(); columnIndex++) {
                    row.createCell(columnIndex).setCellValue(rowValues.get(columnIndex));
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ExcelProcessingException("Failed to write the translated Excel file", e);
        }
    }

    private String buildDownloadFilename(String originalFilename, SpreadsheetFormat format) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "translated" + format.extension();
        }

        int dotIndex = originalFilename.lastIndexOf('.');

        if (dotIndex <= 0) {
            return originalFilename + "-translated" + format.extension();
        }

        return originalFilename.substring(0, dotIndex) + "-translated" + format.extension();
    }

    private enum SpreadsheetFormat {
        CSV(".csv", "text/csv"),
        EXCEL(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        private final String extension;
        private final String contentType;

        SpreadsheetFormat(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        String extension() {
            return extension;
        }

        String contentType() {
            return contentType;
        }

        static SpreadsheetFormat fromFilename(String filename) {
            String lower = filename == null ? "" : filename.toLowerCase();

            if (lower.endsWith(".csv")) {
                return CSV;
            }
            if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
                return EXCEL;
            }

            throw new ExcelProcessingException("Unsupported file type. Please upload a .xlsx, .xls, or .csv file");
        }
    }
}
