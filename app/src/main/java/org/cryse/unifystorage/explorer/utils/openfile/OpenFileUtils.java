package org.cryse.unifystorage.explorer.utils.openfile;

public interface OpenFileUtils {

    void openFileByPath(String filePath, boolean useSystemSelector);

    void openFileByUri(String uriString, boolean useSystemSelector);

}
