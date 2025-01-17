package com.applikeysolutions.cosmocalendar.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.applikeysolutions.cosmocalendar.settings.SettingsManager;
import com.applikeysolutions.cosmocalendar.model.Day;
import com.applikeysolutions.cosmocalendar.model.DayOfWeek;
import com.applikeysolutions.cosmocalendar.model.Month;
import com.applikeysolutions.cosmocalendar.selection.selectionbar.SelectionBarContentItem;
import com.applikeysolutions.cosmocalendar.selection.selectionbar.SelectionBarItem;
import com.applikeysolutions.cosmocalendar.selection.selectionbar.SelectionBarTitleItem;
import com.applikeysolutions.cosmocalendar.settings.lists.DisabledDaysCriteria;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CalendarUtils {

    public static Month createMonth(Date date, SettingsManager settingsManager) {
        final List<Day> days = new ArrayList<>();

        final Calendar firstDisplayedDayCalendar = Calendar.getInstance();
        final Calendar firstDayOfMonthCalendar = Calendar.getInstance();

        //First day that belongs to month
        final Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(date);
        firstDayOfMonthCalendar.setTime(firstDayOfMonth);
        firstDayOfMonthCalendar.get(Calendar.MONTH);
        int targetMonth = firstDayOfMonthCalendar.get(Calendar.MONTH);

        //First displayed day, can belong to previous month
        final Date firstDisplayedDay = DateUtils.getFirstDayOfWeek(firstDayOfMonth, settingsManager.getFirstDayOfWeek());
        firstDisplayedDayCalendar.setTime(firstDisplayedDay);

        final Calendar end = Calendar.getInstance();
        end.setTime(DateUtils.getLastDayOfWeek(DateUtils.getLastDayOfMonth(date)));

        //Create week day titles
        if (settingsManager.isShowDaysOfWeek()) {
            days.addAll(createDaysOfWeek(firstDisplayedDay));
        }


        List<String> monthHolidays = new ArrayList<>();

        Map<String, List<String>> holidaysMap = settingsManager.getHolidays();
        if (holidaysMap != null) {
            String dateKeyString = generateDayKeyForMonth(firstDayOfMonthCalendar);
            if (holidaysMap.containsKey(dateKeyString)) {
                List<String> holidayNames = holidaysMap.get(dateKeyString);
                if (holidayNames != null) {
                    monthHolidays.addAll(holidayNames);
                }
            }
        }

        Map<String, Integer> determinatorMap = settingsManager.getDeterminators();

        //Create first day of month
        days.add(createDay(firstDisplayedDayCalendar, settingsManager, targetMonth,determinatorMap));

        //Create other days in month
        do {
            DateUtils.addDay(firstDisplayedDayCalendar);
            days.add(createDay(firstDisplayedDayCalendar, settingsManager, targetMonth,determinatorMap));
        } while (!DateUtils.isSameDayOfMonth(firstDisplayedDayCalendar, end)
                || !DateUtils.isSameMonth(firstDisplayedDayCalendar, end));

        return new Month(createDay(firstDayOfMonthCalendar, settingsManager, targetMonth,determinatorMap), days, monthHolidays);
    }

    private static String generateDayKey(Calendar calendar) {
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        String day = String.format("%02d",  calendar.get(Calendar.DAY_OF_MONTH));
        return calendar.get(Calendar.YEAR) + "-" + month + "-" + day;
    }

    private static String generateDayKeyForMonth(Calendar calendar) {
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        return calendar.get(Calendar.YEAR) + "-" + month;
    }

    private static Day createDay(Calendar calendar, SettingsManager settingsManager, int targetMonth, Map<String,Integer> determinatorMap) {
        Day day = new Day(calendar);
        day.setBelongToMonth(calendar.get(Calendar.MONTH) == targetMonth);

        String key = generateDayKey(calendar);
        if (determinatorMap!= null && determinatorMap.containsKey(key)){
            day.setDeterminate(true);
            day.setDeterminatorColor(determinatorMap.get(key));
        }

        HashMap<String, String> holidayPins = settingsManager.getHolidaysPins();
        if (holidayPins != null && holidayPins.containsKey(key)) {
            day.setIsHoliday(true);
        }

        CalendarUtils.setDay(day, settingsManager);
        return day;
    }

    private static List<DayOfWeek> createDaysOfWeek(Date firstDisplayedDay) {
        final List<DayOfWeek> daysOfTheWeek = new ArrayList<>();

        final Calendar calendar = DateUtils.getCalendar(firstDisplayedDay);
        final int startDayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK);
        do {
            daysOfTheWeek.add(new DayOfWeek(calendar.getTime()));
            DateUtils.addDay(calendar);
        } while (calendar.get(Calendar.DAY_OF_WEEK) != startDayOfTheWeek);
        return daysOfTheWeek;
    }

    public static List<String> createWeekDayTitles(int firstDayOfWeek) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DAY_NAME_FORMAT, Locale.getDefault());
        final List<String> titles = new ArrayList<>();

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        do {
            titles.add(sdf.format(calendar.getTime()));
            DateUtils.addDay(calendar);
        } while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek);
        return titles;
    }

    public static List<Month> createInitialMonths(SettingsManager settingsManager, boolean hasLimit) {
        return hasLimit ? createInitialMonthsWithLimit(settingsManager) : createInitialMonthsDefault(settingsManager);
    }

    private static List<Month> createInitialMonthsDefault(SettingsManager settingsManager) {
        final List<Month> months = new ArrayList<>();

        final Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < SettingsManager.DEFAULT_MONTH_COUNT / 2; i++) {
            calendar.add(Calendar.MONTH, -1);
        }

        settingsManager.setInitialPosition(SettingsManager.DEFAULT_MONTH_COUNT / 2);

        for (int i = 0; i < SettingsManager.DEFAULT_MONTH_COUNT; i++) {
            months.add(createMonth(calendar.getTime(), settingsManager));
            DateUtils.addMonth(calendar);
        }
        return months;
    }


    private static List<Month> createInitialMonthsWithLimit(SettingsManager settingsManager) {
        final List<Month> months = new ArrayList<>();

        final Calendar calendar = Calendar.getInstance();

        int previousMonthCount;

        if (settingsManager.getVisibleMaxDate() == null) {
            return new ArrayList<>();
        }

        if (settingsManager.getVisibleMinDate() != null) {

            previousMonthCount = monthsBetween(settingsManager.getVisibleMinDate(), calendar);
            settingsManager.setInitialPosition(previousMonthCount);

            while (calendar.compareTo(settingsManager.getVisibleMinDate()) >= 0) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            }
        }

        do {
            months.add(createMonth(calendar.getTime(), settingsManager));
            DateUtils.addMonth(calendar);
        } while (calendar.compareTo(settingsManager.getVisibleMaxDate()) <= 0
                || calendar.get(Calendar.MONTH) == settingsManager.getVisibleMaxDate().get(Calendar.MONTH));

        return months;
    }

    public static int monthsBetween(Calendar startDate, Calendar endDate) {
        int diffYear = endDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH);
        return diffMonth;
    }

    /**
     * Returns selected Days grouped by month/year
     *
     * @param selectedDays
     * @return
     */
    public static List<SelectionBarItem> getSelectedDayListForMultipleMode(List<Day> selectedDays) {
        List<SelectionBarItem> result = new ArrayList<>();

        Calendar tempCalendar = Calendar.getInstance();
        int tempYear = -1;
        int tempMonth = -1;
        for (Day day : selectedDays) {
            tempCalendar.setTime(day.getCalendar().getTime());
            if (tempCalendar.get(Calendar.YEAR) != tempYear || tempCalendar.get(Calendar.MONTH) != tempMonth) {
                result.add(new SelectionBarTitleItem(getYearNameTitle(day)));
                tempYear = tempCalendar.get(Calendar.YEAR);
                tempMonth = tempCalendar.get(Calendar.MONTH);
            }
            result.add(new SelectionBarContentItem(day));
        }
        return result;
    }

    public static String getYearNameTitle(Day day) {
        return new SimpleDateFormat("MMM''yy").format(day.getCalendar().getTime());
    }

    /**
     * Returns width of circle
     *
     * @return
     */
    public static int getCircleWidth(Context context) {
        return getDisplayWidth(context) / Constants.DAYS_IN_WEEK;
    }

    public static int getCircleHeight(Context context) {
        return getDisplayWidth(context) / (Constants.DAYS_IN_WEEK + 2);
    }

    public static int getDisplayWidth(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    public static int dipToPx(Context context, float dipValue) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float scale = displayMetrics.density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * Sets variables(isWeekend, isDisabled, isFromConnectedCalendar) to day
     */
    public static void setDay(Day day, SettingsManager settingsManager) {
        if (settingsManager.getWeekendDays() != null) {
            day.setWeekend(settingsManager.getWeekendDays().contains(day.getCalendar().get(Calendar.DAY_OF_WEEK)));
        }

        if (settingsManager.getEnableMinDate() != null) {
            day.setDisabled(isDayDisabledByMinDate(day, settingsManager.getEnableMinDate()));
        }
        if (settingsManager.getEnableMaxDate() != null) {
            if (!day.isDisabled()) {
                day.setDisabled(isDayDisabledByMaxDate(day, settingsManager.getEnableMaxDate()));
            }
        }

        if (settingsManager.getDisabledDays() != null) {
            if (!day.isDisabled()) {
                day.setDisabled(isDayInSet(day, settingsManager.getDisabledDays()));
            }
        }

        if (settingsManager.getDisabledDaysCriteria() != null) {
            if (!day.isDisabled()) {
                day.setDisabled(isDayDisabledByCriteria(day, settingsManager.getDisabledDaysCriteria()));
            }
        }

        if (settingsManager.getConnectedDaysManager().isAnyConnectedDays()) {
            settingsManager.getConnectedDaysManager().applySettingsToDay(day);
        }
    }

    public static boolean isDayInSet(Day day, Set<Long> daysInSet) {
        for (long disabledTime : daysInSet) {
            Calendar disabledDayCalendar = DateUtils.getCalendar(disabledTime);
            if (day.getCalendar().get(Calendar.YEAR) == disabledDayCalendar.get(Calendar.YEAR)
                    && day.getCalendar().get(Calendar.DAY_OF_YEAR) == disabledDayCalendar.get(Calendar.DAY_OF_YEAR)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDayDisabledByMinDate(Day day, Calendar minDate) {
        return day.getCalendar().get(Calendar.YEAR) < minDate.get(Calendar.YEAR)
                || day.getCalendar().get(Calendar.YEAR) == minDate.get(Calendar.YEAR)
                && day.getCalendar().get(Calendar.DAY_OF_YEAR) < minDate.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isDayDisabledByMaxDate(Day day, Calendar maxDate) {
        return day.getCalendar().get(Calendar.YEAR) > maxDate.get(Calendar.YEAR)
                || day.getCalendar().get(Calendar.YEAR) == maxDate.get(Calendar.YEAR)
                && day.getCalendar().get(Calendar.DAY_OF_YEAR) > maxDate.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isDayDisabledByCriteria(Day day, DisabledDaysCriteria criteria) {
        int field = -1;
        switch (criteria.getCriteriaType()) {
            case DAYS_OF_MONTH:
                field = Calendar.DAY_OF_MONTH;
                break;

            case DAYS_OF_WEEK:
                field = Calendar.DAY_OF_WEEK;
                break;
        }

        for (int dayInt : criteria.getDays()) {
            if (dayInt == day.getCalendar().get(field)) {
                return true;
            }
        }
        return false;
    }

    public static int getIconHeight(Resources resources, int iconResId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, iconResId, options);
        return options.outHeight;
    }
}