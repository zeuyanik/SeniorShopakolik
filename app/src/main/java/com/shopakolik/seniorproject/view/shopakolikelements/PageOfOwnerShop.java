package com.shopakolik.seniorproject.view.shopakolikelements;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.shopakolik.seniorproject.R;
import com.shopakolik.seniorproject.controller.databasecontroller.DatabaseManager;
import com.shopakolik.seniorproject.model.shopakolikelements.Campaign;
import com.shopakolik.seniorproject.model.shopakolikelements.Constants;
import com.shopakolik.seniorproject.model.shopakolikelements.Store;
import com.shopakolik.seniorproject.model.shopakolikelements.Util;

import org.w3c.dom.Text;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by IREM on 4/23/2015.
 */
public class PageOfOwnerShop extends BaseActivity {


    private String email = "";
    private String password = "";
    private String userType = "";

    private RelativeLayout baseLayout;
    private View shopView;
    private LinearLayout campaignList;
    private ScrollView scroll;
    private int scrollY;

    private Bitmap image;
    private BitmapFactory.Options options;
    private Context mContext;


    public void onCreate(Bundle savedInstanceState) {


        Bundle extras = getIntent().getExtras();

        email = extras.getString("user_email");
        password = extras.getString("user_password");
        userType = extras.getString("user_type");
        mContext = this;


        super.onCreate(savedInstanceState);
        baseLayout = (RelativeLayout) findViewById(R.id.baseLayout);
        shopView = getLayoutInflater().inflate(R.layout.brandpage, baseLayout, false);
        campaignList = (LinearLayout) shopView.findViewById(R.id.campaignlist);
        scroll = (ScrollView) findViewById(R.id.content_frame);

//        Log.e("After on Clck ", "" + scroll.getScrollY());

        fillContent();

        baseLayout.addView(shopView);
    }

    private void fillContent() {

//        scroll.scrollTo(0, scrollY);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Store store = DatabaseManager.getMyStore(email, password);
                    final ArrayList<Campaign> list = store.getCampaigns();


                    //String logourl = DatabaseManager.getServerUrl() + "Images/StoreLogos/" + store.getLogo();
                    URL url = new URL("https://s3.amazonaws.com/shopakolik/"+store.getLogo());
                    final String logourl = store.getLogo();
                    options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    final File file = new File(
                            Environment.getExternalStorageDirectory().getPath()+"/Shop/",
                            logourl);

                    //File f = new File(getExternalCacheDir(), logourl);
                    if (file.exists()) {
                        Log.e("PAGE OF OWNER ", "EXIST");
                        image = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/Shop/"+logourl, options);
                    }else {
                        Log.e("PAGE OF OWNER ", "NOT EXIST "+Environment.getExternalStorageDirectory().getPath()+"/Shop/"+logourl);
                        Thread downloadT = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                TransferManager tm = null;
                                try {
                                    tm = new TransferManager(Util.getCredProvider(mContext));
                                    File mFile = new File(
                                            Environment.getExternalStorageDirectory().getPath()+"/Shop/",logourl);
                                    tm.download("shopakolik", logourl, mFile);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        downloadT.start();
                        image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView logo = (ImageView) shopView.findViewById(R.id.brand_logo);
                            TextView title = (TextView) shopView.findViewById(R.id.brand_name);
                            ImageView locationsButton = (ImageView) shopView.findViewById(R.id.storeLocationsButton);

                            logo.setImageBitmap(image);
                            title.setText(store.getName());

                            locationsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    float[] latitudes = new float[store.getLocations().size()];
                                    float[] longitudes = new float[store.getLocations().size()];
                                    String[] locations = new String[store.getLocations().size()];
                                    String[] addresses = new String[store.getLocations().size()];

                                    for (int i = 0; i < store.getLocations().size(); i++) {
                                        latitudes[i] = store.getLocations().get(i).getLatitude();
                                        longitudes[i] = store.getLocations().get(i).getLongitude();
                                        locations[i] = store.getLocations().get(i).getLocation();
                                        addresses[i] = store.getLocations().get(i).getAddress();
                                    }

                                    Intent intent = new Intent(PageOfOwnerShop.this, map.class);
                                    intent.putExtra("latitudes", latitudes);
                                    intent.putExtra("longitudes", longitudes);
                                    intent.putExtra("locations", locations);
                                    intent.putExtra("addresses", addresses);
                                    startActivity(intent);
                                }
                            });

