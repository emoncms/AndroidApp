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
    private String unitCost;
    private String costSymbol;
    private String powerScale;
    private String customCurrencySymbol;
    private float powerScaleFloat;
    private float unitCostFloat;
    private boolean deleted = false;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MyElectricSettings createFromParcel(Parcel in) {
            return new MyElectricSettings(in);
        }
        public MyElectricSettings[] newArray(int size) {
            return new MyElectricSettings[size];
        }
    };

    public static MyElectricSettings fromJson(int id, JSONObject jsonObject) throws JSONException {

        String customCurrencySymbol = "";
        if (jsonObject.has("customCurrencySymbol")) {
            customCurrencySymbol = jsonObject.getString("customCurrencySymbol");
        }

        return new MyElectricSettings(id, jsonObject.getString("name"),
                jsonObject.getInt("powerFeedId"),
                jsonObject.getInt("useFeedId"),
                jsonObject.getString("powerScale"),
                jsonObject.getString("unitCost"),
                jsonObject.getString("costSymbol"),
                customCurrencySymbol);
    }

    public MyElectricSettings(int id, String name, int powerFeedId, int useFeedId, String powerScale, String unitCost, String costSymbol, String customCurrencySymbol) {
        this.id = id;
        this.name = name;
        this.powerFeedId = powerFeedId;
        this.useFeedId = useFeedId;
        this.unitCost = unitCost;
        this.costSymbol = costSymbol;
        this.powerScale = powerScale;
        this.powerScaleFloat = stringToFloat(powerScale);
        this.unitCostFloat = stringToFloat(unitCost);
        this.customCurrencySymbol = customCurrencySymbol;
    }

    public MyElectricSettings(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.powerFeedId = in.readInt();
        this.useFeedId = in.readInt();
        this.powerScale = in.readString();
        this.unitCost = in.readString();
        this.costSymbol = in.readString();
        this.customCurrencySymbol = in.readString();
        this.powerScaleFloat = stringToFloat(powerScale);
        this.unitCostFloat = stringToFloat(unitCost);
    }

    public void setDeleted() {
        deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
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

    public String getPowerScale() {
        return powerScale;
    }

    public float getPowerScaleAsFloat() {
        return powerScaleFloat;
    }

    public void setPowerScale(String powerScale) {
        this.powerScale = powerScale;
        this.powerScaleFloat = stringToFloat(powerScale);
    }

    public String getUnitCost() {
        return unitCost;
    }

    public String getCostSymbol() {
        return costSymbol;
    }

    public String getCustomCurrencySymbol() {
        return customCurrencySymbol;
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

    public void setCustomCurrencySymbol(String customCurrencySymbol) {
        this.customCurrencySymbol = customCurrencySymbol;
    }

    public void setUnitCost(String unitCost) {

        this.unitCost = unitCost;
        this.unitCostFloat = stringToFloat(unitCost);

    }

    public float getUnitCostFloat() {
        return unitCostFloat;
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
        parcel.writeString(powerScale);
        parcel.writeString(unitCost);
        parcel.writeString(costSymbol);
        parcel.writeString(customCurrencySymbol);
    }

    public String toJson() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("powerFeedId",powerFeedId);
        jsonObject.put("useFeedId",useFeedId);
        jsonObject.put("powerScale",powerScale);
        jsonObject.put("unitCost",unitCost);
        jsonObject.put("costSymbol",costSymbol);
        jsonObject.put("customCurrencySymbol",customCurrencySymbol);
        return jsonObject.toString();
    }

    public String toString() {
        return name + ", power: " + powerFeedId + ", use: " + useFeedId;
    }

    private float stringToFloat(String val) {
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
