package com.applikeysolutions.cosmocalendar.adapter.viewholder;

import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applikeysolutions.cosmocalendar.settings.SettingsManager;
import com.applikeysolutions.customizablecalendar.R;
import com.applikeysolutions.cosmocalendar.adapter.DaysAdapter;
import com.applikeysolutions.cosmocalendar.model.Month;
import com.applikeysolutions.cosmocalendar.view.MonthView;

import java.util.List;

public class MonthHolder extends RecyclerView.ViewHolder {

    private TextView tvMonthName;
    private MonthView monthView;
    private View monthHeaderBorder;
    private SettingsManager appearanceModel;
    private View dividerView, holidayView;

    private TextView holidayTextView;
    private ImageView ivHolidayIcon;

    private LinearLayout monthHolidayContainer;

    public MonthHolder(View itemView, SettingsManager appearanceModel) {
        super(itemView);
        monthView = itemView.findViewById(R.id.month_view);
        tvMonthName = itemView.findViewById(R.id.tv_month_name);
        monthHeaderBorder = itemView.findViewById(R.id.month_header_border);
        monthHolidayContainer = itemView.findViewById(R.id.month_holidays_container);
        dividerView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.view_divider, null);
        holidayView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.view_holidays, null);
        holidayTextView = holidayView.findViewById(R.id.txtHolidayName);
        ivHolidayIcon = holidayView.findViewById(R.id.ivHolidayViewImage);
        this.appearanceModel = appearanceModel;
    }

    public void setDayAdapter(DaysAdapter adapter) {
        getMonthView().setAdapter(adapter);
    }

    public void bind(Month month) {
        tvMonthName.setText(month.getMonthName());
        tvMonthName.setTextColor(appearanceModel.getMonthTextColor());
        TextViewCompat.setTextAppearance(tvMonthName, appearanceModel.getMonthTextAppearance());

        List<String> holidays = month.getHolidays();

        monthHolidayContainer.removeAllViews();
        if (holidays != null && !holidays.isEmpty()) {
            monthHolidayContainer.addView(dividerView);
            for (String item : holidays) {
                addHolidayView(item);
            }
        }

        tvMonthName.setBackgroundResource(appearanceModel.getCalendarOrientation() == OrientationHelper.HORIZONTAL ? R.drawable.border_top_bottom : 0);
        monthHeaderBorder.setVisibility(appearanceModel.getCalendarOrientation() == OrientationHelper.HORIZONTAL ? View.GONE : View.VISIBLE);
        monthView.initAdapter(month);
    }

    public void addHolidayView(String holidayName) {

        View holidayView = LayoutInflater.from(monthHolidayContainer.getContext()).inflate(R.layout.view_holidays, monthHolidayContainer, false);

        TextView holidayTextView = holidayView.findViewById(R.id.txtHolidayName);
        ImageView ivHolidayIcon = holidayView.findViewById(R.id.ivHolidayViewImage);

        holidayTextView.setText(holidayName);
        ivHolidayIcon.setImageResource(appearanceModel.getConnectedDayIconRes());

        monthHolidayContainer.addView(holidayView);
    }

    public MonthView getMonthView() {
        return monthView;
    }
}

