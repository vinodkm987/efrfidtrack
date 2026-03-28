package com.sample.api3transport;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MenuViewModel extends ViewModel {
    private final MutableLiveData<Boolean> selectedItem = new MutableLiveData<Boolean>();
    public void selectItem(Boolean item) {
        selectedItem.setValue(item);
    }
    public LiveData<Boolean> getSelectedItem() {
        return selectedItem;
    }
}
