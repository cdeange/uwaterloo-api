package com.deange.uwaterlooapi.model.foodservices;

import com.deange.uwaterlooapi.model.BaseModel;
import com.deange.uwaterlooapi.model.common.DateRange;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class MenuInfo extends BaseModel {

    @SerializedName("date")
    private DateRange mDateRange;

    @SerializedName("outlets")
    private List<Outlet> mOutlets;

    public DateRange getDateRange() {
        return mDateRange;
    }

    public List<Outlet> getOutlets() {
        return Collections.unmodifiableList(mOutlets);
    }

}
