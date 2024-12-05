package edu.uga.cs.roomieslist;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * This class handles the login process of a user
 */
public class LoginActivity extends AppCompatActivity {

    // Variables
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signUpLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Obtain object views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpLink = findViewById(R.id.signUpLink);

        // If login button clicked, then go into the account
        loginButton.setOnClickListener(v -> loginUser());

        // If no account then go to SignupActivity to create an account
        signUpLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    /**
     * Handles the user login.
     * Makes sures that user information is authenticated
     */
    private void loginUser() {
        // Get email and password
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Make sure that the email and password were entered
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please you must enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate user using Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // If login is successful, then go to shopping list
                        navigateToShoppingList();
                    } else {
                        // If login failed, then try again.
                        Toast.makeText(LoginActivity.this, "Authentication failed. Try again!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * If the user is authenticated then take them to the shopping list activity
     */
    private void navigateToShoppingList() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Make sure that the user was logged in
        if (currentUser != null) {
            // Get the users id
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            userRef.child("groupId").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String groupId = snapshot.getValue(String.class);
                        if (groupId != null) {
                            // If the group id was found, then take the user to the shopping list
                            Intent intent = new Intent(LoginActivity.this, ShoppingListActivity.class);
                            intent.putExtra("GROUP_ID", groupId);
                            startActivity(intent);
                            finish();
                        } else {
                            // If groupId is null
                            Toast.makeText(LoginActivity.this, "Group ID was not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Group ID does not exist for this user.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Failed to retrieve Group ID.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}