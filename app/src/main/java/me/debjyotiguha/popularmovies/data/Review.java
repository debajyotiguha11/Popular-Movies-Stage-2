package me.debjyotiguha.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Review implements Serializable, Parcelable {

    private String mAuthor;
    private String mContent;
    private String mUrlString;

    public Review(String author, String content, String urlString){
        setAuthor(author);
        setContent(content);
        setUrlString(urlString);
    }

    public String getAuthor(){
        return mAuthor;
    }

    private void setAuthor(String author){
        this.mAuthor = author;
    }

    public String getContent(){
        return mContent;
    }

    private void setContent(String content){
        this.mContent = content;
    }

    public String getUrlString(){
        return mUrlString;
    }

    private void setUrlString(String urlString){
        this.mUrlString = urlString;
    }

    @Override
    public String toString() {
        return "Review:{author:" + getAuthor() +
                ",content:" + getContent() +
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
        parcel.writeString(mAuthor);
        parcel.writeString(mContent);
        parcel.writeString(mUrlString);
    }

    public static final Parcelable.Creator<Review> CREATOR
            = new Parcelable.Creator<Review>() {
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    //constructor for Parcelable
    private Review(Parcel in) {
        mAuthor = in.readString();
        mContent = in.readString();
        mUrlString = in.readString();
    }
}
