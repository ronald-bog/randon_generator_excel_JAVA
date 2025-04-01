/*package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class ExcelGenerator {
    private static final int NUM_ROWS = 10000;
    private static final int NUM_COLS = 50;
    private static final String DELIMITER = ",";
    private static final String LINE_SEPARATOR = "\n";

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

        try (FileWriter fileWriter = new FileWriter("./datos.csv")) {
            // Escribir encabezados
            for (int i = 0; i < NUM_COLS; i++) {
                fileWriter.append("Col_").append(String.valueOf(i + 1));
                if (i < NUM_COLS - 1) {
                    fileWriter.append(DELIMITER);
                }
            }
            fileWriter.append(LINE_SEPARATOR);

            // Escribir datos
            Object[][] data = generateData(NUM_ROWS, NUM_COLS);
            for (int i = 0; i < NUM_ROWS; i++) {
                for (int j = 0; j < NUM_COLS; j++) {
                    fileWriter.append(data[i][j].toString());
                    if (j < NUM_COLS - 1) {
                        fileWriter.append(DELIMITER);
                    }
                }
                fileWriter.append(LINE_SEPARATOR);
            }

            long endTime = System.currentTimeMillis(); // Terminar medición de tiempo
            System.out.println("Archivo CSV guardado con éxito.");
            System.out.println("Tiempo de generación: " + (endTime - startTime) + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}*/
/*
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
*/
package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ExcelGenerator  {
    private static final int NUM_ROWS = 10000;
    private static final int NUM_COLS = 50;
    private static final int THRESHOLD = 1000; // Filas por hilo

    // Datos compartidos (generados en paralelo)
    private static final Object[][] data = new Object[NUM_ROWS][NUM_COLS];

    // Generar datos en paralelo
    static class GenerateDataTask extends RecursiveAction {
        private final int startRow;
        private final int endRow;

        public GenerateDataTask(int startRow, int endRow) {
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        protected void compute() {
            if (endRow - startRow <= THRESHOLD) {
                Random random = new Random();
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < NUM_COLS; j++) {
                        data[i][j] = random.nextDouble();
                    }
                }
            } else {
                int mid = (startRow + endRow) / 2;
                invokeAll(
                        new GenerateDataTask(startRow, mid),
                        new GenerateDataTask(mid, endRow)
                );
            }
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // Paso 1: Generar datos en paralelo
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(new GenerateDataTask(0, NUM_ROWS));
        pool.shutdown();

        // Paso 2: Escribir en Excel (secuencial, POI no es thread-safe)
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // Escribir encabezados
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < NUM_COLS; i++) {
            headerRow.createCell(i).setCellValue("Col_" + (i + 1));
        }

        // Escribir datos (secuencial)
        for (int i = 0; i < NUM_ROWS; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < NUM_COLS; j++) {
                row.createCell(j).setCellValue((Double) data[i][j]);
            }
        }

        // Guardar archivo
        try (FileOutputStream fileOut = new FileOutputStream("./datos_concurrente.xlsx")) {
            workbook.write(fileOut);
            long endTime = System.currentTimeMillis();
            System.out.println("Archivo generado en " + (endTime - startTime) + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}