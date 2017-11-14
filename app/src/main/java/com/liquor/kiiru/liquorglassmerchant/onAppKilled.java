package com.liquor.kiiru.liquorglassmerchant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.firebase.geofire.GeoFire;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.liquor.kiiru.liquorglassmerchant.Common.Common;

/**
 * Created by Kiiru on 11/14/2017.
 */

public class onAppKilled extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        String userId = Common.currentUser.getPhone();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("merchantsAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }
}
