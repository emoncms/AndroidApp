package org.emoncms.myapps.myelectric;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Holds the details of a MyElectric instance
 */
public class MyElectricSettings implements Parcelable {

    private int id;
    private String name;
    private int powerFeedId;
    private int useFeedId;
    private double unitCost;
    private String costSymbol;
    private String currency;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MyElectricSettings createFromParcel(Parcel in) {
            return new MyElectricSettings(in);
        }
        public MyElectricSettings[] newArray(int size) {
            return new MyElectricSettings[size];
        }
    };

    public static MyElectricSettings fromJson(int id, JSONObject jsonObject) throws JSONException {
        return new MyElectricSettings(id, jsonObject.getString("name"),
                jsonObject.getInt("powerFeedId"),
                jsonObject.getInt("useFeedId"),
                jsonObject.getDouble("unitCost"),
                jsonObject.has("currency") ? jsonObject.getString("currency") : "",
                jsonObject.getString("costSymbol"));
    }

    public MyElectricSettings(int id, String name, int powerFeedId, int useFeedId, double unitCost, String currency, String costSymbol) {
        this.id = id;
        this.name = name;
        this.powerFeedId = powerFeedId;
        this.useFeedId = useFeedId;
        this.unitCost = unitCost;
        this.costSymbol = costSymbol;
        this.currency = currency;
    }

    public MyElectricSettings(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.powerFeedId = in.readInt();
        this.useFeedId = in.readInt();
        this.unitCost = in.readDouble();
        this.currency = in.readString();
        this.costSymbol = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public double getUnitCost() {
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

    public void setName(String name) {
        this.name = name;
    }

    public void setCostSymbol(String costSymbol) {
        this.costSymbol = costSymbol;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeInt(powerFeedId);
        parcel.writeInt(useFeedId);
        parcel.writeDouble(unitCost);
        parcel.writeString(currency);
        parcel.writeString(costSymbol);
    }

    public String toJson() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("powerFeedId",powerFeedId);
        jsonObject.put("useFeedId",useFeedId);
        jsonObject.put("unitCost",unitCost);
        jsonObject.put("costSymbol",costSymbol);
        jsonObject.put("currency",currency);
        return jsonObject.toString();
    }

    public String toString() {
        return name + ", power: " + powerFeedId + ", use: " + useFeedId;
    }
}
