package org.cryse.unifystorage.explorer.utils.openfile;


import org.cryse.unifystorage.explorer.base.IContext;


public interface OpenFileUtils<C, IC extends IContext<C>> {

    void openFileByPath(String filePath, boolean useSystemSelector);

    void openFileByUri(String uriString, boolean useSystemSelector);

}
