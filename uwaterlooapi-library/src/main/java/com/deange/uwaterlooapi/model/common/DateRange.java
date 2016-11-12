package com.deange.uwaterlooapi.model.common;

import com.deange.uwaterlooapi.model.BaseModel;
import com.deange.uwaterlooapi.utils.Formatter;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class DateRange extends BaseModel {

    @SerializedName("week")
    int mWeek;

    @SerializedName("year")
    int mYear;

    @SerializedName("start")
    String mStart;

    @SerializedName("end")
    String mEnd;

    /**
     * Requested week
     */
    public int getWeek() {
        return mWeek;
    }

    /**
     * Requested year
     */
    public int getYear() {
        return mYear;
    }

    /**
     * Starting day of the menu (Y-m-d)
     */
    public Date getStart() {
        return Formatter.parseDate(mStart);
    }

    /**
     * Ending day of the menu (Y-m-d)
     */
    public Date getEnd() {
        return Formatter.parseDate(mEnd);
    }

    /**
     * Starting day of the menu (Y-m-d) as a string
     */
    public String getRawStartDate() {
        return mStart;
    }

    /**
     * Ending day of the menu (Y-m-d) as a string
     */
    public String getRawEndDate() {
        return mEnd;
    }
}