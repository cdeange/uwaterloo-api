package com.deange.uwaterlooapi.api;

import com.deange.uwaterlooapi.model.courses.CourseLocations;
import com.deange.uwaterlooapi.model.courses.PrerequisiteInfo;
import com.deange.uwaterlooapi.model.courses.PrerequisiteInfo.PrerequisiteGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.converter.GsonConverter;

public class ApiModelConverter extends GsonConverter {

    private ApiModelConverter(final Gson gson, final String encoding) {
        super(gson, encoding);
    }

    public static ApiModelConverter newInstance() {
        return newInstance("UTF-8");
    }

    public static ApiModelConverter newInstance(String encoding) {
        return new ApiModelConverter(getGson(), encoding);
    }

    public static Gson getGson() {

        return new GsonBuilder()
                .setVersion(ApiBuilder.VERSION)
                .registerTypeAdapter(CourseLocations.class, new CourseLocations.Converter())
                .registerTypeAdapter(PrerequisiteGroup.class, new PrerequisiteInfo.Converter())
                .create();

    }

}
