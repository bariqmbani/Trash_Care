package com.unpad.trashcare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.unpad.trashcare.models.Warga;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";

    private Button btnLogin;
    EditText etUsername, etPassword;
    private DatabaseReference databaseReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static  final  String COLLECTION_NAME_KEY = "warga";
    public static  final  String USERNAME_KEY = "username";
    public static  final  String PASSWORD_KEY = "password";
    public static final String NUMBER_KEY = "phoneNo:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.inputUsername);
        etPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("warga");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (!etUsername.getText().toString().equals("") && !etPassword.getText().toString().equals("")) {
                        DocumentReference docRef = db.collection(COLLECTION_NAME_KEY).document(etUsername.getText().toString());
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    Warga user = documentSnapshot.toObject(Warga.class);
                                    ((UserClient)(getApplicationContext())).setWarga(user);
                                    if (user.getPassword().equals(etPassword.getText().toString())) {
                                        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "signIn: success");
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    wargaToken();
                                                    Toast.makeText(getApplicationContext(), "welcome", Toast.LENGTH_SHORT).show();
                                                    Intent it = new Intent(LoginActivity.this,MainActivity.class);
                                                    it.putExtra("ID",etUsername.getText().toString());
                                                    startActivity(it);
                                                }
                                            }
                                        });

                                    } else {
                                        Toast.makeText(LoginActivity.this, "Passsword Mismatching", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Check your Username ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "Username or Password Cannot be Empty", Toast.LENGTH_SHORT).show();
                    }

                }
        });
    }

    private void wargaToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.d(TAG, "Token warga: " + token);

                        Map<String, Object> userId = new HashMap<>();
                        userId.put("uid", mAuth.getInstance().getUid());
                        userId.put("id_warga", etUsername.getText().toString());

                        db.collection("Token Warga").document(token).set(userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "token succesfully writed to firestore");
                                }
                            }
                        });

                    }
                });
    }

}
