import com.kms.katalon.core.annotation.Keyword

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQueries

/**
 * Date and time keywords.
 *
 * Date keywords default to DEFAULT_FORMAT (24/Nov/2026); time keywords default to
 * DEFAULT_DATETIME_FORMAT (24/Nov/2026 18:45:10). Any input/output format can be passed.
 *
 * Format tokens (case-insensitive where it is unambiguous):
 *   dd            day                      yyyy / yy   year
 *   MM            month number             MMM / mmm   short month (Nov)
 *   MMMM / mmmm   full month (November)    EEE / EEEE  weekday (Thu / Thursday)
 *   HH / hh       hour (hh = 12-hour only when 'a' is present, otherwise 24-hour)
 *   mm            minutes                  ss          seconds
 *   SSS / sss     milliseconds             a           AM/PM
 *   z             time zone (IST)          Z / XXX     offset (+0530 / +05:30) 
 */
class DateUtils {

    static final String DEFAULT_FORMAT = 'dd/MMM/yyyy'
    static final String DEFAULT_DATETIME_FORMAT = 'dd/MMM/yyyy HH:mm:ss'

    // ---------- today / now ----------

    /** Today's date in outputFormat. */
    @Keyword
    static String todaysDate(String outputFormat = DEFAULT_FORMAT) {
        return format(now(), outputFormat)
    }

    /** Current date and time in outputFormat. */
    @Keyword
    static String currentDateTime(String outputFormat = DEFAULT_DATETIME_FORMAT) {
        return format(now(), outputFormat)
    }

    // ---------- days ----------

    /** The given date plus a number of days. */
    @Keyword
    static String plusDays(String date, int days,
                           String inputFormat = DEFAULT_FORMAT, String outputFormat = DEFAULT_FORMAT) {
        return format(parse(date, inputFormat).plusDays(days), outputFormat)
    }

    /** The given date minus a number of days. */
    @Keyword
    static String minusDays(String date, int days,
                            String inputFormat = DEFAULT_FORMAT, String outputFormat = DEFAULT_FORMAT) {
        return format(parse(date, inputFormat).minusDays(days), outputFormat)
    }

    /** Today plus a number of days. */
    @Keyword
    static String todayPlusDays(int days, String outputFormat = DEFAULT_FORMAT) {
        return format(now().plusDays(days), outputFormat)
    }

    /** Today minus a number of days. */
    @Keyword
    static String todayMinusDays(int days, String outputFormat = DEFAULT_FORMAT) {
        return format(now().minusDays(days), outputFormat)
    }

    // ---------- any unit ----------

    /**
     * The given date/time plus an amount of a unit.
     * unit: years, months, weeks, days, hours, minutes, seconds, millis (singular or plural).
     */
    @Keyword
    static String plusTime(String dateTime, long amount, String unit,
                           String inputFormat = DEFAULT_DATETIME_FORMAT, String outputFormat = DEFAULT_DATETIME_FORMAT) {
        return format(parse(dateTime, inputFormat).plus(amount, toUnit(unit)), outputFormat)
    }

    /** The given date/time minus an amount of a unit (see plusTime for units). */
    @Keyword
    static String minusTime(String dateTime, long amount, String unit,
                            String inputFormat = DEFAULT_DATETIME_FORMAT, String outputFormat = DEFAULT_DATETIME_FORMAT) {
        return format(parse(dateTime, inputFormat).minus(amount, toUnit(unit)), outputFormat)
    }

    /** Now plus an amount of a unit (see plusTime for units). */
    @Keyword
    static String nowPlusTime(long amount, String unit, String outputFormat = DEFAULT_DATETIME_FORMAT) {
        return format(now().plus(amount, toUnit(unit)), outputFormat)
    }

    /** Now minus an amount of a unit (see plusTime for units). */
    @Keyword
    static String nowMinusTime(long amount, String unit, String outputFormat = DEFAULT_DATETIME_FORMAT) {
        return format(now().minus(amount, toUnit(unit)), outputFormat)
    }

    // ---------- conversion ----------

    /** Converts a date/time string from inputFormat to outputFormat. */
    @Keyword
    static String convertFormat(String dateTime, String inputFormat, String outputFormat = DEFAULT_FORMAT) {
        return format(parse(dateTime, inputFormat), outputFormat)
    }

    // ---------- helpers ----------

    private static ZonedDateTime now() {
        return ZonedDateTime.now()
    }

    /** Parses date-only, date-time or zoned strings; missing time is midnight, missing zone is local. */
    private static ZonedDateTime parse(String value, String pattern) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException('date must not be blank')
        }
        TemporalAccessor parsed
        try {
            parsed = formatter(pattern).parse(value.trim())
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date '${value}' does not match format '${pattern}'", e)
        }

        LocalDate date = parsed.query(TemporalQueries.localDate())
        if (date == null) {
            throw new IllegalArgumentException("Format '${pattern}' must contain a full date (day, month and year)")
        }
        LocalTime time = parsed.query(TemporalQueries.localTime()) ?: LocalTime.MIDNIGHT
        ZoneId zone = parsed.query(TemporalQueries.zone()) ?: ZoneId.systemDefault()
        return ZonedDateTime.of(date, time, zone)
    }

    private static String format(ZonedDateTime dateTime, String pattern) {
        return dateTime.format(formatter(pattern))
    }

    private static DateTimeFormatter formatter(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException('format must not be blank')
        }
        // English month/day names so 'MMM' gives Nov regardless of the machine's locale.
        return DateTimeFormatter.ofPattern(normalize(pattern), Locale.ENGLISH)
    }

    /**
     * Accepts common spellings: mmm/mmmm as month names, sss as milliseconds,
     * YYYY as calendar year, hh as 24-hour when there is no AM/PM marker.
     */
    static String normalize(String pattern) {
        String result = pattern
                .replaceAll(/m{3,}/) { String m -> 'M' * m.length() }
                .replaceAll(/(?<!s)sss(?!s)/, 'SSS')
                .replaceAll(/Y+/) { String y -> 'y' * y.length() }
        if (!result.contains('a')) {
            result = result.replaceAll(/(?<!h)hh(?!h)/, 'HH')
        }
        return result
    }

    private static ChronoUnit toUnit(String unit) {
        String key = unit?.trim()?.toLowerCase()
        switch (key) {
            case ['year', 'years', 'y']: return ChronoUnit.YEARS
            case ['month', 'months']: return ChronoUnit.MONTHS
            case ['week', 'weeks', 'w']: return ChronoUnit.WEEKS
            case ['day', 'days', 'd']: return ChronoUnit.DAYS
            case ['hour', 'hours', 'h']: return ChronoUnit.HOURS
            case ['minute', 'minutes', 'min', 'mins']: return ChronoUnit.MINUTES
            case ['second', 'seconds', 'sec', 'secs', 's']: return ChronoUnit.SECONDS
            case ['milli', 'millis', 'millisecond', 'milliseconds', 'ms']: return ChronoUnit.MILLIS
            default:
                throw new IllegalArgumentException("Unknown unit '${unit}'. Use years, months, weeks, days, hours, minutes, seconds or millis")
        }
    }
}
