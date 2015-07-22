package com.deange.uwaterlooapi.model.foodservices;

import com.deange.uwaterlooapi.utils.Formatter;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class SpecialOperatingHours extends OperatingHours {

    @SerializedName("date")
    String mDate;

    /**
     * Y-m-d format date for the special case
     */
    public Date getDate() {
        return Formatter.parseDate(mDate, Formatter.YMD);
    }

    /**
     * Y-m-d format date for the special case as a string
     */
    public String getRawDate() {
        return mDate;
    }

}
