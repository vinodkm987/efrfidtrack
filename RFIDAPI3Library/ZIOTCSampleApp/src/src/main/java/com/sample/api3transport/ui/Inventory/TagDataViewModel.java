package com.sample.api3transport.ui.Inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zebra.rfid.api3.TagData;

public class TagDataViewModel extends ViewModel {

    private final MutableLiveData<TagData[]> inventoryItem =
            new MutableLiveData<>();

    public LiveData<TagData[]> getInventoryItem() {
        return inventoryItem;
    }

    public void setTagItems(TagData[] item) {
        inventoryItem.setValue(item);
    }
}


