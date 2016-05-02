package com.deange.uwaterlooapi.sample.ui.modules.resources;


import android.animation.LayoutTransition;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.deange.uwaterlooapi.annotations.ModuleFragment;
import com.deange.uwaterlooapi.api.UWaterlooApi;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.common.Response;
import com.deange.uwaterlooapi.model.resources.GooseNest;
import com.deange.uwaterlooapi.sample.R;
import com.deange.uwaterlooapi.sample.ui.modules.ModuleType;
import com.deange.uwaterlooapi.sample.ui.modules.base.BaseMapFragment;
import com.deange.uwaterlooapi.sample.utils.DateUtils;
import com.deange.uwaterlooapi.sample.utils.MapManager;
import com.deange.uwaterlooapi.sample.utils.Px;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

@ModuleFragment(
        path = "/resources/goosewatch",
        layout = R.layout.module_resources_goosewatch
)
public class GooseWatchFragment
        extends BaseMapFragment<Response.GooseWatch, GooseNest>
        implements
        GoogleMap.OnMarkerClickListener {

    public static final String TAG = GooseWatchFragment.class.getSimpleName();

    @Bind(R.id.goosewatch_empty_view) View mEmptyView;
    @Bind(R.id.goosewatch_nest_info) ViewGroup mInfoRoot;
    @Bind(android.R.id.text1) TextView mNestDetails;
    @Bind(android.R.id.text2) TextView mNestUpdated;

    private List<GooseNest> mResponse;

    @Override
    protected View getContentView(final LayoutInflater inflater, final ViewGroup parent) {
        final View view = inflater.inflate(R.layout.fragment_goosewatch, parent, false);
        ButterKnife.bind(this, view);

        mInfoRoot.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        return view;
    }

    @Override
    public Response.GooseWatch onLoadData(final UWaterlooApi api) {
        return api.Resources.getGeeseNests();
    }

    @Override
    public void onBindData(final Metadata metadata, final List<GooseNest> data) {
        mResponse = data;

        if (mMapView.getMap() != null) {
            showNests();
        }
    }

    @Override
    public String getContentType() {
        return ModuleType.GOOSEWATCH;
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_resources_goosewatch);
    }

    private void showNests() {
        if (mResponse == null || mResponse.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        } else {
            mEmptyView.setVisibility(View.GONE);
        }

        final GoogleMap map = mMapView.getMap();
        map.clear();

        for (final GooseNest nest : mResponse) {
            map.addMarker(
                    new MarkerOptions().position(getLatLng(nest))
                                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_alert))
                                       .title(nest.getLocationDescription())
                                       .snippet(getString(
                                               R.string.goosewatch_last_updated,
                                               DateUtils.formatDate(getContext(), nest.getUpdatedDate())))
            );
        }

        final LatLngBounds.Builder builder = LatLngBounds.builder();
        for (final GooseNest nest : mResponse) {
            builder.include(getLatLng(nest));
        }
        final LatLngBounds bounds = builder.build();
        final int padding = Px.fromDp(16);

        map.setIndoorEnabled(false);
        map.setOnMapClickListener(this);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        MapManager.setLocationEnabled(getActivity(), map);
    }

    private LatLng getLatLng(final GooseNest nest) {
        final float[] location = nest.getLocation();
        return new LatLng(location[0], location[1]);
    }

    @Override
    protected void onRefreshRequested() {
        hideNestDetails();
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        hideNestDetails();
    }

    private void hideNestDetails() {
        if (mInfoRoot.getVisibility() == View.VISIBLE) {
            final Animation animOut = AnimationUtils.loadAnimation(getContext(), R.anim.top_out);
            animOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {
                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    if (mInfoRoot != null) {
                        mInfoRoot.setVisibility(View.GONE);
                    }
                }
            });
            mInfoRoot.startAnimation(animOut);
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        marker.hideInfoWindow();
        final LatLng position = marker.getPosition();

        for (final GooseNest nest : mResponse) {
            final float[] points = nest.getLocation();
            if (position.latitude == points[0] && position.longitude == points[1]) {
                onNestDetailsRequested(nest);
                return true;
            }
        }
        return false;
    }

    private void onNestDetailsRequested(final GooseNest nest) {
        mMapView.getMap().animateCamera(CameraUpdateFactory.newLatLng(getLatLng(nest)));

        if (mInfoRoot.getVisibility() == View.GONE) {
            final Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.top_in);
            mInfoRoot.startAnimation(bottomUp);
            mInfoRoot.setVisibility(View.VISIBLE);
        }

        String title = nest.getLocationDescription();
        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.goosewatch_no_title);
        }
        final String date = getString(
                R.string.goosewatch_last_updated,
                DateUtils.getTimeDifference(getResources(), nest.getUpdatedDate().getTime()).toLowerCase());

        mNestDetails.setText(title);
        mNestUpdated.setText(date);
    }

    @Override
    public int getMapViewId() {
        return R.id.goosewatch_map;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (mResponse != null) {
            showNests();
        }
    }
}
