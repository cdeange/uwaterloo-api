package com.deange.uwaterlooapi.api;

import com.deange.uwaterlooapi.model.common.Response;

import retrofit.http.GET;

public interface PointsOfInterestApi {

    /**
     * This method returns list of ATMs across campus.
     */
    @GET("/poi/atms.json")
    Response.ATMs getATMs();

    /**
     * This method returns list of Greyhound bus stops across the city.
     */
    @GET("/poi/greyhound.json")
    Response.Greyhound getGreyhoundStops();

    /**
     * This method returns list of photospheres across campus.
     */
    @GET("/poi/photospheres.json")
    Response.Photospheres getPhotospheres();

    /**
     * This method returns list of emergency helplines across campus.
     */
    @GET("/poi/helplines.json")
    Response.Helplines getHelplines();

    /**
     * This method returns list of libraries across the city.
     */
    @GET("/poi/libraries.json")
    Response.Libraries getLibraries();

    /**
     * This method returns list of defibrillators across campus.
     */
    @GET("/poi/defibrillators.json")
    Response.Defibrillators getDefibrillators();

}
