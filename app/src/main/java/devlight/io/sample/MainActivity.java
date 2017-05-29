package devlight.io.sample;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;


import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


public class MainActivity extends Activity implements Button.OnClickListener {
    private MyInstanceIDService mID;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button login_button;
    private TextView Register;
    private EditText account;
    private EditText password;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("member");
    private int Friend_count;
    private Intent i;
    private FirebaseUser user;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        initUI();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    userId=user.getUid();
                    myRef.child(userId).child("token").setValue(FirebaseInstanceId.getInstance().getToken());
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Friend_count= (int) dataSnapshot.child(userId).child("friend").getChildrenCount();
                            Log.d("Friend_count",String.valueOf(Friend_count));
                            i = new Intent(MainActivity.this, HorizontalNtbActivity.class);
                            // User is signed in
                            i.putExtra("count",Friend_count);
                            startActivity(i);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void initUI() {
        login_button = (Button) findViewById(R.id.Login_button);
        login_button.setOnClickListener(this);
        Register = (TextView) findViewById(R.id.register);
        Register.setOnClickListener(this);
        password = (EditText) findViewById(R.id.Password);
        account = (EditText) findViewById(R.id.Account);
    }

    @Override
    public void onClick(View V) {

        if (V.getId() == login_button.getId()) {
            login();
        } else if (V.getId() == Register.getId()) {
            register();
        }
    }

    public void register() {
        startActivity(
                new Intent(MainActivity.this, Register.class)
        );
    }

    public void login() {

        mAuth.signInWithEmailAndPassword(account.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d("onComplete", "登入失敗");
                        }
                    }
                });
    }
}