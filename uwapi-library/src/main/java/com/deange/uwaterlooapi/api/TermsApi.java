package com.deange.uwaterlooapi.api;

import com.deange.uwaterlooapi.model.common.Response;

import retrofit.http.GET;
import retrofit.http.Path;

public interface TermsApi {

    /**
     * This method returns the current, previous and next term's id along with a list of terms
     * in the past year and the next year
     */
    @GET("/terms/list.json")
    Response.Terms getTermList();

    /**
     * This method returns a given term's exam schedule
     * @param termId Numeric representation of the term
     */
    @GET("/terms/{term}/examschedule.json")
    Response.TermExamSchedule getExamSchedule(@Path("term") int termId);

    /**
     * This method returns all class schedule for the given subject for a given term
     * @param termId Four digit term representation
     * @param subject Valid uWaterloo subject name, eg: MATH, CS, ENGL
     */
    @GET("/terms/{term}/{subject}/schedule.json")
    Response.CoursesSchedule getSchedule(@Path("term") int termId, @Path("subject") String subject);

    /**
     * This method returns the class schedule for the given course of a given term
     * @param termId Four digit term representation
     * @param subject Valid uWaterloo subject name, eg: MATH, CS, ENGL
     * @param catalog Course name, eg: 101, 108D, 412
     */
    @GET("/terms/{term}/{subject}/{catalog_number}/schedule.json")
    Response.CoursesSchedule getSchedule(@Path("term") int termId, @Path("subject") String subject, @Path("catalog_number") String catalog);

    /**
     * This method returns the schedule for employer information sessions of a given term
     * @param termId Four digit term representation
     */
    @GET("/terms/{term}/infosessions.json")
    Response.InfoSessions getInfoSessions(@Path("term") int termId);

}
