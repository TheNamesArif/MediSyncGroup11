package com.example.medisync.auth;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.medisync.R;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText email, oldPassword, newPassword, confirmPassword;
    ImageView toggleOldPassword, toggleNewPassword, toggleConfirmPassword;
    Button changeBtn, backBtn;
    FirebaseAuth mAuth;

    boolean isOldPasswordVisible = false;
    boolean isNewPasswordVisible = false;
    boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        email = findViewById(R.id.email);
        oldPassword = findViewById(R.id.old_password);
        newPassword = findViewById(R.id.new_password);
        confirmPassword = findViewById(R.id.confirm_password);
        changeBtn = findViewById(R.id.change_passwordBtn);
        toggleOldPassword = findViewById(R.id.toggleOldPassword);
        toggleNewPassword = findViewById(R.id.toggleNewPassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);
        backBtn = findViewById(R.id.backBtn);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        //Autofill email section
        String emailAutofill = user.getEmail();
        email.setText(emailAutofill);

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

            user.reauthenticate(
                    EmailAuthProvider.getCredential(emailStr, oldPass)
            ).addOnSuccessListener(aVoid -> {

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
                    Toast.makeText(this, "Old password incorrect", Toast.LENGTH_LONG).show()
            );
        });

        toggleOldPassword.setOnClickListener(v -> {
            isOldPasswordVisible = !isOldPasswordVisible;
            oldPassword.setInputType(isOldPasswordVisible
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            oldPassword.setSelection(oldPassword.getText().length());
            toggleOldPassword.setColorFilter(ContextCompat.getColor(this,
                    isOldPasswordVisible ? R.color.colorPrimary : R.color.gray));
        });

        toggleNewPassword.setOnClickListener(v -> {
            isNewPasswordVisible = !isNewPasswordVisible;
            newPassword.setInputType(isNewPasswordVisible
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            newPassword.setSelection(newPassword.getText().length());
            toggleNewPassword.setColorFilter(ContextCompat.getColor(this,
                    isNewPasswordVisible ? R.color.colorPrimary : R.color.gray));
        });

        toggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            confirmPassword.setInputType(isConfirmPasswordVisible
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPassword.setSelection(confirmPassword.getText().length());
            toggleConfirmPassword.setColorFilter(ContextCompat.getColor(this,
                    isConfirmPasswordVisible ? R.color.colorPrimary : R.color.gray));
        });

        // Go to previous page
        backBtn.setOnClickListener(v -> finish());
    }
}