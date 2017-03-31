package com.wojciechkocik.usage.service;

import com.wojciechkocik.usage.dto.DailyUsage;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link TimeSpentCrossMidnightService} using java 8 features
 *
 * @author Wojciech Kocik
 * @since 24.03.2017
 */
@Service
public class TimeSpentCrossMidnightServiceImpl implements TimeSpentCrossMidnightService {

    private static final long SECONDS_IN_DAY = 60 * 60 * 24;
    //or alternatively to one above:
    private static final long ONE_DAY_SECONDS = TimeUnit.DAYS.toSeconds(1);


    @Override
    public List<DailyUsage> divideDaysWithTimeSessionCrossedMidnight(List<DailyUsage> dailyUsagesForCourse) {
        return dailyUsagesForCourse.stream()
                .filter(dailyUsage -> dailyUsage.getTime() > 0)
                .flatMap(dailyUsage -> {
                    if (dailyUsage.isMultiDaySession()) {
                        List<DailyUsage> dividedDailyUsages = generateSessionDays(dailyUsage);
                        return dividedDailyUsages.stream();
                    }
                    return Arrays.asList(dailyUsage).stream();
                }).collect(toList());
    }

    private static List<DailyUsage> generateSessionDays(DailyUsage dailyUsage) {
        List<DailyUsage> dividedDailyUsages = new ArrayList<>();

        ZonedDateTime timeDateStarted = dailyUsage.getDateTime();
        ZonedDateTime firstDayMidnight = getMidnightOfDate(timeDateStarted);

        long sessionTime = dailyUsage.getTime();
        long untilMidnight = timeDateStarted.until(getMidnightOfDate(timeDateStarted.plusDays(1)), ChronoUnit.SECONDS);

        long restEpoch = sessionTime - untilMidnight;

        long wholeDaysCount = restEpoch / ONE_DAY_SECONDS;

        long lastDaySessionDuration = restEpoch % ONE_DAY_SECONDS;

        DailyUsage dayOne = new DailyUsage(timeDateStarted, untilMidnight);

        //production code may need to verify, whether wholeDaysCount is < Integer.MAX_VALUE;
        List<DailyUsage> wholeDaysList = IntStream.rangeClosed(1, (int) wholeDaysCount)
                .mapToObj(dayNumber -> new DailyUsage(firstDayMidnight.plusDays(dayNumber), SECONDS_IN_DAY))
                .collect(toList());

        dividedDailyUsages.add(dayOne);
        dividedDailyUsages.addAll(wholeDaysList);

        if (lastDaySessionDuration > 0) {
            DailyUsage lastDay = new DailyUsage(firstDayMidnight.plusDays(wholeDaysCount + 1), lastDaySessionDuration);
            dividedDailyUsages.add(lastDay);
        }
        return dividedDailyUsages;
    }

    public static ZonedDateTime getMidnightOfDate(ZonedDateTime dateTime) {
        return dateTime
                .toLocalDate().atTime(LocalTime.MIDNIGHT).atZone(dateTime.getZone());
    }
}
