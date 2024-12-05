package edu.uga.cs.roomieslist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * This class handles when a user wants to create an account
 */
public class SignupActivity extends AppCompatActivity {

    // Variables
    private static final String DEBUG_TAG = "SignupActivity";
    private EditText nameEditText, emailEditText, passwordEditText, groupIdEditText;
    private Button signUpButton;
    private TextView loginLink;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    /**
     * It is initialize when the Activity is first created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Obtain object Views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        groupIdEditText = findViewById(R.id.groupIdEditText);
        signUpButton = findViewById(R.id.signUpButton);
        loginLink = findViewById(R.id.loginLink);

        // Sign-Up button functionality
        signUpButton.setOnClickListener(v -> registerUser());

        // Navigate to Login page
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });
    }

    /**
     * Handle registration
     */
    private void registerUser() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String groupId = groupIdEditText.getText().toString();

        // Check if all fields are filled
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || groupId.isEmpty()) {
            Toast.makeText(this, "All fields need to be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Checks if the password is less than 6 characters
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user using email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Update the user's display name
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name) // Set the user's name
                                        .build();

                                firebaseUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            if (profileTask.isSuccessful()) {
                                                Log.d(DEBUG_TAG, "User profile updated with name: " + name);
                                            } else {
                                                Log.w(DEBUG_TAG, "Failed to update user profile", profileTask.getException());
                                            }
                                        });

                                // Save user details to the Realtime Database
                                String userId = firebaseUser.getUid();
                                User user = new User(name, email, groupId);
                                databaseReference.child(userId).setValue(user)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(SignupActivity.this, "Registration successful for: " + email, Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Log.w(DEBUG_TAG, "Failed to save user details", dbTask.getException());
                                                Toast.makeText(SignupActivity.this, "Failed to save user details.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(DEBUG_TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}