package com.android.composedemo.widgets.lrc;

/**
 * douzifly @Aug 10, 2013
 * github.com/douzifly
 * douzifly@gmail.com
 */

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * describe the lyric line
 *
 * @author douzifly
 */
public class LrcRow implements Comparable<LrcRow> {
    public final static String TAG = "LrcRow";

    /**
     * begin time of this lrc row
     */
    public long time;
    /**
     * content of this lrc
     */
    public String content;

    public String strTime;
    public LrcRow() {
    }

    public LrcRow(String strTime, long time, String content) {
        this.strTime = strTime;
        this.time = time;
        this.content = content;
        Log.d(TAG, "strTime:" + strTime + " time:" + time + " content:" + content);
    }

    /**
     * create LrcRows by standard Lrc Line , if not standard lrc line,
     * return false<br />
     * [00:00:20] balabalabalabala
     */
    public static List<LrcRow> createRows(String standardLrcLine) {
        try {
            if (!standardLrcLine.contains("[") || !standardLrcLine.contains("]")) {
                return null;
            }
            int lastIndexOfRightBracket = standardLrcLine.lastIndexOf("]");
            String content = standardLrcLine.substring(lastIndexOfRightBracket + 1);

            // times [mm:ss.SS][mm:ss.SS] -> *mm:ss.SS**mm:ss.SS*
            String times = standardLrcLine.substring(0, lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-");
            String[] arrTimes = times.split("-");
            List<LrcRow> listTimes = new ArrayList<LrcRow>();
            for (String temp : arrTimes) {
                if (temp.trim().isEmpty()) {
                    continue;
                }
                LrcRow lrcRow = new LrcRow(temp, timeConvert(temp), content);
                listTimes.add(lrcRow);
            }
            return listTimes;
        } catch (Exception e) {
            Log.e(TAG, "createRows exception:" + e.getMessage());
            return null;
        }
    }

    private static long timeConvert(String timeString) {
        timeString = timeString.replace('.', ':');
        String[] times = timeString.split(":");
        // mm:ss:SS
        int min = Integer.parseInt(times[0]);
        int sec = Integer.parseInt(times[1]);
        if (times.length < 3) {
            return min * 60 * 1000L + sec * 1000L;
        }

        return min * 60 * 1000L +
                sec * 1000L +
                Integer.parseInt(times[2]);
    }

    @Override
    public int compareTo(LrcRow another) {
        return (int) (this.time - another.time);
    }

}
