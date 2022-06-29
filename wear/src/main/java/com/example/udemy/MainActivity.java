package com.example.udemy;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.example.udemy.databinding.ActivityMainBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient googleApiClient = null;
    public static final String TAG = "My Assets.....";
    public static final String IMAGE_PATH = "/storage/emulated/0/Pictures/Screenshots";
    private TextView mTextView;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(binding.getRoot());
        setContentView(R.layout.activity_main);
        //binding = ActivityMainBinding.inflate(getLayoutInflater());

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addApi(Wearable.API);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);

        googleApiClient = builder.build();
        //mTextView = binding.text;
    }

    //connecting to data layer on starting activity
    @Override
    protected void onStart(){
        super.onStart();
        googleApiClient.connect();
    }

    //disconnecting to data layer on stopping activity
    @Override
    protected void onStop(){
        if(googleApiClient!=null &&  googleApiClient.isConnected())
        {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //leave blank
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //Bitmap myImage = BitmapFactory.decodeResource(getResources(),R.drawable.image);


    public void sendPictureOnClick(View view){
        Bitmap myImage = BitmapFactory.decodeResource(getResources(),R.drawable.image);

        if(myImage!=null && googleApiClient.isConnected()) {
            Asset asset = convertPictureToAsset(myImage);
            sendImageToDataLayer(asset);
        }
        else{
            Log.d(TAG,"Either myImage is null or googleApiClient is not connected");
        }
    }

    private void sendImageToDataLayer(Asset asset){
        if(googleApiClient.isConnected()){
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(IMAGE_PATH);
            putDataMapRequest.getDataMap().putAsset("image",asset);
            putDataMapRequest.getDataMap().putLong("time",new Date().getTime());

            PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(googleApiClient,putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                    Log.d(TAG,"sending image was successful: "+dataItemResult.getStatus().isSuccess());
                }
            });
        }
    }

    private Asset convertPictureToAsset(Bitmap myImage) {
        ByteArrayOutputStream byteStream = null;
        try{
            byteStream = new ByteArrayOutputStream();
            myImage.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        }
        finally{
            if(null!=byteStream){
                try{
                    byteStream.close();
                } catch (IOException e){
                    // ignore
                }
            }
        }

    }
}