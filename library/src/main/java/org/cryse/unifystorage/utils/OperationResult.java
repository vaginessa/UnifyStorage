package org.cryse.unifystorage.utils;

import android.util.Pair;

import org.cryse.unifystorage.RemoteFile;

public class OperationResult extends Pair<RemoteFile, Boolean> {

    public OperationResult(RemoteFile file, Boolean result) {
        super(file, result);
    }

    public static OperationResult create(RemoteFile a, Boolean b) {
        return new OperationResult(a, b);
    }
}

