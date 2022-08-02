package com.helper;



import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
public class ExcelHelper {

    private List<String> fieldNames = new ArrayList<>();
    private Workbook workbook = null;
    private String workbookName = "";

    public ExcelHelper(String workbookName) {
        this.workbookName = workbookName;
        initialize();
    }

    private void initialize() {
        setWorkbook(new HSSFWorkbook());
    }

    public void closeWorksheet() {
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(getWorkbookName());
            getWorkbook().write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean setupFieldForClass(Class<?> clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fieldNames.add(fields[i].getName());
        }
        return true;
    }

    private Sheet getSheetWithName(String name) {
        Sheet sheet = null;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (name.compareTo(workbook.getSheetName(i)) == 0) {
                sheet = workbook.getSheetAt(i);
                break;
            }
        }
        return sheet;
    }

    private void initializeForRead() throws IOException, InvalidFormatException {
        InputStream inp = new FileInputStream(getWorkbookName());
        workbook = WorkbookFactory.create(inp);
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> List<T> readData(String classname) throws Exception {
        initializeForRead();
        Sheet sheet = getSheetWithName("dateSheet");
        Class clazz = Class.forName(classname);

        setupFieldForClass(clazz);
        List<T> result = new ArrayList<T>();
        Row row;
        for (int rowCount = 1; rowCount <= sheet.getLastRowNum(); rowCount++) {
            T one = (T) clazz.newInstance();

            row = sheet.getRow(rowCount);
            int colCount = 1;

            for (int i = 1; i < row.getLastCellNum(); i++) {

                int type = row.getCell(i).getCellType();

                String fieldName = fieldNames.get(colCount++);

                Method method = constructMethod(clazz, fieldName);
                if (type == Cell.CELL_TYPE_STRING) {
                    String value = row.getCell(i).getStringCellValue();
                    method.invoke(one, value);
                } else if (type == Cell.CELL_TYPE_NUMERIC) {
                    Double num = row.getCell(i).getNumericCellValue();
                    Class<?> returnType = getGetterReturnClass(clazz, fieldName);
                    if (returnType == int.class || returnType == Integer.class) {
                        method.invoke(one, num.intValue());
                    } else if (returnType == double.class || returnType == Double.class) {
                        method.invoke(one, num.intValue());
                    } else if (returnType == float.class || returnType == Float.class) {
                        method.invoke(one, num.intValue());
                    } else if (returnType == long.class || returnType == Long.class) {
                        method.invoke(one, num.intValue());
                    } else if (returnType == Date.class) {
                        Date date = HSSFDateUtil.getJavaDate(row.getCell(i).getNumericCellValue());
                        method.invoke(one, date);
                    }
                } else if (type == Cell.CELL_TYPE_BOOLEAN) {
                    boolean num = row.getCell(i).getBooleanCellValue();
                    Object[] values = new Object[1];
                    values[0] = num;
                    method.invoke(one, values);
                }
            }
            result.add(one);
        }
        return result;
    }

    private Class<?> getGetterReturnClass(Class<?> clazz, String fieldName) {
        String methodName = "get" + capitalize(fieldName);
        String methodIsName = "is" + capitalize(fieldName);
        Class<?> returnType = null;
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) || method.getName().equals(methodIsName)) {
                returnType = method.getReturnType();
                break;
            }
        }
        return returnType;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Method constructMethod(Class clazz, String fieldName) throws SecurityException, NoSuchMethodException {
        Class<?> fieldClass = getGetterReturnClass(clazz, fieldName);
        return clazz.getMethod("set" + capitalize(fieldName), fieldClass);
    }

    public <T> void writeDate(List<T> data) throws Exception {
        try {
            Sheet sheet = getWorkbook().createSheet(data.get(0).getClass().getName());
            setupFieldForClass(data.get(0).getClass());
            int rowCount = 0;
            int columnCount = 0;
            Row row = sheet.createRow(rowCount++);
            for (String fieldName : fieldNames) {
                Cell cel = row.createCell(columnCount++);
                cel.setCellValue(fieldName);
            }
            Class<? extends Object> classz = data.get(0).getClass();
            for (T t : data) {
                row = sheet.createRow(rowCount++);
                columnCount = 0;
                for (String fieldName : fieldNames) {
                    Cell cel = row.createCell(columnCount);
                    Method method = hasMethod(classz, fieldName)
                            ? classz.getMethod("get" + capitalize(fieldName))
                            : classz.getMethod("is" + capitalize(fieldName));
                    Object value = method.invoke(t, (Object[]) null);
                    if (value != null) {
                        if (value instanceof String) {
                            cel.setCellValue((String) value);
                        } else if (value instanceof Long) {
                            cel.setCellValue((Long) value);
                        } else if (value instanceof Integer) {
                            cel.setCellValue((Integer) value);
                        } else if (value instanceof Double) {
                            cel.setCellValue((Double) value);
                        } else if (value instanceof Date) {
                            cel.setCellValue((Date) value);
                            CellStyle styleDate = workbook.createCellStyle();
                            DataFormat dataFormatDate = workbook.createDataFormat();
                            styleDate.setDataFormat(dataFormatDate.getFormat("m/d/yy"));
                            cel.setCellStyle(styleDate);
                        } else if (value instanceof Boolean) {
                            cel.setCellValue((Boolean) value);
                        }
                    }
                    columnCount++;
                }
            }
            // 자동맞춤
            for (int i=0; i<fieldNames.size(); i++)
                sheet.autoSizeColumn(i);
            FileOutputStream out = new FileOutputStream(new File(workbookName));
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            System.out.println("에러났습니다 : " + e);
        }


    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean hasMethod(Class classz, String fieldName) {
        try {
            classz.getMethod("get" + capitalize(fieldName));
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    private String capitalize(String string) {
        String capital = string.substring(0, 1).toUpperCase();
        return capital + string.substring(1);
    }



}
