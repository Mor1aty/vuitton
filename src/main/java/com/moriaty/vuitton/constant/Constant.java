package com.moriaty.vuitton.constant;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * <p>
 * 常量
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/23 下午1:06
 */
public class Constant {

    private Constant() {

    }

    public static class Regex {

        private Regex() {

        }

        public static final String POSITIVE_INTEGER = "^\\d*[1-9]\\d*$";
        public static final String NATURE_NUMBER = "^\\d*[0-9]\\d*$";
    }

    public static class Date {

        private Date() {

        }

        public static final DateTimeFormatter FORMAT_ID = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");

        public static final DateTimeFormatter FORMAT_RECORD_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public static class Network {

        private Network() {

        }

        public static final int CONNECT_TIMEOUT = 60 * 1000;

        public static final Map<String, String> CHROME_HEADERS = Map.of(
                "user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"
        );
    }

    public static class Novel {

        private Novel() {

        }

        public static final String DOWNLOADING_STORAGE_KEY = "novelDownloadingList";

        public static final int DOWNLOAD_ACTION_ASK = 1;

        public static final int DOWNLOAD_ACTION_DO = 2;

        public static final String CHECKING_STORAGE_KEY = "novelChapterCheckingList";

        public static final int CHECK_ACTION_ASK = 1;

        public static final int CHECK_ACTION_RECOVER = 2;

        public static final int CHECK_ACTION_FILL_UP = 3;

        public static final int CHECK_ACTION_RESET_SEQ = 4;

        public static final int CHAPTER_ORDER_DESC = 2;

        public static final String FILL_UP_STORAGE_KEY = "novelFillUpList";
    }

    public static class Setting {

        private Setting() {

        }

        public static final String NOVEL_CONTENT_FONT_SIZE = "novel_content_font_size";

        public static final String DEFAULT_NOVEL_IMG_URL = "default_novel_img_url";
    }
}
