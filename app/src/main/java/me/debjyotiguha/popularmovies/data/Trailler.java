package me.debjyotiguha.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Trailler implements Serializable, Parcelable {

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com";
    private static final String YOUTUBE_PATH_URL = "/watch?v=";

    private String mkey;
    private String mName;
    private String mUrlString;

    public Trailler(String key, String name){
        setKey(key);
        setName(name);
        setUrl();
    }

    private String getKey() {
        return mkey;
    }

    private void setKey(String key) {
        this.mkey = key;
    }

    public String getName() {
        return mName;
    }

    private void setName(String Name) {
        this.mName = Name;
    }

    public String getUrlString(){
        return mUrlString;
    }

    private void setUrl(){
        this.mUrlString = Trailler.YOUTUBE_BASE_URL+Trailler.YOUTUBE_PATH_URL+getKey();
    }

    @Override
    public String toString() {
        return "Trailler:{key:" + getKey() +
                ",name:" + getName() +
                ",url:" + getUrlString() +
                "}";
    }

    //methods below exists to implement Parcelable interface
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mkey);
        parcel.writeString(mName);
        parcel.writeString(mUrlString);
    }

    public static final Parcelable.Creator<Trailler> CREATOR
            = new Parcelable.Creator<Trailler>() {
        public Trailler createFromParcel(Parcel in) {
            return new Trailler(in);
        }

        public Trailler[] newArray(int size) {
            return new Trailler[size];
        }
    };

    //constructor for Parcelable
    private Trailler(Parcel in) {
        mkey = in.readString();
        mName = in.readString();
        mUrlString = in.readString();
    }
}
