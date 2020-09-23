package com.kirti.videoeditor;

import android.app.ProgressDialog;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
