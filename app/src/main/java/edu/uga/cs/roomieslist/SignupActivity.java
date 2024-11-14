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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "SignupActivity";
    private EditText nameEditText, emailEditText, passwordEditText, groupIdEditText;
    private Button signUpButton;
    private TextView loginLink;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

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

    // Handle user registration
    private void registerUser() {
        String name = nameEditText.getText().toString().trim(); // remove the trim()
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String groupId = groupIdEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || groupId.isEmpty()) {
            Toast.makeText(this, "All fields need to be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // This is how we can create a new user using an email/password combination.
        // Note that we also add an onComplete listener, which will be invoked once
        // a new user has been created by Firebase.  This is how we will know the
        // new user creation succeeded or failed.
        // If a new user has been created, Firebase already signs in the new user;
        // no separate sign in is needed.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            User user = new User(name, email, groupId);
                            databaseReference.child(userId).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(SignupActivity.this, "Registration successful for : " + email, Toast.LENGTH_SHORT).show();
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(DEBUG_TAG, "createUserWithEmail: success");
                                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Log.w(DEBUG_TAG, "saveUserRegistration: failure", dbTask.getException());
                                            Toast.makeText(SignupActivity.this, "Failed to save user registration.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(DEBUG_TAG, "createUserWithEmail: failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}