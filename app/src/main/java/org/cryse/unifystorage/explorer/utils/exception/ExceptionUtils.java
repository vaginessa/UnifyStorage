package org.cryse.unifystorage.explorer.utils.exception;

import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.explorer.R;

import java.net.SocketTimeoutException;

public class ExceptionUtils {
    public static int exceptionToStringRes(Throwable throwable) {
        int result = R.string.label_error_something_wrong;
        if(throwable instanceof StorageException) {
            Throwable cause = throwable.getCause();
            if(cause != null) {
                if(cause instanceof SocketTimeoutException)
                    result = R.string.label_error_network_error;
            }
        }
        return result;
    }
}
