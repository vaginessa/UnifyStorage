package org.cryse.unifystorage.exception;

import org.cryse.unifystorage.StorageException;

public class BadRequestException extends StorageException {
    public BadRequestException() {
    }

    public BadRequestException(String detailMessage) {
        super(detailMessage);
    }

    public BadRequestException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BadRequestException(Throwable throwable) {
        super(throwable);
    }
}