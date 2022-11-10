package dk.magenta.datafordeler.ger.parser;

import dk.magenta.datafordeler.ger.data.RawData;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 26-11-15.
 */
public class XlsxConverter extends XlsConverter {

    protected String[] getApplicableContentTypes() {
        return new String[]{
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/wps-office.xlsx"
        };
    }

    protected String[] getApplicableFileExtensions() {
        return new String[]{
                "xlsx"
        };
    }

    public Map<String, List<RawData>> convert(InputStream data) throws Exception {
        return this.convert(new XSSFWorkbook(data));
    }

    public Map<String, List<RawData>> convert(File data) throws Exception {
        return this.convert((XSSFWorkbook) WorkbookFactory.create(data));
    }

    private Map<String, List<RawData>> convert(XSSFWorkbook document) throws Exception {
        HashMap<String, List<RawData>> documentConversion = new HashMap<>();
        int sheetCount = document.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            ArrayList<RawData> sheetConversion = new ArrayList<>();
            XSSFSheet sheet = document.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            int firstRowIndex = sheet.getFirstRowNum();
            int rowCount = sheet.getLastRowNum() + 1;
            ArrayList<String> columnNames = new ArrayList<>();
            for (Object value : getRow(sheet.getRow(firstRowIndex))) {
                columnNames.add(value.toString());
            }
            for (int rowIndex = firstRowIndex + 1; rowIndex < rowCount; rowIndex++) {
                RawData rowConversion = new RawData();
                XSSFRow row = sheet.getRow(rowIndex);
                if (row != null) {
                    List<Object> values = getRow(row);
                    if (!values.isEmpty()) {
                        for (int cellIndex = 0; cellIndex < values.size(); cellIndex++) {
                            rowConversion.put(columnNames.get(cellIndex), values.get(cellIndex));
                        }
                        sheetConversion.add(rowConversion);
                    }
                }
            }
            documentConversion.put(sheetName, sheetConversion);
        }
        return documentConversion;
    }

    private static List<Object> getRow(XSSFRow row) {
        ArrayList<Object> values = new ArrayList<>();
        int firstCell = row.getFirstCellNum();
        int lastCell = row.getLastCellNum();
        for (int cellIndex = 0; cellIndex < lastCell; cellIndex++) {
            values.add(cellIndex >= firstCell ? getCellValue(row.getCell(cellIndex)) : null);
        }
        return values;
    }

}
