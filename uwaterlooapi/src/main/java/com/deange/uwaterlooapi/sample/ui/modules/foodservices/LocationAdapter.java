package com.deange.uwaterlooapi.sample.ui.modules.foodservices;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deange.uwaterlooapi.model.foodservices.Location;
import com.deange.uwaterlooapi.model.foodservices.OperatingHours;
import com.deange.uwaterlooapi.model.foodservices.SpecialOperatingHours;
import com.deange.uwaterlooapi.sample.R;
import com.deange.uwaterlooapi.sample.ui.ModuleAdapter;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.List;

public class LocationAdapter
        extends ModuleAdapter {

    private final List<Location> mLocations;

    public LocationAdapter(
            final Context context,
            final List<Location> locations,
            final ModuleListItemListener listener) {
        super(context, listener);
        mLocations = locations;
    }

    @Override
    public View newView(final Context context, final int position, final ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.list_item_foodservices_location, parent, false);
    }

    @Override
    public void bindView(final Context context, final int position, final View view) {

        final Location location = getItem(position);
        final TextView titleView = (TextView) view.findViewById(R.id.list_location_title);

        final Spannable wordtoSpan = new SpannableString(location.getName());
        int hyphen = location.getName().indexOf('-');
        if (hyphen == -1) {
            hyphen = wordtoSpan.length();
        }
        wordtoSpan.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, hyphen, 0);
        titleView.setText(wordtoSpan);

        final TextView timingView = (TextView) view.findViewById(R.id.list_location_timing_desc);

        if (!location.isOpenNow()) {
            timingView.setTextColor(context.getResources().getColor(R.color.foodservices_location_closed));
            timingView.setText(R.string.foodservices_location_closed_now);

        } else {
            final LocalTime closing = getClosingTime(location);
            final String mintuesFormat = (closing.getMinuteOfHour() == 0) ? "" : ":mm";
            timingView.setTextColor(context.getResources().getColor(R.color.foodservices_location_open));
            timingView.setText(context.getString(
                    R.string.foodservices_location_closes_at,
                    closing.toString("h" + mintuesFormat + " a")));
        }
    }

    @Override
    public int getCount() {
        return mLocations.size();
    }

    @Override
    public Location getItem(final int position) {
        return mLocations.get(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    final LocalTime getClosingTime(final Location location) {

        LocalTime closing = null;
        final LocalDate today = LocalDate.now();
        final DateTimeFormatter format = DateTimeFormat.forPattern(OperatingHours.TIME_FORMAT);

        // Check for special hours for this particular day
        for (final SpecialOperatingHours specialDay : location.getSpecialOperatingHoursRaw()) {
            final LocalDate day = LocalDate.fromDateFields(specialDay.getDate());
            final LocalTime openTime = LocalTime.parse(specialDay.getOpeningHour(), format);
            final LocalTime closeTime = LocalTime.parse(specialDay.getClosingHour(), format);

            if (day.isEqual(today)
                    || (day.plusDays(1).equals(today) && closeTime.isBefore(openTime))) {
                // Check for post-midnight hours as well
                closing = LocalTime.parse(specialDay.getClosingHour(), format);
            }
        }

        final int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        final OperatingHours hours = location.getHours(OperatingHours.WEEKDAYS.get(day));
        final LocalTime opening = LocalTime.parse(hours.getOpeningHour(), format);

        // Check the normal operating hours schedule (if necessary)
        if (closing == null) {
            if (opening.isAfter(DateTime.now().toLocalTime())) {
                // It is open but opens after now, we need to look at the hours for yesterday
                // since it closes after midnight from yesterday
                final int yesterday = (day == Calendar.SUNDAY) ? Calendar.SATURDAY : day - 1;
                final OperatingHours yesterdayHours =
                        location.getHours(OperatingHours.WEEKDAYS.get(yesterday));
                closing = LocalTime.parse(yesterdayHours.getClosingHour(), format);

            } else {
                closing = LocalTime.parse(hours.getClosingHour(), format);
            }
        }

        return closing;
    }

}
