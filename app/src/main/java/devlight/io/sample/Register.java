package devlight.io.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;


import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Printer;
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


public class Register extends Activity implements Button.OnClickListener {
    private FirebaseAuth mAuth;
    private Button re_button;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("member");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);
        mAuth = FirebaseAuth.getInstance();
        re_button = (Button) findViewById(R.id.Register_button);
        re_button.setOnClickListener(this);

    }

    @Override
    public void onClick(View V) {
        final String[] result = new String[1];
        final EditText re_nickname = (EditText) findViewById(R.id.Register_nickname);
        final EditText re_account = (EditText) findViewById(R.id.Register_Account);
        final EditText re_password = (EditText) findViewById(R.id.Register_Password);
        EditText re_check = (EditText) findViewById(R.id.Register_Check);
        if (re_password.getText().toString().compareTo(re_check.getText().toString()) != 0) {
        } else {
            mAuth.createUserWithEmailAndPassword(re_account.getText().toString(), re_password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Log.d("帳號註冊失敗", task.getException().toString());
                            } else {
                                final FirebaseUser currentUser = mAuth.getCurrentUser();
                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.hasChild(currentUser.getUid())) {
                                            myRef.child(currentUser.getUid()).child("name").setValue(re_nickname.getText().toString());
                                        }
                                        Register.this.finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                            }
                        }
                    });
        }
    }
}
