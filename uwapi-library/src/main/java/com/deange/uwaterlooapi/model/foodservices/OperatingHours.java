package com.deange.uwaterlooapi.model.foodservices;

import com.deange.uwaterlooapi.model.BaseModel;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Arrays;
import java.util.List;

@Parcel
public class OperatingHours extends BaseModel {

    public static final String SUNDAY = "sunday";
    public static final String MONDAY = "monday";
    public static final String TUESDAY = "tuesday";
    public static final String WEDNESDAY = "wednesday";
    public static final String THURSDAY = "thursday";
    public static final String FRIDAY = "friday";
    public static final String SATURDAY = "saturday";

    // Corresponds to Calendar.DAY_OF_WEEK values
    public static final List<String> WEEKDAYS = Arrays.asList(
            null, SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    );

    public static final String TIME_FORMAT = "HH:mm";

    @SerializedName("opening_hour")
    String mOpeningHour;

    @SerializedName("closing_hour")
    String mClosingHour;

    @SerializedName("is_closed")
    boolean mClosedAllDay;

    /**
     * Locations opening time {@link #TIME_FORMAT (H:i format)}
     */
    public String getOpeningHour() {
        return mOpeningHour;
    }

    /**
     * Locations closing time {@link #TIME_FORMAT (H:i format)}
     */
    public String getClosingHour() {
        return mClosingHour;
    }

    /**
     * If the location is closed on that day
     */
    public boolean isClosedAllDay() {
        return mClosedAllDay;
    }
}
