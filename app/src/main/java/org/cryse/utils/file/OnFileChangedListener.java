package org.cryse.utils.file;

public interface OnFileChangedListener {
    boolean onFileCreate(String path, String file);
    boolean onFileDelete(String path, String file);
    boolean onFileModify(String path, String file);
    boolean onFileEvent(int event, String path, String file);
}
