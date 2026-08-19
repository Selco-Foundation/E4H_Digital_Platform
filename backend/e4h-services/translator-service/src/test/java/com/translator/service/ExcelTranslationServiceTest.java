package com.translator.service;

import com.translator.dto.TranslationItemDto;
import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.exception.ExcelProcessingException;
import com.translator.provider.TranslationProviderType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelTranslationServiceTest {

    @Mock
    private TranslationService translationService;

    private ExcelTranslationService excelTranslationService;

    @BeforeEach
    void setUp() {
        excelTranslationService = new ExcelTranslationService(translationService);
    }

    @Test
    @DisplayName("should create and fill a new destination column, named after the actual language, for an xlsx upload")
    void shouldTranslateXlsxAndNameDestinationColumnAfterLanguage() throws IOException {
        MockMultipartFile file = buildXlsxFile("English", List.of("Potato", "Tomato", "Water"));

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
                new TranslationResponse("english", "hindi", List.of(
                        new TranslationItemDto("Potato", "आलू"),
                        new TranslationItemDto("Tomato", "टमाटर"),
                        new TranslationItemDto("Water", "पानी")
                )));

        TranslatedSpreadsheet result = excelTranslationService.translateWorkbook(file, "english", "hindi", TranslationProviderType.GEMINI);

        assertThat(result.filename()).isEqualTo("words-translated.xlsx");
        assertThat(result.contentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(cellValue(sheet, 0, 1)).isEqualTo("Hindi");
            assertThat(cellValue(sheet, 1, 1)).isEqualTo("आलू");
            assertThat(cellValue(sheet, 2, 1)).isEqualTo("टमाटर");
            assertThat(cellValue(sheet, 3, 1)).isEqualTo("पानी");
        }
    }

    @Test
    @DisplayName("should translate a csv upload and return a csv with the destination column appended")
    void shouldTranslateCsvUpload() throws IOException {
        MockMultipartFile file = buildCsvFile("English", List.of("Potato", "Tomato"));

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
                new TranslationResponse("english", "hindi", List.of(
                        new TranslationItemDto("Potato", "आलू"),
                        new TranslationItemDto("Tomato", "टमाटर")
                )));

        TranslatedSpreadsheet result = excelTranslationService.translateWorkbook(file, "english", "hindi", null);

        assertThat(result.filename()).isEqualTo("words-translated.csv");
        assertThat(result.contentType()).isEqualTo("text/csv");

        List<CSVRecord> records = parseCsv(result.content());
        assertThat(records.get(0).get(0)).isEqualTo("English");
        assertThat(records.get(0).get(1)).isEqualTo("Hindi");
        assertThat(records.get(1).get(1)).isEqualTo("आलू");
        assertThat(records.get(2).get(1)).isEqualTo("टमाटर");
    }

    @Test
    @DisplayName("should fill only empty cells across multiple destination-language columns, leaving existing translations untouched")
    void shouldFillOnlyEmptyCellsAcrossMultipleDestinationColumns() throws IOException {
        MockMultipartFile file = buildXlsxFileWithColumns(
                List.of("English", "Hindi", "French"),
                List.of(
                        List.of("Potato", "", "Pomme de terre"),
                        List.of("Tomato", "टमाटर", "")
                )
        );

        when(translationService.translate(argThat(req -> req != null && "Hindi".equals(req.destinationLanguage()))))
                .thenReturn(new TranslationResponse("english", "Hindi", List.of(new TranslationItemDto("Potato", "आलू"))));
        when(translationService.translate(argThat(req -> req != null && "French".equals(req.destinationLanguage()))))
                .thenReturn(new TranslationResponse("english", "French", List.of(new TranslationItemDto("Tomato", "Tomate"))));

        TranslatedSpreadsheet result = excelTranslationService.translateWorkbook(file, "english", null, null);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(cellValue(sheet, 1, 1)).isEqualTo("आलू");
            assertThat(cellValue(sheet, 1, 2)).isEqualTo("Pomme de terre");
            assertThat(cellValue(sheet, 2, 1)).isEqualTo("टमाटर");
            assertThat(cellValue(sheet, 2, 2)).isEqualTo("Tomate");
        }
    }

    @Test
    @DisplayName("should reuse an existing column already named after the destination language")
    void shouldReuseExistingDestinationColumn() throws IOException {
        MockMultipartFile file = buildXlsxFileWithColumns(
                List.of("English", "Hindi"),
                List.of(List.of("Potato", ""), List.of("Tomato", ""))
        );

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
                new TranslationResponse("english", "hindi", List.of(
                        new TranslationItemDto("Potato", "आलू"),
                        new TranslationItemDto("Tomato", "टमाटर")
                )));

        TranslatedSpreadsheet result = excelTranslationService.translateWorkbook(file, "english", "hindi", null);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat((int) sheet.getRow(0).getLastCellNum()).isEqualTo(2);
            assertThat(cellValue(sheet, 1, 1)).isEqualTo("आलू");
            assertThat(cellValue(sheet, 2, 1)).isEqualTo("टमाटर");
        }
    }

    @Test
    @DisplayName("should treat the first column as source words regardless of its header text")
    void shouldUseFirstColumnRegardlessOfHeaderName() {
        MockMultipartFile file = buildXlsxFile("Some Random Header", List.of("Potato"));

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
                new TranslationResponse("english", "hindi", List.of(new TranslationItemDto("Potato", "आलू"))));

        TranslatedSpreadsheet result = excelTranslationService.translateWorkbook(file, "english", "hindi", null);

        assertThat(result.content()).isNotEmpty();
    }

    @Test
    @DisplayName("should skip columns with a blank header rather than treating them as a destination language")
    void shouldSkipColumnsWithBlankHeader() throws IOException {
        MockMultipartFile file = buildXlsxFileWithColumns(
                List.of("English", "", "Hindi"),
                List.of(List.of("Potato", "stray-value", ""))
        );

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
                new TranslationResponse("english", "hindi", List.of(new TranslationItemDto("Potato", "आलू"))));

        TranslatedSpreadsheet result = excelTranslationService.translateWorkbook(file, "english", null, null);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(cellValue(sheet, 1, 1)).isEqualTo("stray-value");
            assertThat(cellValue(sheet, 1, 2)).isEqualTo("आलू");
        }

        verify(translationService, times(1)).translate(any(TranslationRequest.class));
    }

    @Test
    @DisplayName("should return the file unchanged when there are no destination columns and no destinationLanguage is given")
    void shouldNoOpWhenNoDestinationColumnsAndNoDestinationLanguage() throws IOException {
        MockMultipartFile file = buildXlsxFile("English", List.of("Potato", "Tomato"));

        TranslatedSpreadsheet result = excelTranslationService.translateWorkbook(file, "english", null, null);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat((int) sheet.getRow(0).getLastCellNum()).isEqualTo(1);
            assertThat(cellValue(sheet, 1, 0)).isEqualTo("Potato");
            assertThat(cellValue(sheet, 2, 0)).isEqualTo("Tomato");
        }

        verifyNoInteractions(translationService);
    }

    @Test
    @DisplayName("should succeed with a no-op when the sheet has a header but no data rows")
    void shouldNoOpWhenNoDataRows() throws IOException {
        MockMultipartFile file = buildXlsxFile("English", List.of());

        TranslatedSpreadsheet result = excelTranslationService.translateWorkbook(file, "english", "hindi", null);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result.content()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(cellValue(sheet, 0, 1)).isEqualTo("Hindi");
            assertThat(sheet.getLastRowNum()).isZero();
        }

        verifyNoInteractions(translationService);
    }

    @Test
    @DisplayName("should throw when the uploaded file is empty")
    void shouldThrowWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "sheet.xlsx", "application/octet-stream", new byte[0]);

        assertThatThrownBy(() -> excelTranslationService.translateWorkbook(emptyFile, "english", "hindi", null))
                .isInstanceOf(ExcelProcessingException.class)
                .hasMessageContaining("empty");
    }

    @Test
    @DisplayName("should throw when the uploaded file has an unsupported extension")
    void shouldThrowWhenFileTypeUnsupported() {
        MockMultipartFile file = new MockMultipartFile("file", "sheet.txt", "text/plain", "irrelevant".getBytes());

        assertThatThrownBy(() -> excelTranslationService.translateWorkbook(file, "english", "hindi", null))
                .isInstanceOf(ExcelProcessingException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    @DisplayName("should throw when translation results don't match the word count")
    void shouldThrowWhenTranslationCountMismatches() {
        MockMultipartFile file = buildXlsxFile("English", List.of("Potato", "Tomato"));

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
                new TranslationResponse("english", "hindi", List.of(new TranslationItemDto("Potato", "आलू"))));

        assertThatThrownBy(() -> excelTranslationService.translateWorkbook(file, "english", "hindi", null))
                .isInstanceOf(ExcelProcessingException.class)
                .hasMessageContaining("did not match");
    }

    private MockMultipartFile buildXlsxFile(String sourceHeader, List<String> words) {
        return buildXlsxFileWithColumns(
                List.of(sourceHeader),
                words.stream().map(List::of).toList()
        );
    }

    private MockMultipartFile buildXlsxFileWithColumns(List<String> headers, List<List<String>> dataRows) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
            }

            for (int rowIndex = 0; rowIndex < dataRows.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                List<String> values = dataRows.get(rowIndex);

                for (int columnIndex = 0; columnIndex < values.size(); columnIndex++) {
                    row.createCell(columnIndex).setCellValue(values.get(columnIndex));
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return new MockMultipartFile(
                    "file", "words.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MockMultipartFile buildCsvFile(String sourceHeader, List<String> words) {
        StringBuilder csv = new StringBuilder(sourceHeader).append("\n");
        words.forEach(word -> csv.append(word).append("\n"));

        return new MockMultipartFile("file", "words.csv", "text/csv", csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private List<CSVRecord> parseCsv(byte[] content) throws IOException {
        try (var reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            return parser.getRecords();
        }
    }

    private String cellValue(Sheet sheet, int rowIndex, int columnIndex) {
        return sheet.getRow(rowIndex).getCell(columnIndex).getStringCellValue();
    }
}
