package com.deange.uwaterlooapi.sample.ui.modules.weather;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.deange.uwaterlooapi.annotations.ModuleFragment;
import com.deange.uwaterlooapi.api.UWaterlooApi;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.common.LegacyWeatherResponse;
import com.deange.uwaterlooapi.model.weather.LegacyWeatherReading;
import com.deange.uwaterlooapi.sample.R;
import com.deange.uwaterlooapi.sample.model.Photo;
import com.deange.uwaterlooapi.sample.model.PhotoDetails;
import com.deange.uwaterlooapi.sample.model.PhotoSize;
import com.deange.uwaterlooapi.sample.model.PhotoUrl;
import com.deange.uwaterlooapi.sample.ui.Colors;
import com.deange.uwaterlooapi.sample.ui.CoverPhotoPresenter;
import com.deange.uwaterlooapi.sample.ui.modules.ModuleType;
import com.deange.uwaterlooapi.sample.ui.modules.base.BaseModuleFragment;
import com.deange.uwaterlooapi.sample.ui.view.RangeView;
import com.deange.uwaterlooapi.sample.ui.view.SliceView;
import com.deange.uwaterlooapi.sample.ui.view.WaveView;
import com.deange.uwaterlooapi.sample.utils.DateUtils;
import com.deange.uwaterlooapi.sample.utils.IntentUtils;
import com.deange.uwaterlooapi.sample.utils.MathUtils;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;

