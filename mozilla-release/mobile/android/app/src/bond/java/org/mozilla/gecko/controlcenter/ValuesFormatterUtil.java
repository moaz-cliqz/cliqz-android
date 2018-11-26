package org.mozilla.gecko.controlcenter;

/**
 * Copyright © Cliqz 2018
 *
 * Utility class to convert the dashboard values into appropriate units
 */
public class ValuesFormatterUtil {

    /**
     * @param value time in milliseconds
     * @return Properly formatted time
     */
    static MeasurementWrapper formatTime(int value) {
        final int oneHour = 60 * 60;
        final int oneDay = 24 * 60 * 60;
        final int days;
        final int hours;
        final int minutes;
        final int seconds;
        String dayString;
        String hourString;
        String minutesString;
        String secondsString;
        int timeInSeconds = (int) Math.ceil(value/1000);
        if (timeInSeconds < oneHour) {
            minutes = timeInSeconds / 60;
            if (minutes > 0) {
                seconds = timeInSeconds % (minutes * 60);
            } else {
                seconds = timeInSeconds;
            }
            minutesString = Integer.toString(minutes);
            secondsString = Integer.toString(seconds);
            //append 0 if single digit
            if (minutes < 10) {
                minutesString = "0" + minutesString;
            }
            if (seconds < 10) {
                secondsString = "0" + secondsString;
            }
            return new MeasurementWrapper(minutesString + ":" + secondsString, "MIN");
        }
        if (timeInSeconds < oneDay) {
            hours = timeInSeconds / (60 * 60);
            minutes = (timeInSeconds % (hours * 60 * 60)) / 60;
            hourString = Integer.toString(hours);
            minutesString = Integer.toString(minutes);
            //append 0 if single digit
            if (hours < 10) {
                hourString = "0" + hourString;
            }
            if (minutes < 10) {
                minutesString = "0" + minutesString;
            }
            return new MeasurementWrapper(hourString + ":" + minutesString, "HRS");
        }
        days = timeInSeconds / (24 * 60 * 60);
        dayString = Integer.toString(days);
        if (days < 10) {
            dayString = "0" + dayString;
        }
        return new MeasurementWrapper(dayString, days == 1 ? "DAY" : "DAYS");
    }

    static MeasurementWrapper formatBlockCount(int totalCount) {
        if (totalCount < 100000) {
            return new MeasurementWrapper(Integer.toString(totalCount), "");
        } else {
            return new MeasurementWrapper("100k+", "");
        }
    }

    static MeasurementWrapper formatBytesCount(int bytes) {
        final int kiloBytes = bytes / 1024;
        final int gigaBytes;
        final int megaBytes;
        String megaBytesString;
        String kiloBytesString;
        String gigaBytesString;
        if (kiloBytes < 1000) {
            kiloBytesString = Integer.toString(kiloBytes);
            //append zeros if single or double digit
            if (kiloBytes < 10) {
                kiloBytesString = "00" + kiloBytesString;
            } else if (kiloBytes < 100) {
                kiloBytesString = "0" + kiloBytesString;
            }
            return new MeasurementWrapper(kiloBytesString, "KB");
        }
        if (kiloBytes < 1000 * 1000) {
            megaBytes = kiloBytes / 1000;
            megaBytesString = Integer.toString(megaBytes);
            //append zeros if single or double digit
            if (megaBytes < 10) {
                megaBytesString = "00" + megaBytesString;
            } else if (megaBytes < 100) {
                megaBytesString = "0" + megaBytesString;
            }
            return new MeasurementWrapper(megaBytesString, "MB");
        }
        gigaBytes = kiloBytes / (1000 * 1000);
        gigaBytesString = Integer.toString(gigaBytes);
        //append zeros if single or double digit
        if (gigaBytes < 10) {
            gigaBytesString = "00" + gigaBytesString;
        } else if (gigaBytes < 100) {
            gigaBytesString = "0" + gigaBytesString;
        }
        return new MeasurementWrapper(gigaBytesString, "GB");
    }
}
