package cn.wizzer.framework.log4j;


import java.util.*;

/**
 * MyRollingCalendar is a helper class to DailyMaxRollingFileAppender. Given a periodicity
 * type and the current time, it computes the past maxBackupIndex date.
 */
class MyRollingCalendar extends GregorianCalendar
{
    private static final long serialVersionUID = 1L;
    public static final int TOP_OF_TROUBLE = -1;
    public static final int TOP_OF_MINUTE = 0;
    public static final int TOP_OF_HOUR = 1;
    public static final int HALF_DAY = 2;
    public static final int TOP_OF_DAY = 3;
    public static final int TOP_OF_WEEK = 4;
    public static final int TOP_OF_MONTH = 5;
    int type = TOP_OF_TROUBLE;
    MyRollingCalendar()
    {
        super();
    }
    MyRollingCalendar(TimeZone tz, Locale locale)
    {
        super(tz, locale);
    }
    public long getPastCheckMillis(Date now, int maxBackupIndex)
    {
        return getPastDate(now, maxBackupIndex).getTime();
    }
    public Date getPastDate(Date now, int maxBackupIndex)
    {
        this.setTime(now);
        switch (type)
        {
            case TOP_OF_MINUTE:
                this.set(Calendar.MINUTE, this.get(Calendar.MINUTE) - maxBackupIndex);
                break;
            case TOP_OF_HOUR:
                this.set(Calendar.HOUR_OF_DAY, this.get(Calendar.HOUR_OF_DAY) - maxBackupIndex);
                break;
            case HALF_DAY:
                int hour = get(Calendar.HOUR_OF_DAY);
                if (hour < 12)
                {
                    this.set(Calendar.HOUR_OF_DAY, 12);
                }
                else
                {
                    this.set(Calendar.HOUR_OF_DAY, 0);
                }
                this.set(Calendar.DAY_OF_MONTH, this.get(Calendar.DAY_OF_MONTH) - maxBackupIndex);
                break;
            case TOP_OF_DAY:
                this.set(Calendar.DATE, this.get(Calendar.DATE) - maxBackupIndex);
                break;
            case TOP_OF_WEEK:
                this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
                this.set(Calendar.WEEK_OF_YEAR, this.get(Calendar.WEEK_OF_YEAR) - maxBackupIndex);
                break;
            case TOP_OF_MONTH:
                this.set(Calendar.MONTH, this.get(Calendar.MONTH) - maxBackupIndex);
                break;
            default:
                throw new IllegalStateException("Unknown periodicity type.");
        }
        return this.getTime();
    }
    public long getNextCheckMillis(Date now)
    {
        return getNextCheckDate(now).getTime();
    }
    public Date getNextCheckDate(Date now)
    {
        this.setTime(now);
        switch (type)
        {
            case TOP_OF_MINUTE:
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.MINUTE, 1);
                break;
            case TOP_OF_HOUR:
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.HOUR_OF_DAY, 1);
                break;
            case HALF_DAY:
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                int hour = get(Calendar.HOUR_OF_DAY);
                if (hour < 12)
                {
                    this.set(Calendar.HOUR_OF_DAY, 12);
                }
                else
                {
                    this.set(Calendar.HOUR_OF_DAY, 0);
                    this.add(Calendar.DAY_OF_MONTH, 1);
                }
                break;
            case TOP_OF_DAY:
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.DATE, 1);
                break;
            case TOP_OF_WEEK:
                this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case TOP_OF_MONTH:
                this.set(Calendar.DATE, 1);
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.MONTH, 1);
                break;
            default:
                throw new IllegalStateException("Unknown periodicity type.");
        }
        return getTime();
    }
    void setType(int type)
    {
        this.type = type;
    }
}