@ModuleFragment(
        path = "/weather/current",
        layout = R.layout.module_weather
)
public class WeatherFragment
        extends BaseModuleFragment<LegacyWeatherResponse, LegacyWeatherReading>
        implements
        ViewTreeObserver.OnScrollChangedListener {

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static final ArgbEvaluator sEvaluator = new ArgbEvaluator();
    private static final float MAX_BEARING_TOLERANCE = 25f;
    private static final float MIN_WAVE_LEVEL = 0.1f;
    private static final float MAX_WAVE_LEVEL = 0.95f;
    private static final List<String> DIRECTIONS =
            Arrays.asList("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N");

    private final Set<View> mViewsSeen = new HashSet<>();
    private final Rect mRect = new Rect();
    private boolean mPostVisible;
    private boolean mPressed;

    @Bind(R.id.weather_scrollview) ScrollView mScrollView;
    @Bind(R.id.weather_slider) SliceView mSliceView;
    @Bind(R.id.weather_background) ImageView mBackground;

    @Bind(R.id.weather_temperature_bar) View mTemperatureBar;
    @Bind(R.id.weather_temperature) TextView mTemperatureView;
    @Bind(R.id.weather_temperature_range) RangeView mRangeView;
    @Bind(R.id.weather_min_temp) TextView mMinTempView;
    @Bind(R.id.weather_max_temp) TextView mMaxTempView;

    @Bind(R.id.weather_wind_direction_root) View mWindDirectionRoot;
    @Bind(R.id.weather_wind_direction) View mWindDirectionView;
    @Bind(R.id.weather_wind_speed) TextView mWindSpeedView;

    @Bind(R.id.weather_waveview) WaveView mWaveView;
    @Bind(R.id.weather_precipitation) TextView mPrecipitationView;

    @Bind(R.id.weather_pressure_trend_layout) ViewGroup mPressureLayout;
    @Bind(R.id.weather_pressure) TextView mPressureDescription;
    @Bind(R.id.weather_pressure_trend) TextView mPressureTrend;

    @Bind(R.id.weather_last_updated) TextView mLastUpdated;
    @Bind(R.id.weather_spacer) View mSpacer;
    @Bind(R.id.weather_author_attribution) TextView mAuthor;

    @BindDrawable(R.drawable.ic_arrow_up) Drawable mArrowDrawable;

    private LegacyWeatherReading mLastReading;
    private ValueAnimator mWindTextAnimation;
    private ValueAnimator mWindSpeedAnimation;
    private Photo mPhoto;
    private PhotoSize mPhotoSize;

    @Override
    protected View getContentView(final LayoutInflater inflater, final Bundle savedInstanceState) {
        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_weather, null);

        ButterKnife.bind(this, root);

        mScrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        mScrollView.setOnTouchListener(this);
        mScrollView.setVerticalScrollBarEnabled(false);

        mSpacer.setVisibility(View.GONE);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mBackground.setLayoutParams(
                new FrameLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels));

        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                resizeTemperatureView();
            }
        });

        mBackground.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    final View v,
                    final int left,
                    final int top,
                    final int right,
                    final int bottom,
                    final int oldLeft,
                    final int oldTop,
                    final int oldRight,
                    final int oldBottom) {
                if (mSpacer.getLayoutParams() != null) {
                    mSpacer.getLayoutParams().height =
                            v.getMeasuredHeight()
                                    - getHostActivity().getToolbar().getMeasuredHeight()
                                    - getStatusBarHeight();
                    mSpacer.requestLayout();
                }
            }
        });

        initWindView();
        initPrecipitationView();
        initPressureView();

        return root;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onScrollChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mWindTextAnimation != null) {
            mWindTextAnimation.cancel();
        }
        if (mWindSpeedAnimation != null) {
            mWindSpeedAnimation.cancel();
        }

        ButterKnife.unbind(this);
    }

    @Override
    public LegacyWeatherResponse onLoadData(final UWaterlooApi api) {
        return new LegacyWeatherResponse(api.LegacyWeather.getWeather());
    }

    @Override
    public void onBindData(final Metadata metadata, final LegacyWeatherReading data) {
        mLastReading = data;

        mTemperatureView.setText(formatTemperature(data.getTemperature(), 0));

        mRangeView.reset();
        mRangeView.setMin(data.getTemperature24hMin());
        mRangeView.setMax(data.getTemperature24hMax());
        mTemperatureBar.setBackgroundColor(mRangeView.getStartColour());

        mMinTempView.setText(formatTemperature(data.getTemperature24hMin(), 1));
        mMaxTempView.setText(formatTemperature(data.getTemperature24hMax(), 1));

        final Date updated = mLastReading.getObservationTime();
        final TimeZone tz = TimeZone.getDefault();
        mLastUpdated.setText(
                getString(R.string.weather_last_updated,
                        DateUtils.formatTime(getActivity(), updated),
                        tz.getDisplayName(tz.inDaylightTime(updated), TimeZone.SHORT))
        );

        final String cardinalDirection = data.getWindDirection();
        final int bearing = DIRECTIONS.indexOf(cardinalDirection) * 45;

        final float windSpeed = data.getWindSpeed();
        final String windSpeedString = (windSpeed == (int) windSpeed)
                ? String.valueOf((int) windSpeed)
                : String.valueOf(windSpeed);
        mWindSpeedView.setText(getString(R.string.weather_wind, windSpeedString, cardinalDirection));

        final float windSpeedMax = -(Math.min(windSpeed, 50f) / 100);
        final float windSpeedMin = windSpeedMax / 5f;
        mWindTextAnimation.setFloatValues(windSpeedMin, windSpeedMax);
        mWindTextAnimation.start();

        final float cappedTolerance = Math.min(data.getWindSpeed(), MAX_BEARING_TOLERANCE);
        final float minDegrees = bearing - cappedTolerance;
        final float maxDegrees = bearing + cappedTolerance;
        mWindSpeedAnimation.setFloatValues(minDegrees, maxDegrees);
        mWindSpeedAnimation.start();

        final float precipitation = mLastReading.getPrecipitation24Hr();
        final String precipitationFormatted;
        final int waveColor;
        if (precipitation == 0) {
            precipitationFormatted = getString(R.string.weather_precipitation_none);
            waveColor = Colors.GREY_500;

        } else {
            precipitationFormatted = getString(R.string.weather_precipitation, precipitation);
            waveColor = Colors.BLUE_500;
        }

        final float level = MathUtils.clamp(precipitation / 10f, MIN_WAVE_LEVEL, MAX_WAVE_LEVEL);
        mWaveView.setShowWave(true);
        mWaveView.setColor(waveColor);
        mWaveView.setWaterLevelRatio(level);
        mPrecipitationView.setText(precipitationFormatted);

        mPressureDescription.setText(getString(R.string.weather_pressure, mLastReading.getPressureKpa()));
        mPressureLayout.setPivotX(mPressureLayout.getWidth() / 2f);
        mPressureLayout.setPivotY(mPressureLayout.getHeight() / 2f);

        final String trend = mLastReading.getPressureTrend();
        mPressureTrend.setText(trend);
        if (LegacyWeatherReading.PRESSURE_FALLING.equals(trend)) {
            mPressureLayout.setRotation(0f);

        } else if (LegacyWeatherReading.PRESSURE_RISING.equals(trend)) {
            mPressureLayout.setRotation(180f);

        } else {
            mPressureLayout.setRotation(270f);
        }
    }

    @Override
    public String getContentType() {
        return ModuleType.WEATHER;
    }

    @Override
    protected void onRefreshRequested() {
        PhotoFetcher.fetchRandom(this);
    }

    @Override
    protected void onContentShown() {
        mViewsSeen.clear();
        onScrollChanged();
    }

    @OnClick(R.id.weather_author_source_link)
    public void onOpenFlickrPhotoClicked() {
        if (mPhoto != null) {
            final List<PhotoUrl> urls = mPhoto.getDetails().getUrls();
            IntentUtils.openBrowser(getContext(),
                    (!urls.isEmpty())
                            ? urls.get(0).getUrl()
                            : getPhotoSize().getUrl());
        }
    }

    private void loadPicture(final Photo photo) {
        mPhoto = photo;
        mPhotoSize = null;

        final PhotoSize size = getPhotoSize();
        Picasso.with(getActivity())
               .load(size.getSource())
               .centerCrop()
               .resize(mBackground.getMeasuredWidth(), mBackground.getMeasuredHeight())
               .into(mBackground);

        final PhotoDetails details = mPhoto.getDetails();
        final String text = getString(
                R.string.weather_author_attribution, details.getTitle(), details.getAuthor().getPreferredName());

        final SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, details.getTitle().length(), 0);
        mAuthor.setText(spannable);
    }

    private PhotoSize getPhotoSize() {
        if (mPhotoSize != null) {
            return mPhotoSize;
        }

        final int w = mBackground.getMeasuredWidth();
        final int h = mBackground.getMeasuredHeight();
        final boolean isHeightLarger = (h >= w);

        final List<PhotoSize> photos = new ArrayList<>(mPhoto.getSizes());
        Collections.sort(photos, new Comparator<PhotoSize>() {
            @Override
            public int compare(final PhotoSize lhs, final PhotoSize rhs) {
                return Double.compare(
                        isHeightLarger ? lhs.getHeight() : lhs.getWidth(),
                        isHeightLarger ? rhs.getHeight() : rhs.getWidth()
                );
            }
        });

        // Try to find the smallest image larger than the bounds of the ImageView
        for (final PhotoSize photoSize : photos) {
            if (isHeightLarger && photoSize.getHeight() >= h) {
                mPhotoSize = photoSize;
                break;
            } else if (!isHeightLarger && photoSize.getWidth() >= w) {
                mPhotoSize = photoSize;
                break;
            }
        }

        final String ignoreLabel = "Original";

        if (mPhotoSize == null || ignoreLabel.equals(mPhotoSize.getLabel())) {
            // They are all smaller than the screen, need to scale up
            // Pick the largest one that isn't original (usually it's too big)
            for (int i = photos.size() - 1; i >= 0; --i) {
                final PhotoSize photoSize = photos.get(i);
                if (!ignoreLabel.equals(photoSize.getLabel())) {
                    mPhotoSize = photoSize;
                    break;
                }
            }
        }

        return mPhotoSize;
    }

    private void initWindView() {
        mWindTextAnimation = ValueAnimator.ofFloat();
        mWindTextAnimation.setDuration(1000L);
        mWindTextAnimation.setInterpolator(new BounceInterpolator());
        mWindTextAnimation.setRepeatMode(ValueAnimator.REVERSE);
        mWindTextAnimation.setRepeatCount(ValueAnimator.INFINITE);
        mWindTextAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();
                if (mWindSpeedView != null) {
                    mWindSpeedView.getPaint().setTextSkewX(value);
                    mWindSpeedView.getPaint().setTextScaleX(1 + Math.abs(value));
                    mWindSpeedView.invalidate();
                }
            }
        });

        mWindSpeedAnimation = ValueAnimator.ofFloat();
        mWindSpeedAnimation.setDuration(1000L);
        mWindSpeedAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mWindSpeedAnimation.setRepeatMode(ValueAnimator.REVERSE);
        mWindSpeedAnimation.setRepeatCount(ValueAnimator.INFINITE);
        mWindSpeedAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();
                if (mWindDirectionView != null) {
                    mWindDirectionView.setPivotX(mWindDirectionView.getMeasuredWidth() / 2f);
                    mWindDirectionView.setPivotY(mWindDirectionView.getMeasuredHeight() / 2f);
                    mWindDirectionView.setRotation(value);
                }
            }
        });
    }

    private void initPrecipitationView() {
        // Horizontal animation, wave waves infinitely.
        final ObjectAnimator waveShiftAnimator = ObjectAnimator.ofFloat(
                mWaveView, "waveShiftRatio", 0f, 1f);
        waveShiftAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveShiftAnimator.setDuration(1000);
        waveShiftAnimator.setInterpolator(new LinearInterpolator());

        // Amplitude animation, wave grows big then grows small, repeatedly
        final ObjectAnimator amplitudeAnimator = ObjectAnimator.ofFloat(
                mWaveView, "amplitudeRatio", 0.02f, 0.04f);
        amplitudeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        amplitudeAnimator.setRepeatMode(ValueAnimator.REVERSE);
        amplitudeAnimator.setDuration(5000);
        amplitudeAnimator.setInterpolator(new LinearInterpolator());

        waveShiftAnimator.start();
        amplitudeAnimator.start();
    }

    private void initPressureView() {
        final int height = mArrowDrawable.getIntrinsicHeight();
        mPressureLayout.getLayoutParams().height = height;
        mPressureLayout.setPadding(0, 0, 0, -height);

        final ValueAnimator animator = ValueAnimator.ofFloat(0, -height);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final float translation = (float) animation.getAnimatedValue();
                if (mPressureLayout != null) {
                    mPressureLayout.getChildAt(0).setTranslationY(translation);
                    mPressureLayout.getChildAt(1).setTranslationY(translation);
                } else {
                    animator.cancel();
                }
            }
        });

        animator.start();

        mPressureLayout.setScaleY(-1);
    }

    // TODO use this when moving to the new UWaterloo WeatherApi
    // private String formatBearing(final float bearing) {
    //     final float normalizedBearing = (bearing % 360f) + 360f;
    //     return DIRECTIONS.get((int) Math.floor(((normalizedBearing + 22.5f) % 360) / 45));
    // }

    private String formatTemperature(final float temp, final int decimals) {
        return String.format("%." + decimals + "f˚", temp);
    }

    private void animateTemperatureRange() {
        if (mLastReading == null) return;

        final ValueAnimator animator = ValueAnimator.ofFloat(
                mLastReading.getTemperature24hMin(),
                mLastReading.getTemperature());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                sMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRangeView != null) {
                            mRangeView.setAmplitude(animation.getAnimatedFraction());
                            mRangeView.setValue((Float) animation.getAnimatedValue());

                            mTemperatureBar.setBackgroundColor((int) sEvaluator.evaluate(
                                    mRangeView.getNormalizedValue(),
                                    mRangeView.getStartColour(),
                                    mRangeView.getEndColour()
                            ));
                        }
                    }
                });
            }
        });

        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setStartDelay(500L);
        animator.setDuration(1000L);
        animator.start();
    }

    private void resizeTemperatureView() {
        // Places the temperature view programmatically
        final ViewGroup sliceParent = ((ViewGroup) mSliceView.getParent());
        final ViewGroup.MarginLayoutParams temperatureParams =
                (ViewGroup.MarginLayoutParams) mTemperatureView.getLayoutParams();
        final int height = mTemperatureView.getMeasuredHeight();

        final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final float offset =
                metrics.heightPixels
                        - temperatureParams.bottomMargin
                        - height
                        - temperatureParams.topMargin
                        - mSliceView.getPaddingTop()
                        - getNavBarHeight();

        sliceParent.setPadding(
                mSliceView.getPaddingLeft(),
                (int) offset,
                mSliceView.getPaddingRight(),
                mSliceView.getPaddingBottom()
        );
    }

    public int getStatusBarHeight() {
        final Resources res = getResources();
        final int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        return (resourceId > 0) ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    private int getNavBarHeight() {
        final Resources res = getResources();
        final int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        return (resourceId > 0) ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    @Override
    public void onScrollChanged() {
        // Scrollview has moved
        if (firstViewSinceRefresh(mRangeView)) {
            animateTemperatureRange();
        }

        final int scroll = (mScrollView.getHeight() + mScrollView.getScrollY());
        final int sliceBottom = mSliceView.getBottom();

        if (scroll >= sliceBottom) {
            if (mPressed) {
                mPostVisible = true;
            } else {
                postSetSpacerVisible();
            }
        } else {
            mPostVisible = false;
            mSpacer.setVisibility(View.GONE);
        }
    }

    // Note that this is NOT IDEMPOTENT, successive calls for the same view may return false
    private boolean firstViewSinceRefresh(final View v) {
        if (!mViewsSeen.contains(v) && v.getGlobalVisibleRect(mRect)) {
            mViewsSeen.add(v);
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        if (view == mScrollView) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPressed = true;
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mPressed = false;
                    if (mPostVisible) {
                        postSetSpacerVisible();
                    }
                    break;
            }

            return false;
        } else {
            return super.onTouch(view, motionEvent);
        }
    }

    private void postSetSpacerVisible() {
        mPostVisible = false;
        post(new Runnable() {
            @Override
            public void run() {
                if (mSpacer != null) {
                    mSpacer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private static class PhotoFetcher extends AsyncTask<String, Void, Photo> {

        private final WeakReference<WeatherFragment> mFragmentRef;

        public static void fetchRandom(final WeatherFragment fragment) {
            fetch(fragment, null);
        }

        public static void fetch(final WeatherFragment fragment, final String photoId) {
            new PhotoFetcher(fragment).execute(photoId);
        }

        private PhotoFetcher(final WeatherFragment fragment) {
            mFragmentRef = new WeakReference<>(fragment);
        }

        @Override
        protected Photo doInBackground(final String... params) {
            return CoverPhotoPresenter.getPhoto(params[0]);
        }

        @Override
        protected void onPostExecute(final Photo photo) {
            final WeatherFragment fragment = mFragmentRef.get();
            if (fragment != null) {
                fragment.loadPicture(photo);
            }
        }
    }
}
