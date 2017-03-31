package com.wojciechkocik.usage.dto;

import lombok.Data;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.wojciechkocik.usage.service.TimeSpentCrossMidnightServiceImpl.getMidnightOfDate;

/**
 * @author Wojciech Kocik
 * @since 24.03.2017
 */
@Data
public class DailyUsage {
    public DailyUsage(ZonedDateTime dateTime, long time) {
        this.dateTime = dateTime;
        this.time = time;
    }

    private ZonedDateTime dateTime;

    private long time;

    public void minusSpentSeconds(long seconds) {
        time -= seconds;
    }

    public void plusSpentSeconds(long seconds) {
        time += seconds;
    }

    public String getSimpleDate() {
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public boolean isMultiDaySession() {
        ZonedDateTime timeDateStarted = getDateTime();
        long sessionTime = getTime();
        long untilMidnight = timeDateStarted.until(getMidnightOfDate(timeDateStarted.plusDays(1)), ChronoUnit.SECONDS);

        return untilMidnight < sessionTime;
    }

}
