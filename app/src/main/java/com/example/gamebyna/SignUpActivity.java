package com.example.gamebyna;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    EditText etUser, etPass, etConfirm;
    Button btnRegister;
    koneksi db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUser = findViewById(R.id.usersign);
        etPass = findViewById(R.id.passign);
        etConfirm = findViewById(R.id.consign);
        btnRegister = findViewById(R.id.btnsign);
        db = new koneksi(this);

        btnRegister.setOnClickListener(v -> {
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();
            String c = etConfirm.getText().toString().trim();

            if (u.isEmpty() || p.isEmpty() || c.isEmpty()) {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show();
            } else if (!p.equals(c)) {
                Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show();
            } else if (db.checkUsername(u)) {
                Toast.makeText(this, "Username sudah terdaftar", Toast.LENGTH_SHORT).show();
            } else {
                if (db.addUser(u, p, c)) {
                    Toast.makeText(this, "Berhasil mendaftar", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Gagal mendaftar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
