package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class ExcelGenerator {
    private static final int NUM_ROWS = 10000;
    private static final int NUM_COLS = 50;

    // Generar datos ficticios
    private static Object[][] generateData(int numRows, int numCols) {
        Object[][] data = new Object[numRows][numCols];
        Random random = new Random();

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                data[i][j] = random.nextDouble();
            }
        }
        return data;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // Iniciar medición de tiempo

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // Escribir encabezados
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < NUM_COLS; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue("Col_" + (i + 1));
        }

        // Escribir datos
        Object[][] data = generateData(NUM_ROWS, NUM_COLS);
        for (int i = 0; i < NUM_ROWS; i++) {
            Row row = sheet.createRow(i + 1); // +1 porque la primera fila es para encabezados
            for (int j = 0; j < NUM_COLS; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue((Double) data[i][j]);
            }
        }

        // Guardar archivo
        try (FileOutputStream fileOut = new FileOutputStream("./datos.xlsx")) {
            workbook.write(fileOut);
            workbook.close();
            long endTime = System.currentTimeMillis(); // Terminar medición de tiempo
            System.out.println("Archivo guardado con éxito.");
            System.out.println("Tiempo de generación: " + (endTime - startTime) + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
