package org.cryse.unifystorage;

public class StorageException extends RuntimeException {
    private int httpCode;
    public StorageException() {
    }

    public StorageException(String detailMessage) {
        super(detailMessage);
    }

    public StorageException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public StorageException(Throwable throwable) {
        super(throwable);
    }

    public StorageException(int httpCode) {
        super();
        this.httpCode = httpCode;
    }

    public StorageException(int httpCode, String errorMessage) {
        super(errorMessage);
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
