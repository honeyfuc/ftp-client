package ru.yankovyaroslav.ftp.domain.exception;

public class FtpServerConnectionException extends RuntimeException {
    public FtpServerConnectionException(String message) {
        super(message);
    }
}
