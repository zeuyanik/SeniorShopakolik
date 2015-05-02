package com.shopakolik.seniorproject.controller.notificationcontroller;

/**
 * Created by Yusuf on 19.04.2015.
 */

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.shopakolik.seniorproject.R;
import com.shopakolik.seniorproject.controller.databasecontroller.DatabaseManager;
import com.shopakolik.seniorproject.controller.databasecontroller.UserType;
import com.shopakolik.seniorproject.model.shopakolikelements.Store;
import com.shopakolik.seniorproject.model.shopakolikelements.User;
import com.shopakolik.seniorproject.view.shopakolikelements.BrandPage;
import com.shopakolik.seniorproject.view.shopakolikelements.MainActivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NotificationService extends Service {
    LocationManager locationManager;
    LocationListener locationListener;
    private String email, password;
    private ArrayList<Store> stores = new ArrayList<>();
    SharedPreferences sharedpreferences;

    public NotificationService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        email = sharedpreferences.getString("emailKey", "");
        password = sharedpreferences.getString("passwordKey", "");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stores = DatabaseManager.getFavoriteStores(email, password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
            Log.e("notification service", "service started");
            /*Location fake = new Location("shopProv");
            fake.setLatitude(39.8715f);
            fake.setLongitude(32.7503f);*/
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        for (int i = 0; i < stores.size(); i++) {
                            ArrayList<com.shopakolik.seniorproject.model.shopakolikelements.Location> locs = stores.get(i).getLocations();
                            for (int j = 0; j < locs.size(); j++) {
                                Location storeLoc = new Location("shopProv");
                                storeLoc.setLongitude(locs.get(j).getLongitude());
                                storeLoc.setLatitude(locs.get(j).getLatitude());
                                if (location.distanceTo(storeLoc) < 200) {
                                    createNotification(stores.get(i).getName(), locs.get(j).getLocation(), stores.get(i).getLogo(), i * j, stores.get(i).getStoreId());
                                    Log.e("notification", "created");
                                }
                            }
                        }

                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private void createNotification(final String storeName, final String branch, final String logo, final int id, final int storeID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this);
                builder.setOnlyAlertOnce(true);
                String mlogo = DatabaseManager.getServerUrl() + "Images/StoreLogos/" + logo;
                Intent i = new Intent(NotificationService.this, BrandPage.class);
                i.putExtra("user_email", email);
                i.putExtra("user_password", password);
                i.putExtra("store_id", storeID);
                i.putExtra("user_type", UserType.Customer.toString());
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent intent = PendingIntent.getActivity(NotificationService.this, 0, i,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(intent);

                builder.setTicker(storeName + " " + branch + " is around you!");

                builder.setSmallIcon(R.drawable.ic_launcher);

                builder.setAutoCancel(true);

                Notification notification = builder.build();

                RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notifcollapsed);

                URL url = null;
                Bitmap image = null;
                try {
                    url = new URL(mlogo);
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                contentView.setTextViewText(R.id.notiftext1, storeName + " " + branch + " is around you!");
                contentView.setImageViewBitmap(R.id.notifimage1, image);
                // set content view
                notification.contentView = contentView;

                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(id, notification);
            };

        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    /*@Override
    protected void onHandleIntent(Intent intent) {

    }*/
}