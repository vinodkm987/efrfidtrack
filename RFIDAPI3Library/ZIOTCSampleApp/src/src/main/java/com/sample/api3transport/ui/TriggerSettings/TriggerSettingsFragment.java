package com.sample.api3transport.ui.TriggerSettings;




import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.sample.api3transport.databinding.FragmentTriggerSettingsBinding;

public class TriggerSettingsFragment extends Fragment {
    private static final String TAG = "TRIGGER_SETTINGS";
    private FragmentTriggerSettingsBinding binding;
    FragmentAdaptor fragmentAdaptor;
    ViewPager2 viewPager;
    String[] tabTittle = {"Start Trigger", "Stop Trigger"};


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentTriggerSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentAdaptor = new FragmentAdaptor(this);
        binding.pager.setAdapter(fragmentAdaptor);
        new TabLayoutMediator(binding.tabLayout, binding.pager,
                (tab, position) -> tab.setText(tabTittle[position])).attach();



    }

    public static class FragmentAdaptor extends FragmentStateAdapter {
        public FragmentAdaptor(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if(position == 0){
                return new StartTriggerFragment();
            }else if(position == 1){
                return new StopTriggerFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}