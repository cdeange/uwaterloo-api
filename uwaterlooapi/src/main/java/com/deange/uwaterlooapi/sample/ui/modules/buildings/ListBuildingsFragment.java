package com.deange.uwaterlooapi.sample.ui.modules.buildings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.deange.uwaterlooapi.api.UWaterlooApi;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.buildings.Building;
import com.deange.uwaterlooapi.model.common.Response;
import com.deange.uwaterlooapi.sample.R;
import com.deange.uwaterlooapi.sample.model.FragmentInfo;
import com.deange.uwaterlooapi.sample.ui.ModuleAdapter;
import com.deange.uwaterlooapi.sample.ui.modules.ApiFragment;
import com.deange.uwaterlooapi.sample.ui.modules.base.BaseListModuleFragment;

import java.util.List;

@ApiFragment(value = "/buildings/list", isBare = true)
public class ListBuildingsFragment extends BaseListModuleFragment<Response.Buildings, Building>
        implements AdapterView.OnItemClickListener {

    private List<Building> mResponse;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public Response.Buildings onLoadData(final UWaterlooApi api) {
        return api.Buildings.getBuildings();
    }

    @Override
    public void onBindData(final Metadata metadata, final List<Building> data) {
        mResponse = data;
        notifyDataSetChanged();
    }

    @Override
    public FragmentInfo getFragmentInfo(final Context context) {
        return null;
    }

    @Override
    public ModuleAdapter getAdapter() {
        return new BuildingsAdapter(getActivity());
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view,
                            final int position, final long id) {
        showFragment(new BuildingFragment(), true,
                BuildingFragment.newBundle(mResponse.get(position).getBuildingCode()));
    }

    private final class BuildingsAdapter extends ModuleAdapter {

        public BuildingsAdapter(final Context context) {
            super(context);
        }

        @Override
        public int getListItemLayoutId() {
            return R.layout.list_item_condensed;
        }

        @Override
        public void bindView(final Context context, final int position, final View view) {
            ((TextView) view).setText(getItem(position).getBuildingName());
        }

        @Override
        public int getCount() {
            return mResponse == null ? 0 : mResponse.size();
        }

        @Override
        public Building getItem(final int position) {
            return mResponse == null ? null : mResponse.get(position);
        }
    }
}
