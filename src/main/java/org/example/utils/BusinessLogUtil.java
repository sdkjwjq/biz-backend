package org.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统一业务日志：保持一行一个动作，方便排查时按 action、userId、taskId、perfId 检索。
 */
public final class BusinessLogUtil {
    private static final Logger LOG = LoggerFactory.getLogger("BUSINESS");
    private static final int MAX_VALUE_LENGTH = 160;

    private BusinessLogUtil() {
    }

    public static void info(String action, Object... kvPairs) {
        LOG.info(format(action, kvPairs));
    }

    public static void warn(String action, Object... kvPairs) {
        LOG.warn(format(action, kvPairs));
    }

    private static String format(String action, Object... kvPairs) {
        StringBuilder builder = new StringBuilder("[业务日志] action=");
        builder.append(clean(action));
        if (kvPairs == null) {
            return builder.toString();
        }
        for (int i = 0; i + 1 < kvPairs.length; i += 2) {
            builder.append(' ')
                    .append(clean(kvPairs[i]))
                    .append('=')
                    .append(clean(kvPairs[i + 1]));
        }
        return builder.toString();
    }

    private static String clean(Object value) {
        if (value == null) {
            return "-";
        }
        String text = String.valueOf(value)
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .trim();
        if (text.length() > MAX_VALUE_LENGTH) {
            return text.substring(0, MAX_VALUE_LENGTH) + "...";
        }
        return text.isEmpty() ? "-" : text;
    }
}
