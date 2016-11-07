package org.emoncms.myapps.myelectric;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Holds the details of a MyElectric instance
 */
public class MyElectricSettings implements Parcelable {

    private String name;
    private int powerFeedId;
    private int useFeedId;
    private float unitCost;
    private String costSymbol;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MyElectricSettings createFromParcel(Parcel in) {
            return new MyElectricSettings(in);
        }
        public MyElectricSettings[] newArray(int size) {
            return new MyElectricSettings[size];
        }
    };

    public MyElectricSettings(String name, int powerFeedId, int useFeedId, float unitCost, String costSymbol) {
        this.name = name;
        this.powerFeedId = powerFeedId;
        this.useFeedId = useFeedId;
        this.unitCost = unitCost;
        this.costSymbol = costSymbol;
    }

    public MyElectricSettings(Parcel in) {
        this.name = in.readString();
        this.powerFeedId = in.readInt();
        this.useFeedId = in.readInt();
        this.unitCost = in.readFloat();
        this.costSymbol = in.readString();
    }


    public String getName() {
        return name;
    }

    public int getPowerFeedId() {
        return powerFeedId;
    }

    public int getUseFeedId() {
        return useFeedId;
    }

    public float getUnitCost() {
        return unitCost;
    }

    public String getCostSymbol() {
        return costSymbol;
    }

    public void setUseFeedId(int useFeedId) {
        this.useFeedId = useFeedId;
    }

    public void setPowerFeedId(int powerFeedId) {
        this.powerFeedId = powerFeedId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(powerFeedId);
        parcel.writeInt(useFeedId);
        parcel.writeFloat(unitCost);
        parcel.writeString(costSymbol);
    }

    public String toString() {
        return name + ", power: " + powerFeedId + ", use: " + useFeedId;
    }
}
