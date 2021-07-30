package de.fraunhofer.iosb.svs.spc;

import de.fraunhofer.iosb.svs.spc.exceptions.FTPConnectionException;

import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class FTPClientService {
    private static final Logger log = LoggerFactory.getLogger(FTPClientService.class);

    private final String ontologiesMountpoint;
    private final String host;
    private final int port;
    private final String path;

    public FTPClientService(@Value("${mountpoint.ontologies}") String ontologiesMountpoint) {
        this.ontologiesMountpoint = ontologiesMountpoint;
        log.debug("Found ontologies mountpoint: {}", this.ontologiesMountpoint);
        try {
            URI uri = new URI(this.ontologiesMountpoint);
            if (!uri.getScheme().equals("ftp")) {
                throw new RuntimeException("FTP Client needs an uri with ftp scheme");
            }
            host = uri.getHost();
            log.debug("Set FTP host '{}'", host);
            if (uri.getPort() == -1) {
                log.debug("Port not specified. Using default port 21");
                port = 21;
            } else {
                port = uri.getPort();
            }
            log.debug("Set FTP port '{}'", port);
            path = uri.getPath();
            log.debug("Set FTP path '{}'", uri.getPath());
        } catch (URISyntaxException e) {
            log.warn("Mountpoint ontologies '{}' not a valid uri. Reason: '{}'", e.getInput(), e.getReason(), e);
            throw new RuntimeException("FTP Client has no valid uri", e);
        }
    }

    public String createDirectory(String childDirectory, String parentDirecotry) {
        FTPSClient ftpClient = new FTPSClient();
        ftpClient.setRemoteVerificationEnabled(false);
        
        Boolean result = false;
        
        try {
            ftpClient.connect(host, port);
            ftpClient.enterLocalPassiveMode();
            ftpClient.login("anonymous", "");
            ftpClient.changeWorkingDirectory(parentDirecotry);
            result =  ftpClient.makeDirectory(childDirectory);
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        if(result) {
            log.debug("Created directory '{}' under '{}' ", childDirectory, parentDirecotry );
        }
        else {
            log.debug("Creating directory failed");
        }
        
        String createdDirectoryPath = parentDirecotry + "/" + childDirectory;
        return createdDirectoryPath;
    }
    
    public void uploadFile(String fileName, InputStream inputStream) {
        FTPSClient ftpClient = new FTPSClient();
        ftpClient.setRemoteVerificationEnabled(false);
        
        try {
            ftpClient.connect(host, port);

            ftpClient.enterLocalPassiveMode();
            ftpClient.login("anonymous", "");
            ftpClient.changeWorkingDirectory(path);
            log.debug("Store file {} to {}", fileName, path);
            ftpClient.storeFile(fileName, inputStream);
            ftpClient.logout();
        } catch (IOException ex) {
            log.error("Problem with FTP server", ex);
            throw new FTPConnectionException("Caught IO exception", ex);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                log.error("Exception while closing", ex);
            }
        }
    }

    public String uploadFile(String fileName, String location, InputStream inputStream) {        
        FTPSClient ftpClient = new FTPSClient();
        ftpClient.setRemoteVerificationEnabled(false);
        
        try {
            ftpClient.connect(host, port);

            ftpClient.enterLocalPassiveMode();
            ftpClient.login("anonymous", "");
            ftpClient.changeWorkingDirectory(location);
            log.debug("Store file {} to {}", fileName, location);
            
            ftpClient.storeFile(fileName, inputStream);
            ftpClient.logout();
        } catch (IOException ex) {
            log.error("Problem with FTP server", ex);
            throw new FTPConnectionException("Caught IO exception", ex);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                log.error("Exception while closing", ex);
            }
        }
        
        return location + "/" + fileName;
    }

}
