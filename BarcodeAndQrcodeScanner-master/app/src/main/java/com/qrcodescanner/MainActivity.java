package com.qrcodescanner;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements OnClickListener {

    //View Objects

    private Button buttonScan;
    private TextView textViewName, textViewAddress,textViewKelas ;
    private LinearLayout llSearch;

    String goolgeMap = "com.google.android.apps.maps"; // identitas package aplikasi google masps android
    Uri gmmIntentUri;
    Intent mapIntent;

    //qr code scanner object
    private IntentIntegrator qrScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //View objects
        buttonScan = (Button) findViewById(R.id.scan_button);
        textViewName = (TextView) findViewById(R.id.textViewNama);
        textViewAddress = (TextView) findViewById(R.id.textViewNim);
        textViewKelas = (TextView) findViewById(R.id.textViewKelas);
        llSearch = (LinearLayout) findViewById(R.id.llSearch);


        //inisialisasi scan object
        qrScan = new IntentIntegrator(this);

        //menambahkan onclick listener
        buttonScan.setOnClickListener(this);
    }

    //Getting the scan results

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            llSearch.setVisibility(View.GONE);
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
            else if(Patterns.WEB_URL.matcher(result.getContents()).matches()){
                Intent visitUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(result.getContents()));
                startActivity(visitUrl);

            }
            else if (Patterns.PHONE.matcher(result.getContents()).matches()) {
                //Uri number = Uri.parse("tel:*888#");
                String telp = String.valueOf(result.getContents());
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + telp));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);
                try {
                    startActivity(Intent.createChooser(callIntent, "Waiting..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no phone apk clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
            else if(Patterns.EMAIL_ADDRESS.matcher(result.getContents()).matches()) {
                String email = String.valueOf(result.getContents());
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setType("message/rfc822");
                //i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                i.putExtra(Intent.EXTRA_SUBJECT, "tugas uts berhasil nilai A");
                i.putExtra(Intent.EXTRA_TEXT, "telah berhasil");
                i.setData(Uri.parse("mailto:"+email));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                    finish();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }


            }
            else {
                //if qr contains data
                try {
                    llSearch.setVisibility(View.VISIBLE);
                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());
                    //setting values to textviews
                    textViewName.setText(obj.getString("nama"));
                    textViewKelas.setText(obj.getString("kelas"));
                    textViewAddress.setText(obj.getString("nim"));
                } catch (JSONException e) {
                    llSearch.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                    //if control comes here
                    //that means the encoded format not match
                    //in this case you can display whatever data is available on the qrcoce
                    //to a toast


                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
                String lokasiku = String.valueOf(result.getContents());

                // Buat Uri dari intent string. Gunakan hasilnya ugntuk membuat Intent.
                gmmIntentUri = Uri.parse(lokasiku);

                // Buat Uri dari intent gmmIntentUri. Set action => ACTION_VIEW
                mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                // Set package Google Maps untuk tujuan aplikasi yang di Intent yaitu google maps
                mapIntent.setPackage(goolgeMap);

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }


            }

        }
        else {

            super.onActivityResult(requestCode, resultCode, data);

        }

    }



    @Override
    public void onClick(View view) {
        //inisialisaasi the qr code scan
        qrScan.initiateScan();

    }
}