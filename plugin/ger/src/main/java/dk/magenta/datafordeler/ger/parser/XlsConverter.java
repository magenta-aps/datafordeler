package dk.magenta.datafordeler.ger.parser;

import dk.magenta.datafordeler.ger.data.RawData;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 26-11-15.
 */
public class XlsConverter extends SpreadsheetConverter {

    protected String[] getApplicableContentTypes() {
        return new String[]{
                "application/vnd.ms-excel",
        };
    }

    protected String[] getApplicableFileExtensions() {
        return new String[]{
                "xls"
        };
    }

    public Map<String, List<RawData>> convert(InputStream data) throws Exception {
        return this.convert(new HSSFWorkbook(data));
    }

    public Map<String, List<RawData>> convert(File data) throws Exception {
        return this.convert((HSSFWorkbook) WorkbookFactory.create(data));
    }

    private Map<String, List<RawData>> convert(HSSFWorkbook document) throws Exception {
        HashMap<String, List<RawData>> documentConversion = new HashMap<>();
        int sheetCount = document.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            ArrayList<RawData> sheetConversion = new ArrayList<>();
            HSSFSheet sheet = document.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            int firstRowIndex = sheet.getFirstRowNum();
            int rowCount = sheet.getLastRowNum() + 1;
            ArrayList<String> columnNames = new ArrayList<>();
            for (Object value : getRow(sheet.getRow(firstRowIndex))) {
                columnNames.add((String) value);
            }
            for (int rowIndex = firstRowIndex + 1; rowIndex < rowCount; rowIndex++) {
                RawData rowConversion = new RawData();
                HSSFRow row = sheet.getRow(rowIndex);
                if (row != null) {
                    List<Object> values = getRow(row);
                    for (int cellIndex = 0; cellIndex < values.size(); cellIndex++) {
                        rowConversion.put(columnNames.get(cellIndex), values.get(cellIndex));
                    }
                }
                sheetConversion.add(rowConversion);
            }
            documentConversion.put(sheetName, sheetConversion);
        }
        return documentConversion;
    }


    private static List<Object> getRow(HSSFRow row) {
        ArrayList<Object> values = new ArrayList<>();
        int firstCell = row.getFirstCellNum();
        int lastCell = row.getLastCellNum();
        for (int cellIndex = 0; cellIndex < lastCell; cellIndex++) {
            values.add(cellIndex >= firstCell ? getCellValue(row.getCell(cellIndex)) : null);
        }
        return values;
    }

    protected static Object getCellValue(Cell cell) {
        if (cell != null) {
            CellType cellType = cell.getCellType();
            if (cellType == CellType.STRING) {
                return cell.getStringCellValue();
            } else if (cellType == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DateUtil.getJavaDate(cell.getNumericCellValue());
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return (long) value;
                    } else {
                        return value;
                    }
                }
            } else if (cellType == CellType.BOOLEAN) {
                return cell.getBooleanCellValue();
            }
        }
        return null;
    }
}
