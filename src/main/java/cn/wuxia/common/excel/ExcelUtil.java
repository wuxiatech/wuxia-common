package cn.wuxia.common.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import cn.wuxia.common.excel.annotation.ExcelColumn;
import cn.wuxia.common.excel.bean.ExcelBean;
import cn.wuxia.common.excel.exception.ExcelException;
import cn.wuxia.common.exception.AppServiceException;
import cn.wuxia.common.exception.ValidateException;
import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.common.util.SystemUtil;
import cn.wuxia.common.util.ValidatorUtil;
import cn.wuxia.common.util.reflection.ReflectionUtil;
import jodd.typeconverter.TypeConverterManager;

/**
 * [ticket id] Description of the class
 *
 * @author songlin.li @ Version : V<Ver.No> <May 17, 2012>
 */
public class ExcelUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    private static final int MAX_ROWS = 65535;

    public static Workbook getWorkbookFormExcel(File file) throws FileNotFoundException, IOException {
        Workbook wb = null;

        if (file.getName().toUpperCase().endsWith(".XLS")) {
            wb = createHSSFWorkbook(file);
        } else if (file.getName().toUpperCase().endsWith(".XLSX")) {
            wb = createXSSFWorkbook(file);
        } else {
            throw new AppServiceException(file.getName() + " is not valid excel file.");
        }
        return wb;
    }

    /**
     * @param excelBean
     * @param outputStream
     * @throws Exception
     * @description : map object
     * @author songlin.li
     */
    public static void createExcel(ExcelBean excelBean, OutputStream outputStream) throws Exception {
        long start = System.currentTimeMillis();
        logger.debug("create Excel begin...");
        Workbook wb = null;

        if (excelBean.getFileName().toUpperCase().endsWith(".XLS")) {
            wb = createHSSFWorkbook(excelBean);
        } else if (excelBean.getFileName().toUpperCase().endsWith(".XLSX")) {
            wb = createXSSFWorkbook(excelBean);
        } else {
            throw new Exception("filename is:" + excelBean.getFileName() + " filename should end with .xls or .xlsx");
        }

        long end = System.currentTimeMillis();
        logger.debug("create Excel end, Used " + (end - start) + " ms");
        wb.write(outputStream);
        outputStream.flush();
    }

    private static Workbook createHSSFWorkbook(File file) throws FileNotFoundException, IOException {
        HSSFWorkbook workBook = new HSSFWorkbook(new FileInputStream(file));
        return workBook;
    }

    @SuppressWarnings("unchecked")
    private static Workbook createHSSFWorkbook(ExcelBean excelBean) throws Exception {
        HSSFWorkbook workBook = new HSSFWorkbook();
        List<?> list = excelBean.getDataList();
        int sheetSizes = list.size() / MAX_ROWS + 1;
        if (list.size() % MAX_ROWS == 0) {
            sheetSizes -= 1;
        }
        int listStart = 0;
        for (int s = 1; s < sheetSizes + 1; s++) {
            int listEnd = listStart + MAX_ROWS;
            if (list.size() < listEnd) {
                listEnd = list.size();
            }
            List<?> pageList = list.subList(listStart, listEnd);
            HSSFSheet sheet = workBook.createSheet(excelBean.getSheetName() + s);
            HSSFRow row = sheet.createRow(0);
            String[] head = excelBean.getSelfieldsName();
            for (int i = 0; i < head.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(head[i]);
                cell.setCellStyle(setHSSFCellStyle(workBook, true));
            }

            CellStyle cs = setHSSFCellStyle(workBook, false);
            for (int i = 0; i < pageList.size(); i++) {
                row = sheet.createRow(i + 1);
                Object object = pageList.get(i);
                if (object instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) object;
                    for (int j = 0; j < excelBean.getSelfields().length; j++) {
                        String key = excelBean.getSelfields()[j];
                        HSSFCell cell = row.createCell(j);
                        cell.setCellValue(StringUtil.isNotBlank(map.get(key)) ? map.get(key).toString() : "");
                        cell.setCellStyle(cs);
                    }
                } else {
                    for (int j = 0; j < excelBean.getSelfields().length; j++) {
                        String name = excelBean.getSelfields()[j];
                        HSSFCell cell = row.createCell(j);
                        cell.setCellValue(BeanUtils.getSimpleProperty(object, name));
                        cell.setCellStyle(cs);
                    }
                }
            }
            for (int i = 0; i < head.length; i++) {
                sheet.autoSizeColumn(i);
                int colwidth = sheet.getColumnWidth(i);
                if (colwidth < 10 * 2 * 256) {
                    sheet.setColumnWidth(i, 10 * 2 * 256);
                }
            }
            listStart += MAX_ROWS;
        }
        return workBook;
    }

    private static Workbook createXSSFWorkbook(File file) throws FileNotFoundException, IOException {
        return new XSSFWorkbook(new FileInputStream(file));
    }

    @SuppressWarnings("unchecked")
    private static Workbook createXSSFWorkbook(ExcelBean excelBean) throws Exception {
        XSSFWorkbook workBook = new XSSFWorkbook();
        List<?> list = excelBean.getDataList();
        int sheetSizes = list.size() / MAX_ROWS + 1;
        if (list.size() % MAX_ROWS == 0) {
            sheetSizes -= 1;
        }
        int listStart = 0;
        for (int s = 1; s < sheetSizes + 1; s++) {
            int listEnd = listStart + MAX_ROWS;
            if (list.size() < listEnd) {
                listEnd = list.size();
            }
            List<?> pageList = list.subList(listStart, listEnd);
            XSSFSheet sheet = workBook.createSheet(excelBean.getSheetName() + s);
            XSSFRow row = sheet.createRow(0);
            String[] head = excelBean.getSelfieldsName();
            CellStyle cstitle = setXSSFCellStyle(workBook, true);
            for (int i = 0; i < head.length; i++) {
                XSSFCell cell = row.createCell(i);
                cell.setCellStyle(cstitle);
                cell.setCellValue(head[i]);
            }

            CellStyle cs = setXSSFCellStyle(workBook, false);
            for (int i = 0; i < pageList.size(); i++) {
                row = sheet.createRow(i + 1);
                Object object = pageList.get(i);
                if (object instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) object;
                    for (int j = 0; j < excelBean.getSelfields().length; j++) {
                        String key = excelBean.getSelfields()[j];
                        XSSFCell cell = row.createCell(j);
                        cell.setCellValue(StringUtil.isNotBlank(map.get(key)) ? map.get(key).toString() : "");
                        cell.setCellStyle(cs);
                    }
                } else {
                    for (int j = 0; j < excelBean.getSelfields().length; j++) {
                        String name = excelBean.getSelfields()[j];
                        XSSFCell cell = row.createCell(j);

                        try {
                            cell.setCellValue(BeanUtils.getProperty(object, name));
                        } catch (java.lang.NoSuchMethodException e) {
                            logger.warn("", e);
                            cell.setCellValue("");
                        }
                        cell.setCellStyle(cs);
                    }
                }
            }
            sheet.setDefaultRowHeight((short) (1.5 * 256)); //设置默认行高，表示2个字符的高度
            sheet.setDefaultColumnWidth(10);//设置默认列宽，实际上回多出2个字符，不知道为什么
            //这只poi组件中的两个方法，需要注意的是，必须先设置列宽然后设置行高，不然列宽没有效果
            for (int i = 0; i < head.length; i++) {
                int bcolwidth = sheet.getColumnWidth(i);
                sheet.autoSizeColumn(i);
                int acolwidth = sheet.getColumnWidth(i);

                System.out.println("列" + i + "，宽度前：" + bcolwidth + "  宽度后：" + acolwidth);
                if (acolwidth < bcolwidth && SystemUtil.IS_OS_LINUX) {
                    System.out.println("列：" + i + "宽:" + bcolwidth);
                    sheet.setColumnWidth(i, bcolwidth);
                }
            }
            listStart = listEnd;
        }
        return workBook;
    }

    private static HSSFFont setHSSFFont(HSSFWorkbook workBook, boolean isTitle) {
        /**
         * Create a font
         */
        HSSFFont font = workBook.createFont();
        /**
         * font color
         */
        // font.setColor(HSSFColor.BLACK.index);
        /**
         * font size
         */
        if (isTitle) {
            font.setFontHeightInPoints((short) 12);
            /**
             * font bold
             */
            font.setBold(true);
        } else {

        }
        /**
         * font
         */
        // font.setFontName("宋体");
        /**
         * FontItalic As Boolean
         */
        font.setItalic(false);
        /**
         * If there is a delete line
         */
        font.setStrikeout(false);
        /**
         * Set superscript, subscript
         */
        font.setTypeOffset(HSSFFont.SS_NONE);
        /**
         * underline
         */
        // font.setUnderline(HSSFFont.U_NONE);
        return font;
    }

    private static HSSFCellStyle setHSSFCellStyle(HSSFWorkbook workBook, boolean isTitle) {
        HSSFCellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFont(setHSSFFont(workBook, isTitle));
        // cellStyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        // cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        /**
         * set border
         */
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        /**
         * border color
         */
        cellStyle.setLeftBorderColor(HSSFColorPredefined.BLACK.getIndex());
        cellStyle.setRightBorderColor(HSSFColorPredefined.BLACK.getIndex());
        cellStyle.setBottomBorderColor(HSSFColorPredefined.BLACK.getIndex());
        cellStyle.setTopBorderColor(HSSFColorPredefined.BLACK.getIndex());
        return cellStyle;
    }

    private static XSSFFont setXSSFFont(XSSFWorkbook workBook, boolean isTitle) {
        /**
         * Create a font
         */
        XSSFFont font = workBook.createFont();
        /**
         * font color
         */
        // font.setColor(HSSFColor.BLACK.index);
        /**
         * font size
         */
        if (isTitle) {
            font.setFontHeightInPoints((short) 12);
            /**
             * font bold
             */
            font.setBold(true);
        } else {

        }
        /**
         * font
         */
        // font.setFontName("宋体");
        /**
         * FontItalic As Boolean
         */
        font.setItalic(false);
        /**
         * If there is a delete line
         */
        font.setStrikeout(false);
        /**
         * Set superscript, subscript
         */
        font.setTypeOffset(XSSFFont.SS_NONE);
        /**
         * underline
         */
        // font.setUnderline(HSSFFont.U_NONE);
        return font;
    }

    private static XSSFCellStyle setXSSFCellStyle(XSSFWorkbook workBook, boolean isTitle) {
        XSSFCellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFont(setXSSFFont(workBook, isTitle));
        // cellStyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        // cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        /**
         * set border
         */
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        /**
         * border color
         */
        cellStyle.setLeftBorderColor((short) 8);
        cellStyle.setRightBorderColor((short) 8);
        cellStyle.setBottomBorderColor((short) 8);
        cellStyle.setTopBorderColor((short) 8);
        return cellStyle;
    }

    /**
     * 按照指定列名导出为excel
     *
     * @param excelName
     * @param sheetName
     * @param excelHeader
     * @param lists
     * @param request
     * @param response
     * @author songlin.li
     */
    public static void export(String excelName, String sheetName, String[] excelHeader, List<?> lists, HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            // 创建Excel对象
            HSSFWorkbook wb = new HSSFWorkbook();
            // 创建工作单
            HSSFSheet sheet = wb.createSheet(sheetName);

            // 创建行对象
            HSSFRow row = sheet.createRow(0);

            // 创建标题
            for (int i = 0; i < excelHeader.length; i++) {
                // 创建单元格对象
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(excelHeader[i]);
            }

            // 创建数据
            for (int i = 0; i < lists.size(); i++) {
                row = sheet.createRow(i + 1);
                Object obj = lists.get(i);
                Field[] fields = obj.getClass().getDeclaredFields();

                for (int j = 0; j < fields.length; j++) {
                    Field field = fields[j];
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    Object value = field.get(obj);
                    HSSFCell cell = row.createCell(j);
                    cell.setCellValue((value != null) ? value.toString() : "");
                }

            }

            // 转码
            String agent = request.getHeader("user-agent");
            if (agent.toLowerCase().indexOf("msie") != -1) { // IE
                excelName = URLEncoder.encode(excelName, "UTF-8");
            } else {
                excelName = new String(excelName.getBytes("UTF-8"), "ISO-8859-1");
            }

            response.setHeader("Content-Disposition", "attachment;filename=" + excelName + ".xls");
            // 写出Excel
            wb.write(response.getOutputStream());
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 导入xls文档
     *
     * @param file
     * @param clazz 需要导入的xls文件
     * @author songlin.li
     */
    public static <T> List<T> importExcel(File file, Class<T> clazz) throws ExcelException {
        if (file != null && file.exists() && !file.isDirectory()) {
            try {
                return importExcel(FileUtils.openInputStream(file), clazz);
            } catch (IOException | EncryptedDocumentException | InvalidFormatException e) {
                logger.error("", e);
                throw new ExcelException("文件不存在！", e);
            }
        } else {
            throw new ExcelException("文件不存在！");
        }
    }

    /**
     * 导入xls文档
     *
     * @param inputStream
     * @param clazz       需要导入的xls文件
     * @throws IOException
     * @throws InvalidFormatException
     * @throws EncryptedDocumentException
     * @author songlin.li
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz)
            throws ExcelException, EncryptedDocumentException, InvalidFormatException, IOException {
        // 创建最终返回的集合
        List<T> lists = new ArrayList<>();
        // 获得工作薄
        Workbook wb = WorkbookFactory.create(inputStream);
        // 获得第一个工作单
        Sheet sheet = wb.getSheetAt(0);
        // 获得行迭带器
        Iterator<Row> rows = sheet.iterator();

        int index = 0;
        List<String> exceptions = Lists.newArrayList();
        while (rows.hasNext()) {
            Row row = rows.next();
            if (index > 0 && !isRowEmpty(row)) {
                // 导入文件不需要标题
                try {
                    T obj = getRowObject(row, clazz);
                    ValidatorUtil.validate(obj);
                    lists.add(obj);
                } catch (ValidateException e) {
                    logger.error("", e);
                    exceptions.add("第" + (index + 1) + "行，" + e.getMessage());
                } catch (Exception e) {
                    logger.error("", e);
                    exceptions.add("第" + (index + 1) + "行，" + e.getMessage());
                }
            }
            index++;
        }
        if (ListUtil.isNotEmpty(exceptions)) {
            throw new ExcelException(StringUtil.join(exceptions, "\t\n"));
        }
        IOUtils.closeQuietly(inputStream);
        return lists;
    }

    /**
     * 校验是否为空行
     *
     * @param row
     * @return
     */
    public static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && CellType.BLANK.compareTo(cell.getCellTypeEnum()) != 0)
                return false;
        }
        return true;
    }

    /**
     * 得到一个单元格内的值, 并根据特定类型赋值
     *
     * @param cell
     * @param bean
     * @param field
     * @return
     * @author songlin.li
     */
    public static void setFieldCellValue(Cell cell, Object bean, Field field) {
        if (cell == null) {
            return;
        }
        Object fieldValue = null;
        CellType cellType = cell.getCellTypeEnum();
        String fieldType = field.getType().getName();
        switch (cellType) {
            case STRING:
                fieldValue = StringUtil.trim(cell.getStringCellValue());
                break;

            case NUMERIC:
                Double value = cell.getNumericCellValue();
                // 读取日期进行
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    ExcelColumn an = field.getAnnotation(ExcelColumn.class);
                    java.util.Date value2 = org.apache.poi.ss.usermodel.DateUtil.getJavaDate((Double) value);
                    fieldValue = DateUtil.dateToString(value2, an.dateFormat());
                } else if (fieldType.equals("java.lang.Integer") || fieldType.equals("int") || fieldType.equals("java.lang.Long")
                        || fieldType.equals("long")) {
                    DecimalFormat df = new DecimalFormat("#");// 转换成整型
                    fieldValue = df.format(value);
                } else if (fieldType.equals("java.lang.String")) {
                    cell.setCellType(CellType.STRING);
                    fieldValue = StringUtil.trim(cell.getStringCellValue());
                } else
                    fieldValue = value;
                break;
            case BOOLEAN:
                fieldValue = cell.getBooleanCellValue();
                break;

            case FORMULA:
                fieldValue = cell.getArrayFormulaRange().formatAsString();
                break;
            case BLANK:
                fieldValue = "";
                break;
        }
        field.setAccessible(true);
        if (fieldValue != null) {
            try {
                Object convertValue = TypeConverterManager.get().convertType(fieldValue, field.getType());
                field.set(bean, convertValue);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.warn(e.getMessage());
                throw new ExcelException("无法赋值，原因是字段类型：" + field.getType() + "，值类型：" + fieldValue.getClass() + "，值：" + fieldValue);
            }
        }
    }

    public static <T> T getRowObject(Row row, Class<T> clazz) throws ExcelException, InstantiationException, IllegalAccessException {
        // 创建集合用于保存一行的单元格数据
        List<String> exceptions = Lists.newArrayList();
        // 创建对象,注入数据
        T obj = clazz.newInstance();
        List<Field> fields = ReflectionUtil.getAccessibleFields(clazz);
        for (Field field : fields) {
            if (field.isAccessible() && !StringUtil.equals("serialVersionUID", field.getName())) {
                ExcelColumn excelHead = ReflectionUtil.getAnnotation(field, ExcelColumn.class);
                if (excelHead == null) {
                    Method method = ReflectionUtil.getGetterMethodByPropertyName(obj, field.getName());
                    if (null != method) {
                        excelHead = ReflectionUtil.getAnnotation(method, ExcelColumn.class);
                    }
                }
                if (excelHead != null) {
                    try {
                        Cell cell = row.getCell(excelHead.colunmIndex());
                        if (cell != null) {
                            /**
                             * 根据field类型转换相应值
                             */
                            setFieldCellValue(cell, obj, field);
                        }
                    } catch (ExcelException e) {
                        exceptions.add("第" + (excelHead.colunmIndex() + 1) + "列，表头为：" + excelHead.columnName() + "，赋值属性名：" + field.getName() + "，值："
                                + ReflectionUtil.getFieldValue(obj, field.getName()) + "【详细错误】" + e.getMessage());
                    }
                } else {
                    logger.info("跳过：" + field.getName() + "赋值");
                }
            } else {
                logger.info("跳过不可访问的属性：" + field.getName());
            }

        }
        if (ListUtil.isNotEmpty(exceptions)) {
            throw new ExcelException(StringUtil.join(exceptions, "；"));
        }
        return obj;
    }

}
