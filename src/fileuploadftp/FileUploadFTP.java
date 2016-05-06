package fileuploadftp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileUploadFTP {

    static String productContent = "G:/Product Content/PRODUCTS/";
    static String dst = "/Design/Supershift S&L/PRODUCTS/";
    static String excelSource = "G:\\CM\\Category Management Only\\_S0000_Trade marketing\\Pictures Spaceman\\SAP_EAN.xlsx";
    static Map<String, String> myMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        FileWriter fw = new FileWriter("H:/Logs/FileUploadFtp.log", true);
        BufferedWriter bw = new BufferedWriter(fw);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        bw.newLine();
        bw.write(sdf.format(new Date()));
        File source = new File(productContent);

        FileInputStream fis_excel = null;
        try {
            fis_excel = new FileInputStream(excelSource);
            XSSFWorkbook wb_excel = new XSSFWorkbook(fis_excel);
            XSSFSheet sheet_excel = wb_excel.getSheetAt(0);
            Iterator rows = sheet_excel.rowIterator();
            while (rows.hasNext()) {
                XSSFRow row = (XSSFRow) rows.next();
                if (row.getCell(1) != null) {
                    myMap.put(row.getCell(0).toString(), row.getCell(1).toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis_excel != null) {
                fis_excel.close();
            }
        }

        String[] directories = source.list(new FilenameFilter() {

            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        List fileUpd = new ArrayList();
        for (int i = 0; i < directories.length; i++) {

            String[] files = new File(productContent + "/" + directories[i]).list(new FilenameFilter() {

                @Override
                public boolean accept(File current, String name) {
                    return (new File(current, name).isFile() 
                            && (new Date(new File(current, name).lastModified()).after(new Date(new Date().getTime() - (1 * 1000 * 60 * 60 * 24)))) 
                            && !name.equals("Thumbs.db")
                            && !name.contains("testDoC_")
                            && !name.contains("repealed_DoC"));
                }
            });

            for (int j = 0; j < files.length; j++) {
                fileUpd.add(directories[i] + "-" + files[j]);
                System.out.println(files[j] + " \t " + sdf.format(new File(productContent + "/" + directories[i] + "/" + files[j]).lastModified()));
                bw.newLine();
                bw.write(files[j] + " \t " + sdf.format(new File(productContent + "/" + directories[i] + "/" + files[j]).lastModified()));
            }
        }
        if (fileUpd.size() > 0) {
            FTPClient ftpClient = new FTPClient();
            Utils con = new Utils();
            if (con.connect(ftpClient)) {
                System.out.println("\nConnected to FTP...\n");
                bw.newLine();
                bw.newLine();
                bw.write("Connected to FTP...");
                bw.newLine();
                for (int i = 0; i < fileUpd.size(); i++) {
                    String folderToUpd = fileUpd.get(i).toString().substring(0, fileUpd.get(i).toString().indexOf("-"));
                    String sap = folderToUpd.substring(0, 2) + "." + folderToUpd.substring(2, 5) + "." + folderToUpd.substring(5, 7);
                    String oldFileName = fileUpd.get(i).toString().substring(fileUpd.get(i).toString().indexOf("-") + 1, fileUpd.get(i).toString().length());
                    StringBuffer currentFileName = new StringBuffer(oldFileName);
                    String newFilename = currentFileName.insert(currentFileName.indexOf(folderToUpd) + 7, "_" + myMap.get(sap).replace("/", "_")).toString();

                    FileUploadFTP(bw, ftpClient, folderToUpd, oldFileName, newFilename);
                }
            }
            if (con.disconnect(ftpClient)) {
                System.out.println("\nDisconnected");
                bw.newLine();
                bw.write("Disconnected");
                System.out.println("----------------------------------------------");
                bw.newLine();
                bw.write("----------------------------------------------");
            }
        }
        bw.flush();
        bw.close();
    }

    static void FileUploadFTP(BufferedWriter bw, FTPClient ftpClient, String folderName, String oldFileName, String newFileName) throws IOException {

        boolean existed = ftpClient.changeWorkingDirectory(dst + "/" + folderName);
        if (!existed) {
            boolean created = ftpClient.makeDirectory(dst + "/" + folderName);
            if (created) {
                System.out.println("CREATED directory: " + folderName);
                bw.newLine();
                bw.write("CREATED directory: " + folderName);
            } else {
                System.out.println("COULD NOT create directory: " + folderName);
                bw.newLine();
                bw.write("COULD NOT create directory: " + folderName);
            }
        }

        File srcPath = new File(productContent + "/" + folderName + "/" + oldFileName);
        if (!srcPath.exists()) {
            System.out.println(srcPath + " file doesn't exist.");
            bw.newLine();
            bw.write(srcPath + " file doesn't exist.");
        } else {
            InputStream inputStream = new FileInputStream(srcPath);
            System.out.print("Start uploading " + oldFileName + " into " + newFileName);
            bw.newLine();
            bw.write("Start uploading " + oldFileName + " into " + newFileName);

            boolean done = ftpClient.storeFile(dst + "/" + folderName + "/" + newFileName, inputStream);

            inputStream.close();
            if (done) {
                System.out.println("\t - Successfully uploaded.");
                bw.write("\t - Successfully uploaded.");
            } else {
                System.out.println("\t - Not uploaded.");
                bw.write("\t - Not uploaded.");
            }
        }
    }
}