                            try {
                                for (int i = 0; i < list.size(); i++) {
                                    //  Log.e("Store", "" + list.get(i).getStoreId());
                                    final int finalI = i;

                                    //decleare
                                    final View itemView = getLayoutInflater().inflate(R.layout.campaignlistitem, campaignList, false);


                                    //initialize
                                    final ImageView campaignImage = (ImageView) itemView.findViewById(R.id.campimage);
                                    final TextView features = (TextView) itemView.findViewById(R.id.features);
                                    final TextView peramo = (TextView) itemView.findViewById(R.id.peramo);
                                    final TextView date = (TextView) itemView.findViewById(R.id.dateRemainer);
                                    final ImageView deleteimage = (ImageView) itemView.findViewById(R.id.deleteimage);
                                    final ImageView updateimage = (ImageView) itemView.findViewById(R.id.update);
                                    final ToggleButton button = (ToggleButton) itemView.findViewById(R.id.addFavoriteIcon);
                                    final ImageView cloud = (ImageView) itemView.findViewById(R.id.image);


                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                final String imagePath = list.get(finalI).getImage();
                                                URL imageURL = new URL("https://s3.amazonaws.com/shopakolik/"+imagePath);
                                                final File file = new File(
                                                        Environment.getExternalStorageDirectory().getPath()+"/Shop/",
                                                        imagePath);
                                                if (file.exists()) {
                                                    image = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/Shop/"+imagePath, options);
                                                }else {
                                                    Thread downloadT = new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            TransferManager tm = null;
                                                            try {
                                                                tm = new TransferManager(Util.getCredProvider(mContext));

                                                                File mFile = new File(
                                                                        Environment.getExternalStorageDirectory().getPath()+"/Shop/"
                                                                        ,imagePath);
                                                                tm.download("shopakolik", imagePath, mFile);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                    downloadT.start();
                                                    image = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                                                }

                                                final Bitmap imageCamp = image;

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        campaignImage.setImageBitmap(imageCamp);

                                                        // set campaign image with to fit screen and keep aspect ratio
                                                        float screenWidth = PageOfOwnerShop.this.getWindowManager()
                                                                .getDefaultDisplay().getWidth();
                                                        float ratio = campaignImage.getLayoutParams().height
                                                                / campaignImage.getLayoutParams().width;
                                                        campaignImage.getLayoutParams().width = (int) screenWidth;
                                                        campaignImage.getLayoutParams().height = (int)
                                                                (screenWidth * ratio);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();

                                    final int finalI1 = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            //set

                                            deleteimage.setVisibility(View.VISIBLE);
                                            updateimage.setVisibility(View.VISIBLE);
                                            deleteimage.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {


                                                    // 1. Instantiate an AlertDialog.Builder with its constructor
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(PageOfOwnerShop.this);

                                                    // 2. Chain together various setter methods to set the dialog characteristics
                                                    builder.setMessage(R.string.deleteCampaignInfo);

                                                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        boolean result = DatabaseManager.removeCampaign(email, password, list.get(finalI).getCampaignId());
                                                                        if (result) { // change as true when want to test only scroll position

                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    // TODO when refreshing the page it goes to the top of the page instead of maintaining the position where it left.
                                                                                    scrollY = campaignList.getBottom();
                                                                                    Log.e("S1 crool Y : ", "" + scroll.getScrollY());
                                                                                    campaignList.removeAllViews();
                                                                                    Log.e("2 Scrool Y : ", "" + scroll.getScrollY());
                                                                                    fillContent();
                                                                                    Log.e("3 Scrool Y : ", "" + scroll.getScrollY());

                                                                                }
                                                                            });
                                                                        }
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }).start();
                                                        }
                                                    });


                                                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    });

                                                    // 3. Get the AlertDialog from create()
                                                    AlertDialog dialog = builder.create();
                                                    dialog.show();
                                                }
                                            });
                                            //Change Click
                                            updateimage.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent(PageOfOwnerShop.this, UpdateCampaignPage.class);
                                                    intent.putExtra("user_email", email);
                                                    intent.putExtra("user_password", password);
                                                    intent.putExtra("campaignID", list.get(finalI).getCampaignId());
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                            String feats = "";
                                            String cond = list.get(finalI).getCondition();
                                            if (cond != null && cond != "") {
                                                feats = cond;
                                            }
                                            String details = list.get(finalI).getDetails();
                                            if (details != null && details != "") {
                                                feats += "\n" + details;
                                            }
                                            features.setText(feats);

                                            String percamo = "";
                                            int pers = list.get(finalI).getPercentage();
                                            if (pers != 0) {
                                                percamo = pers + "%";
                                            } else {
                                                float amo = list.get(finalI).getAmount();
                                                if (amo != 0) {
                                                    percamo = "$" + String.format("%.2f", amo);
                                                }
                                            }
                                            peramo.setText(percamo);

                                            long diff = list.get(finalI).getEndDate().getTime() - System.currentTimeMillis();
                                            if (diff < 0) {
                                                date.setText("" + (Math.abs(diff) / (24 * 60 * 60 * 1000)) + " days expired ");
                                            } else
                                                date.setText("" + (diff / (24 * 60 * 60 * 1000)) + " days");

                                            button.setVisibility(View.GONE);

                                            campaignList.addView(itemView);

                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Log.e("Scrool Y Identified : ", "" + scroll.getScrollY());
        Log.e("Scrool Y : ", "" + scrollY);
        scroll.scrollTo(0, scrollY);
        Log.e("Scrool Y Identified2 : ", "" + scroll.getScrollY());


    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PageOfOwnerShop.this);
        builder.setMessage("Are you sure you want to leave?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
