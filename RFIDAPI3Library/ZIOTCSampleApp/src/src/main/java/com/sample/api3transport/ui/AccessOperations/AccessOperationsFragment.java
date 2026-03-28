package com.sample.api3transport.ui.AccessOperations;

import static com.sample.api3transport.RFIDHandler.mReader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sample.api3transport.databinding.FragmentAccessOperationsBinding;

public class AccessOperationsFragment extends Fragment {
    private static final String TAG = "TRIGGER_SETTINGS";
    private FragmentAccessOperationsBinding binding;
    FragmentAdaptor fragmentAdaptor;
    String[] tabTittle = {"Read/Write", "Lock", "Kill"};


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentAccessOperationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mReader == null || !mReader.isConnected()){
            Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_SHORT).show();
            return;
        }

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
                return new ReadWriteFragment();
            }else if(position == 1){
                return new LockFragment();
            }else if(position == 2){
                return new KillFragment();
            }
            return new ReadWriteFragment();
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}