package com.petkit.android.model;

public class CardDataBase {

    protected int dataType;
    protected int itemViewType;

    public CardDataBase() {
    }

    public CardDataBase(int itemViewType) {
        this.itemViewType = itemViewType;
    }

    public int getItemViewType() {
        return itemViewType;
    }

    public void setItemViewType(int itemViewType) {
        this.itemViewType = itemViewType;
    }

    public int getDateType() {
        return dataType;
    }

    public void setDateType(int dataType) {
        this.dataType = dataType;
    }

}
