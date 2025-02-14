package org.egov.im.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DateParserUtil {

    private static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

    /**
     * Parses date formats to long for all users in responseMap
     *
     * @param responeMap LinkedHashMap got from user api response
     */
    public void parseResponse(Map<String, Object> responeMap, String dobFormat) {
        @SuppressWarnings("unchecked")
        List<LinkedHashMap<String, Object>> users = (List<LinkedHashMap<String, Object>>) responeMap.get("user");
        if (users != null) {
            users.forEach(map -> {
                String createdDate = (String) map.get("createdDate");
                if (createdDate != null) {
                    map.put("createdDate", dateToLong(createdDate, DEFAULT_DATE_FORMAT));
                }
                if (map.get("lastModifiedDate") != null) {
                    map.put("lastModifiedDate", dateToLong((String) map.get("lastModifiedDate"), DEFAULT_DATE_FORMAT));
                }
                if (map.get("dob") != null) {
                    map.put("dob", dateToLong((String) map.get("dob"), dobFormat));
                }
                if (map.get("pwdExpiryDate") != null) {
                    map.put("pwdExpiryDate", dateToLong((String) map.get("pwdExpiryDate"), DEFAULT_DATE_FORMAT));
                }
            });
        }
    }


    /**
     * Converts date to long
     *
     * @param date   date to be parsed
     * @param format Format of the date
     * @return Long value of date
     */
    private Long dateToLong(String date, String format) {
        if (date == null || format == null) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            return formatter.parse(date).getTime();
        } catch (ParseException e) {
            log.error("Error parsing date: {} with format: {}", date, format, e);
            return null;
        }
    }

}