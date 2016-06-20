package org.cryse.unifystorage.explorer;

public class DataContract {
    public static final String PACKAGE_NAME = "org.cryse.unifystorage.explorer";
    public static final String SEPERATOR = ".";
    public static final int CONST_DOUBLE_CLICK_TO_EXIT_INTERVAL = 2000;

    public static class ClientIds {
        // OneDrive
        public static final String OneDriveClientId = "000000004C146A60";
        public static final String[] OneDriveScopes = new String[]{"onedrive.readwrite", "onedrive.appfolder", "wl.offline_access"};

        // Dropbox
        public static final String DropboxAppKey = "linm61rgwdxyyqt";
        public static final String DropboxClientIdentifier = "UnifyStoragePrototype";
    }

    public static class Argument {
        public static final String OperationName = "OperationName";
        public static final String OperationToken = "OperationToken";
        public static final String OperationTokenInt = "OperationTokenInt";
        public static final String SavePath = "SavePath";
        public static final String Opened = "Opened";
        public static final String ProviderId = "ProviderId";
    }

    public static class Action {
        public static final String ShowOperationDialog = PACKAGE_NAME + SEPERATOR + "ShowOperationDialog";
        public static final String OpenFile = PACKAGE_NAME + SEPERATOR + "OpenFile";
        public static final String CancelOperation = PACKAGE_NAME + SEPERATOR + "CancelOperation";
        public static final String NewOperation = PACKAGE_NAME + SEPERATOR + "NewOperation";
        public static final String PauseOperation = PACKAGE_NAME + SEPERATOR + "PauseOperation";
    }
}
