package dk.magenta.datafordeler.ger.parser;

import dk.magenta.datafordeler.ger.data.RawData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jopendocument.dom.ODValueType;
import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by lars on 26-11-15.
 */
public class OdfConverter extends SpreadsheetConverter {

    private static final Logger log = LogManager.getLogger(SpreadsheetConverter.class.getCanonicalName());

    protected String[] getApplicableContentTypes() {
        return new String[]{
                "application/vnd.oasis.opendocument.spreadsheet"
        };
    }

    protected String[] getApplicableFileExtensions() {
        return new String[]{
                "ods"
        };
    }

    public Map<String, List<RawData>> convert(File data) throws Exception {
        return this.convert(SpreadSheet.createFromFile(data));
    }

    private Map<String, List<RawData>> convert(SpreadSheet document) throws Exception {
        HashMap<String, List<RawData>> documentConversion = new HashMap<>();
        int sheetCount = document.getSheetCount();
        for (int i = 0; i < sheetCount; i++) {
            ArrayList<RawData> sheetConversion = new ArrayList<>();
            Sheet sheet = document.getSheet(i);
            String sheetName = sheet.getName();
            int firstRowIndex = 0;
            int rowCount = sheet.getRowCount();

            ArrayList<String> columnNames = new ArrayList<>();

            for (Object value : getRow(sheet, firstRowIndex)) {
                columnNames.add((String) value);
            }

            int columnCount = sheet.getColumnCount();

            int emptyRows = 0;
            for (int rowIndex = firstRowIndex + 1; rowIndex < rowCount; rowIndex++) {
                RawData rowConversion = new RawData();
                boolean emptyRow = true;
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    Cell cell;
                    cell = sheet.getImmutableCellAt(columnIndex, rowIndex);
                    Object value = getCellValue(cell);
                    rowConversion.put(columnNames.get(columnIndex), value);
                }
                if (emptyRow) {
                    emptyRows++;
                    if (emptyRows >= 100) {
                        break;
                    }
                } else {
                    sheetConversion.add(rowConversion);
                }
            }
            documentConversion.put(sheetName, sheetConversion);
        }
        return documentConversion;
    }

    private List<Object> getRow(Sheet sheet, int rowIndex) {
        int columnCount = sheet.getColumnCount();
        List<Object> row = new ArrayList<>();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            Cell cell = sheet.getImmutableCellAt(columnIndex, rowIndex);
            row.add(getCellValue(cell));
        }
        return row;
    }

    protected static Object getCellValue(Cell cell) {
        if (cell != null) {
            ODValueType cellType = cell.getValueType();
            switch (cellType) {
                case STRING:
                    try {
                        return Integer.parseInt(cell.getTextValue());
                    } catch (NumberFormatException e) {
                        return cell.getTextValue();
                    }
                case FLOAT:
                    return cell.getValue();
                case DATE:
                    Date date = (Date) cell.getValue();
                    return LocalDate.from(date.toInstant());
                case BOOLEAN:
                    return cell.getValue();
            }
        }
        return null;
    }

}
