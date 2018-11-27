package org.mozilla.gecko.controlcenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mozilla.gecko.R;
import org.mozilla.gecko.util.GeckoBundle;
import org.mozilla.gecko.util.GeckoBundleUtils;
import org.mozilla.gecko.widget.themed.ThemedRelativeLayout;

import java.util.ArrayList;
import java.util.List;

import static org.mozilla.gecko.controlcenter.ControlCenterPagerAdapter.IS_TODAY;

/**
 * Copyright © Cliqz 2018
 */
public class DashboardFragment extends ControlCenterFragment {

    private ThemedRelativeLayout mBondDashboardLayout;
    private RecyclerView mDashBoardListView;
    private TextView mDashboardStateTextView;
    private View mDisableDashboardView;
    private DashboardAdapter mDashboardAdapter;
    private boolean mIsDailyView;
    private final int [] tabsTitleIds = {R.string.bond_dashboard_today_title,R.string
            .bond_dashboard_week_title};
    private boolean mIsBondWhite = false;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        mIsDailyView = arguments.getBoolean(IS_TODAY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.bond_dashboard_fragment, container,
                false);
        mBondDashboardLayout = (ThemedRelativeLayout) view.findViewById(R.id.bond_dashboard_layout);
        mDashBoardListView = (RecyclerView) view.findViewById(R.id.dash_board_list_view);
        mDashboardStateTextView = (TextView) view.findViewById(R.id.dashboard_state_text);
        mDisableDashboardView = view.findViewById(R.id.dashboard_disable_view);
        mDashboardAdapter = new DashboardAdapter(getContext());
        mDashBoardListView.setAdapter(mDashboardAdapter);
        mDashBoardListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBondDashboardLayout.setBondWhite(mIsBondWhite);
        mDashboardAdapter.setIsBondWhite(mIsBondWhite);
        changeDashboardState(true);//@todo real state, maybe from preference
        return view;
    }

    public void changeDashboardState(boolean isEnabled) {
        final int stateTextId;
        final int overlayVisibility;
        final int stateTextColorId;
        if(isEnabled) {
            stateTextId = R.string.bond_dashboard_contols_on;
            overlayVisibility = View.GONE;
            stateTextColorId = R.color.bond_general_color_blue;
        } else {
            stateTextId = R.string.bond_dashboard_contols_off;
            overlayVisibility = View.VISIBLE;
            stateTextColorId = android.R.color.white;
        }
        mDisableDashboardView.setVisibility(overlayVisibility);
        final StringBuilder stateText = new StringBuilder();
        stateText.append(getString(R.string.bond_dashboard_ultimate_protection));
        stateText.append(" ");
        stateText.append(getString(stateTextId));

        mDashboardStateTextView.setTextColor(ContextCompat.getColor(getContext(),stateTextColorId));
        mDashboardStateTextView.setText(stateText);
    }

    @Override
    public String getTitle(Context context, int pos) {
        return context.getString(tabsTitleIds[pos]);
    }

    @Override
    public void updateUI(GeckoBundle data) {
        if (mDashboardAdapter == null) {
            return;
        }
        if (mIsDailyView) {
            updateViews(GeckoBundleUtils.safeGetBundle(data, "data/day"));
        } else {
            updateViews(GeckoBundleUtils.safeGetBundle(data, "data/week"));
        }
    }

    @Override
    public void refreshUI() {
        if (getView() == null) {
            return; //return if view is not inflated yet
        }
        changeDashboardState(
                (boolean)getActivity().findViewById(R.id.bond_dashboard_state_button).getTag());
        mDashboardAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshUIComponent(int id, boolean optionValue) {
        if(id == R.id.bond_dashboard_state_button) {
            changeDashboardState(optionValue);
        } else if (id == R.id.bond_dashboard_clear_button) {
            mDashboardAdapter.resetData();
        } else if(id == R.id.bond_dashboard_layout) {
            mIsBondWhite = optionValue;
            if(mBondDashboardLayout != null) {
                mBondDashboardLayout.setBondWhite(mIsBondWhite);
                mDashboardAdapter.setIsBondWhite(mIsBondWhite);
            }
        }
    }

    public void updateViews(GeckoBundle data) {
        if (data == null) {
            return;
        }
        final MeasurementWrapper timeSaved = ValuesFormatterUtil.formatTime(data.getInt("timeSaved", 0));
        final MeasurementWrapper dataSaved = ValuesFormatterUtil.formatBytesCount(data.getInt("dataSaved", 0));
        final MeasurementWrapper adsBlocked = ValuesFormatterUtil.formatBlockCount(data.getInt("adsBlocked", 0));
        final MeasurementWrapper trackersDetected = ValuesFormatterUtil.formatBlockCount(data.getInt("trackersDetected", 0));
        final List<DashboardItemEntity> dashboardItems = new ArrayList<>();
        dashboardItems.add(new DashboardItemEntity(timeSaved.getValue(), timeSaved.getUnit(),
                R.drawable.ic_time_circle, "Time Saved", "That you can spend with your friends", -1));
        dashboardItems.add(new DashboardItemEntity(adsBlocked.getValue(), adsBlocked.getUnit(),
                R.drawable.ic_ad_blocking_shiel, "Ads Blocked", "That you can enjoy surfing without ads", -1));
        dashboardItems.add(new DashboardItemEntity(dataSaved.getValue(), dataSaved.getUnit(), -1,
                "Data Saved", "more that enough to watch another video", -1));
        //@TODO decide how to calculate battery saved
        dashboardItems.add(new DashboardItemEntity("255", "MIN", R.drawable.ic_battery, "Battery Saved",
                "so that you can enjoy your phone a little longer", -1));
        dashboardItems.add(new DashboardItemEntity("", "", R.drawable.ic_anti_phishing_hook, "Phishing protection",
                "so that you can swim freely with our browser", -1));
        dashboardItems.add(new DashboardItemEntity(trackersDetected.getValue(), "\n\tCOMPANIES", R.drawable
                .ic_eye, "Tracker Companies blocked",
                "...companies with most trackers: Google, Amaozn, Facebook,...", -1));
        /* @todo unhide the money item
        dashboardItems.add(new DashboardItemEntity("261","EUR",-1,"Money saved",
                "...how much is your time worth per h", AVERAGE_MONEY_BAR_VALUE));
        */
        mDashboardAdapter.addItems(dashboardItems);
    }
}
