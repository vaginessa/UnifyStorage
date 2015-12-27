package org.cryse.unifystorage.utils.hash;

import org.cryse.unifystorage.HashAlgorithm;

import java.io.File;
import java.io.InputStream;

public enum Sha1HashAlgorithm implements HashAlgorithm {
    INSTANCE;

    public static HashAlgorithm getInstance() {
        return INSTANCE;
    }

    @Override
    public String calculate(String content) {
        return null;
    }

    @Override
    public String calculate(File file) {
        return null;
    }

    @Override
    public String calculate(InputStream file) {
        return null;
    }
}
