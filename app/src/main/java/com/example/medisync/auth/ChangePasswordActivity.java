package com.example.medisync.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText email, oldPassword, newPassword, confirmPassword;
    Button changeBtn;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        email = findViewById(R.id.email);
        oldPassword = findViewById(R.id.old_password);
        newPassword = findViewById(R.id.new_password);
        confirmPassword = findViewById(R.id.confirm_password);
        changeBtn = findViewById(R.id.change_passwordBtn);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        changeBtn.setOnClickListener(v -> {

            String emailStr = email.getText().toString().trim();
            String oldPass = oldPassword.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(emailStr) ||
                    TextUtils.isEmpty(oldPass) ||
                    TextUtils.isEmpty(newPass) ||
                    TextUtils.isEmpty(confirmPass)) {

                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            // STEP 1: Re-authenticate user
            user.reauthenticate(
                    EmailAuthProvider.getCredential(emailStr, oldPass)
            ).addOnSuccessListener(aVoid -> {

                // STEP 2: Update password
                user.updatePassword(newPass)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(this,
                                        "Password updated successfully",
                                        Toast.LENGTH_LONG).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        "Update failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );

            }).addOnFailureListener(e ->
                    Toast.makeText(this,
                            "Old password incorrect",
                            Toast.LENGTH_LONG).show()
            );
        });
    }
}