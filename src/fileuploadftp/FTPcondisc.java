package fileuploadftp;

import java.io.IOException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FTPcondisc {

    String server = "ftp.tristar.eu";
    int port = 21;
    String user = "SL-scripts";
    String pass = "uVciVV7v";

    public boolean connect(FTPClient ftpClient) {
        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return true;
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean disconnect(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                return true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }
}
