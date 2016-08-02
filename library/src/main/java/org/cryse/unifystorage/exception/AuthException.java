package org.cryse.unifystorage.exception;

import org.cryse.unifystorage.StorageException;

public class AuthException extends StorageException {
    public String mErrorMessage;
    public String mErrorDescription;
    public String mErrorUri;

    public AuthException() {
    }

    public AuthException(String detailMessage) {
        super(detailMessage);
    }

    public AuthException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public AuthException(Throwable throwable) {
        super(throwable);
    }

    public AuthException(String errorMessage, String errorDescription, String errorUri) {
        this.mErrorMessage = errorMessage;
        this.mErrorDescription = errorDescription;
        this.mErrorUri = errorUri;
    }
}
