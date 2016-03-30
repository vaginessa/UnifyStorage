package org.cryse.unifystorage.exception;

import org.cryse.unifystorage.StorageException;

public class BuildUriException extends StorageException {
    public BuildUriException() {
    }

    public BuildUriException(String detailMessage) {
        super(detailMessage);
    }

    public BuildUriException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BuildUriException(Throwable throwable) {
        super(throwable);
    }
}