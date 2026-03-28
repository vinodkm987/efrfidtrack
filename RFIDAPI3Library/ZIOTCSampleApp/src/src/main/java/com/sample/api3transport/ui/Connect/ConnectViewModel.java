package com.sample.api3transport.ui.Connect;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sample.api3transport.ui.Inventory.InventoryItem;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectViewModel extends ViewModel {

    private final MutableLiveData<ConcurrentHashMap<String, InventoryItem>> inventoryItem =
            new MutableLiveData<>();

    public LiveData<ConcurrentHashMap<String, InventoryItem>> getInventoryItem() {
        return inventoryItem;
    }

    public void setTagItems(ConcurrentHashMap<String, InventoryItem> item) {
        inventoryItem.setValue(item);
    }
}


