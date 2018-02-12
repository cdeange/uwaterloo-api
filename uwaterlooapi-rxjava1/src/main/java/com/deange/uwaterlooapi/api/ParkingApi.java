package com.deange.uwaterlooapi.api;

import com.deange.uwaterlooapi.model.common.Responses;
import retrofit2.http.GET;
import rx.Observable;

public interface ParkingApi {

  /**
   * This method returns real-time parking counts in select parking lots across campus.
   */
  @GET("parking/watpark.json")
  Observable<Responses.Parking> getParkingInfo();

}