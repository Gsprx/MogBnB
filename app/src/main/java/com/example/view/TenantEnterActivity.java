package com.example.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mogbnb.R;

public class TenantEnterActivity extends AppCompatActivity {

    EditText usernameET;
    EditText idET;
    Button beginBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_enter);

        usernameET = findViewById(R.id.tenant_welcome_username);
        idET = findViewById(R.id.tenant_welcome_id);
        beginBtn = findViewById(R.id.tenant_welcome_beginBtn);

        beginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usernameET.getText().toString().equals("") || idET.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter your details", Toast.LENGTH_SHORT).show();
                    return;
                }

                String username = usernameET.getText().toString();
                String id = idET.getText().toString();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("ID", id);
                startActivity(intent);

            }
        });

    }
}
