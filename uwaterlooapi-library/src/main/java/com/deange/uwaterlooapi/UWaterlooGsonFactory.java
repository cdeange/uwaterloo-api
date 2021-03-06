package com.deange.uwaterlooapi;

import com.google.gson.TypeAdapterFactory;
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory;

@GsonTypeAdapterFactory
public abstract class UWaterlooGsonFactory
    implements
    TypeAdapterFactory {

  public static TypeAdapterFactory create() {
    return new AutoValueGson_UWaterlooGsonFactory();
  }
}
