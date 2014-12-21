package org.greenvilleoaks

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/**
 * This class will create a spreadsheet with multiple sheets.
 * Each sheet will have 3 columns representing a histogram
 * of some particular view of the create.
 */
final class Spreadsheet {
    private final XSSFWorkbook wb = new XSSFWorkbook();
    private final Map<String, CellStyle> styles = createStyles(wb);


    /**
     * Add a sheet to the workbook
     * @param title The title of the sheet
     * @param headers The column headers
     * @param content The content of the sheet, where the order of the map keys corresponds to the headers
     */
    public void addContent(
            final String title,
            final String[] headers,
            final List<Map<String, String>> content) {
        Sheet sheet = createSheet(title)
        createTitleRow(sheet, title, headers.size())
        createHeaderRow(sheet, headers)
        createContent(sheet, headers, content)
    }


    private Sheet createSheet(final String title) {
        Sheet sheet = wb.createSheet(title);
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
        return sheet
    }


    private void createTitleRow(final Sheet sheet, final String title, final int titleCellWidth) {
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(45);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(styles.get("title"));

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        String lastColumn = titleCellWidth < alphabet.size() ? alphabet.charAt(titleCellWidth - 1) : "Z"
        sheet.addMergedRegion(CellRangeAddress.valueOf('$A$1:$' + lastColumn + '$1'));
    }



    private void createHeaderRow(final Sheet sheet, final String[] headers) {
        Row headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(40);
        Cell headerCell;
        for (int i = 0; i < headers.length; i++) {
            headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(styles.get("header"));

            // Set the cell width based on the width of the column header
            sheet.setColumnWidth(i, headers[i].size()*256);
        }
    }


    private void createContent(
            final Sheet sheet,
            final String[] headers,
            final List<Map<String, String>> stats) {
        int rownum = 2;
        stats.each { Map<String, String> stat ->
            Row row = sheet.createRow(rownum++);
            for (int j = 0; j < headers.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(styles.get("cell"));
                cell.setCellValue(stat.get(headers[j]))
            }
        }
    }



    /**
     * Write the workbook to a file.  The name of the file will created automatically and is guaranteed to be unique.
     * @param dirName The directory where the file should be created.
     */
    public void writeToFile(final String dirName) {
        String file = "${dirName}\\MemberStat-${System.currentTimeMillis()}.xlsx";

        File dir = new File(dirName)
        if (!dir.exists()) dir.mkdirs()

        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();
    }



    /**
     * Create a library of cell styles
     */
    private static Map<String, CellStyle> createStyles(XSSFWorkbook wb){
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        CellStyle style;
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short)18);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFont(titleFont);
        styles.put("title", style);

        Font monthFont = wb.createFont();
        monthFont.setFontHeightInPoints((short)11);
        monthFont.setColor(IndexedColors.WHITE.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(monthFont);
        style.setWrapText(true);
        styles.put("header", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        styles.put("cell", style);

        return styles;
    }
}
