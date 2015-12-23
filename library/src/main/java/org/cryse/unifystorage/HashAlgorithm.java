package org.cryse.unifystorage;

import java.io.File;
import java.io.InputStream;

public interface HashAlgorithm {
    String calculate(String content);

    String calculate(File file);

    String calculate(InputStream file);
}
