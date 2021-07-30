package de.fraunhofer.iosb.svs.spc.exceptions;

public class FTPConnectionException extends RuntimeException {
    public FTPConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
