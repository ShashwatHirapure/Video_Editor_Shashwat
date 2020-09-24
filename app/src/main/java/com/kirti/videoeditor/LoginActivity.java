package com.kirti.videoeditor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity {
    public String mobile;
    public EditText et_mobile;
    public FancyButton bt_login;
    ProgressDialog pd;
    String[] permissionArrays = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    int REQUEST_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionArrays, REQUEST_CODE );
        } else {
           start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean openActivityOnce = true;
        boolean openDialogOnce = true;
        if (requestCode == REQUEST_CODE ) {
            for (int i = 0; i < grantResults.length; i++) {
                String permission = permissions[i];
                 boolean isPermitted = grantResults[i] == PackageManager.PERMISSION_GRANTED;

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissionArrays, REQUEST_CODE );
                    }

                }else {
                    start();
                }
            }
        }
    }
    public void start(){
        pd = new ProgressDialog(LoginActivity.this);
        pd.setCancelable(false);
        pd.setMessage("Wait....");
        et_mobile = findViewById(R.id.et_mobno);
        bt_login = findViewById(R.id.bt_login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                mobile = et_mobile.getText().toString();
                if (mobile.length() == 10) {
                    pd.dismiss();
                    startActivity(new Intent(LoginActivity.this, VerifyPhone.class).putExtra("mob", mobile));
                } else {
                    pd.dismiss();
                    et_mobile.setError("Incorrect Mobile Number");
                    et_mobile.requestFocus();
                }
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, Main.class));
            finish();
        }

    }
}
