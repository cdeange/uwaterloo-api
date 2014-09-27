package com.deange.uwaterlooapi.sample.utils;

import com.google.gson.Gson;

public class GsonController {
	public static final String TAG = GsonController.class.getSimpleName();

	private static Gson sCache = null;
	private static final Object sLock = new Object();

	public static synchronized void createInstance() {
		if (sCache == null) {
			sCache = new Gson();
		}
	}

	public static Gson getInstance() {
		synchronized (sLock) {
			if (sCache == null) {
				createInstance();
			}
			return sCache;
		}
	}

}
