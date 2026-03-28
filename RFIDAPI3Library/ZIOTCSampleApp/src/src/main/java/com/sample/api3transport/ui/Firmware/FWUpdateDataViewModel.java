package com.sample.api3transport.ui.Firmware;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FWUpdateDataViewModel extends ViewModel {

    private final MutableLiveData<String[]> fwUpdateStatus =
            new MutableLiveData<>();

    public LiveData<String[]> getStatus() {
        return fwUpdateStatus;
    }

    public void setStatus(String[] item) {
        fwUpdateStatus.setValue(item);
    }
}


