package com.airoha.android.spp.headset.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.airoha.android.spp.headset.R;

import java.util.ArrayList;

public class FragmentContainer extends BaseFragment {
    private boolean isAdvance = true;
    private static ArrayList<String> TITLES = new ArrayList<>();
    public static ViewPager pager;
    private View view;
    private View linearlayout;
    private MyPagerAdapter adapter;
    private Button nextButton, prevButton;
    FragmentInfoOption mFragmentInfoOption;
    FragmentSelectMode mFragmentSelectMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mFragmentSelectMode = new FragmentSelectMode();
        mFragmentInfoOption = new FragmentInfoOption();
        view = inflater.inflate(R.layout.fragment_main, container, false);
        linearlayout = view.findViewById(R.id.linearlayout);

        if (isAdvance) {
            TITLES.clear();
            TITLES.add("");
            TITLES.add("");
            linearlayout.setVisibility(View.VISIBLE);
        } else {
            TITLES.clear();
            TITLES.add("");
            linearlayout.setVisibility(View.GONE);
        }

        pager = (ViewPager) view.findViewById(R.id.pager);
        adapter = new MyPagerAdapter(this.getChildFragmentManager());
        pager.setAdapter(adapter);

        getActivity().invalidateOptionsMenu();

        nextButton = (Button) view.findViewById(R.id.nextbtn);
        nextButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (pager.getCurrentItem() != adapter.getCount() - 1) {
                    pager.setCurrentItem(pager.getCurrentItem() + 1);
                    nextButton.setTextColor(getActivity().getResources()
                            .getColor(android.R.color.holo_blue_light));
                    prevButton.setTextColor(getActivity().getResources()
                            .getColor(R.color.viewpages));
                }
            }
        });
        prevButton = (Button) view.findViewById(R.id.prevbtn);
        prevButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (pager.getCurrentItem() != 0) {
                    pager.setCurrentItem(pager.getCurrentItem() - 1);
                    prevButton.setTextColor(getActivity().getResources()
                            .getColor(android.R.color.holo_blue_light));
                    nextButton.setTextColor(getActivity().getResources()
                            .getColor(R.color.viewpages));
                }
            }
        });
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    prevButton.setTextColor(getActivity().getResources()
                            .getColor(android.R.color.holo_blue_light));
                    nextButton.setTextColor(getActivity().getResources()
                            .getColor(R.color.viewpages));
                } else {
                    nextButton.setTextColor(getActivity().getResources()
                            .getColor(android.R.color.holo_blue_light));
                    prevButton.setTextColor(getActivity().getResources()
                            .getColor(R.color.viewpages));
                }
            }
        });

        prevButton.setTextColor(getActivity().getResources().getColor(
                android.R.color.holo_blue_light));
        nextButton.setTextColor(getActivity().getResources().getColor(
                R.color.viewpages));

        return view;
    }

    public void setviewpager(int item) {
        pager.setCurrentItem(item, true);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES.get(position);
        }

        @Override
        public int getCount() {
            return TITLES.size();
        }

        public void setviewpager(int item) {
            pager.setCurrentItem(item, true);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return mFragmentInfoOption;
            } else {
                return mFragmentSelectMode;
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFragmentSelectMode.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void refreshInfo(){
        mFragmentInfoOption.sendDelayedCmdToStart();
    }
}