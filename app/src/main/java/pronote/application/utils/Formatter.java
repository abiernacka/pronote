/**
 * Created by MiQUiDO on 03.09.14.
 *
 * Copyright 2014 MiQUiDO <http://www.miquido.com/>. All rights reserved.
 */
package pronote.application.utils;

import java.text.SimpleDateFormat;

/**
 * TODO Add class description...
 *
 * @author miquido
 */
public class Formatter {
    public static final String PATTERN_DATE = "dd-MM-yyyy";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(PATTERN_DATE);

    public static String date(long dateTime) {
        return DATE_FORMAT.format(dateTime);
    }
}
