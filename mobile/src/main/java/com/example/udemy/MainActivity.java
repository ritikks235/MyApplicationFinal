package com.example.udemy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Freezable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataApi.DataListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    public static final String IMAGE_PATH = "/storage/emulated/0/Pictures/Screenshots";
    private static final int BUFFER_SIZE = 1000;
    private GoogleApiClient googleApiClient;
    private View layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.myLayoutId);
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addApi(Wearable.API);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);

        googleApiClient = builder.build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient,this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {
         Wearable.DataApi.removeListener(googleApiClient,this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Wearable.DataApi.removeListener(googleApiClient,this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        Wearable.DataApi.removeListener(googleApiClient,this);
        googleApiClient.disconnect();
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        for(DataEvent event : events){
            if(event.getType() == DataEvent.TYPE_CHANGED){
                Log.v("Data is changed","=============");
                String path = event.getDataItem().getUri().getPath();
                if(IMAGE_PATH.equals(path)){
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset photo = dataMapItem.getDataMap().getAsset("image");
                    //final Bitmap bitmap =
                    loadBitmapFromAsset(googleApiClient,photo);

//                    runOnUiThread(
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                    layout.setBackground(new BitmapDrawable(getResources(),bitmap));
//                                }
//                            }
//                    );
                }
            }
        }
    }
    private void loadBitmapFromAsset(GoogleApiClient googleApiClient, Asset asset){
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //The line below breaks the app
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(googleApiClient, asset).await().getInputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int byteRead;
                Bitmap bitmap = BitmapFactory.decodeStream(assetInputStream);
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"receivedFromWearable","itachi uchiha of the leaf");

                runOnUiThread(()->{
                    layout.setBackground(new BitmapDrawable(getResources(),bitmap));
                });
                Log.i("Type", assetInputStream.toString());
            }
        }).start();
        //InputStream assetInputStream = Wearable.DataApi.getFdForAsset(googleApiClient,asset).await().getInputStream();

        //return BitmapFactory.decodeStream(assetInputStream);
    }
}