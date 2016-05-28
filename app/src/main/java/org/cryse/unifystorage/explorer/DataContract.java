package org.cryse.unifystorage.explorer;

public class DataContract {
    public static final int CONST_DOUBLE_CLICK_TO_EXIT_INTERVAL = 2000;

    public static final String ARG_CREDENTIAL = "arguments_credential";
    public static final String ARG_STORAGE_PROVIDER_RECORD_ID = "arguments_storage_provider_record_id";
    public static final String ARG_LOCAL_PATH = "arguments_local_path";
    public static final String ARG_EXTRAS = "arguments_extras";

    public static final int CONST_EMPTY_STORAGE_PROVIDER_RECORD_ID = -111;
    public static final String CONST_ONEDRIVE_CLIENT_ID = "000000004C146A60";
    public static final String CONST_DROPBOX_APP_KEY = "linm61rgwdxyyqt";
    public static final String CONST_DROPBOX_CLIENT_IDENTIFIER = "UnifyStoragePrototype";
    public static final String[] CONST_ONEDRIVE_SCOPES = new String[]{"onedrive.readwrite", "onedrive.appfolder", "wl.offline_access"};

    public static final String DOWNLOAD_BROADCAST_START_IDENTIFIER = "unifystorage_download_broadcast_start_identifier";

    public static final String DOWNLOAD_BROADCAST_ERROR_IDENTIFIER = "unifystorage_download_broadcast_error_identifier";
    public static final String DOWNLOAD_BROADCAST_ERROR_MESSAGE = "unifystorage_download_broadcast_error_message";

    public static final String DOWNLOAD_BROADCAST_SUCCESS_IDENTIFIER = "unifystorage_download_broadcast_success_identifier";
    public static final String DOWNLOAD_BROADCAST_SUCCESS_OPEN = "unifystorage_download_broadcast_success_open";
    public static final String DOWNLOAD_BROADCAST_SUCCESS_PATH = "unifystorage_download_broadcast_file_path";

    public static final String DOWNLOAD_BROADCAST__PROGRESS_IDENTIFIER = "unifystorage_download_broadcast_progress_identifier";
    public static final String DOWNLOAD_BROADCAST_TOKEN = "unifystorage_download_broadcast_token";
    public static final String DOWNLOAD_BROADCAST_FILENAME = "unifystorage_download_broadcast_file_name";
    public static final String DOWNLOAD_BROADCAST_FILE_SIZE = "unifystorage_download_broadcast_file_size";
    public static final String DOWNLOAD_BROADCAST_READ_SIZE = "unifystorage_download_broadcast_read_size";
    public static final String DOWNLOAD_BROADCAST_PERCENTAGE = "unifystorage_download_broadcast_percentage";
}
