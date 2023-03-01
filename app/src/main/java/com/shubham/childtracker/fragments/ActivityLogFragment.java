package com.shubham.childtracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.shubham.childtracker.Adapters.ActivityLogFragmentPagerAdapter;
import com.shubham.childtracker.R;

public class ActivityLogFragment extends Fragment {
	public static final String TAG = "ActivityLogTAG";
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_activity_log, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		//getData();
		
		ViewPager viewPager = view.findViewById(R.id.activityLogViewPager);
		viewPager.setAdapter(setupActivityLogFragmentPagerAdapter());
		
		TabLayout tabLayout = view.findViewById(R.id.activityLogTabLayout);
		tabLayout.setupWithViewPager(viewPager);
		
		
	}
	
	private PagerAdapter setupActivityLogFragmentPagerAdapter() {
		ActivityLogFragmentPagerAdapter pagerAdapter = new ActivityLogFragmentPagerAdapter(requireActivity().getSupportFragmentManager());
		pagerAdapter.addFragment(new CallsFragment(), getResources().getString(R.string.calls));
		pagerAdapter.addFragment(new MessagesFragment(), getResources().getString(R.string.messages));
		pagerAdapter.addFragment(new ContactsFragment(), getResources().getString(R.string.contacts));
		
		return pagerAdapter;
	}
}
