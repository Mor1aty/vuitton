package com.moriaty.vuitton.constant;

import java.time.format.DateTimeFormatter;

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
    }

    public static class Date {

        private Date() {

        }

        public static final DateTimeFormatter FORMAT_ID = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");

        public static final DateTimeFormatter FORMAT_RECORD_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
}
