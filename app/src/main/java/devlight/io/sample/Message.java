package devlight.io.sample;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by zeus on 2017/5/27.
 */

public class Message extends Activity implements Button.OnClickListener {
    private RecyclerView recyle;
    private int count;
    private String group_name;
    private String[] message;
    private String[] who_message;
    private Button send_button;
    private TextView send_text;
    private LinearLayoutManager ReManager;
    private FirebaseUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        group_name = getIntent().getExtras().getString("group");
        myRef = database.getReference();
        setContentView(R.layout.activity_message);
        setDataBase();
        recyle = (RecyclerView) findViewById(R.id.message_rv);
        ReManager = new LinearLayoutManager(this);
        ReManager.setStackFromEnd(true);
        recyle.setLayoutManager(ReManager);
        recyle.setAdapter(new RecycleAdapter());
        initUI();
    }

    private void initUI() {
        send_button = (Button) findViewById(R.id.send_text);
        send_button.setOnClickListener(this);
        send_text = (TextView) findViewById(R.id.input_text);
        send_text.setSelected(false);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }


    public void setDataBase() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                String token;
                count = (int) dataSnapshot.child("group").child(group_name).child("message").getChildrenCount();
                Log.d("message_count", String.valueOf(count));
                message = new String[count];
                who_message = new String[count];
                for (DataSnapshot text : dataSnapshot.child("group").child(group_name).child("message").getChildren()) {
                    if (text.child("text").getValue() != null) {
                        message[i] = text.child("text").getValue().toString();
                    }
                    token = text.child("who").getValue().toString();
                    who_message[i] = dataSnapshot.child("member").child(token).child("name").getValue().toString();
                    Log.d("message", message[i]);
                    Log.d("who", who_message[i]);
                    i++;
                }
                recyle.swapAdapter(new RecycleAdapter(), false);
                recyle.smoothScrollToPosition(count - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.d("Database", "success");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == send_button.getId()) {
            if (send_text.getText().toString().trim().length() > 0) {
                text_message temp = new text_message(send_text.getText().toString(), user.getUid().toString());
                Log.d("in the button", "button");
                myRef.child("group").child(group_name).child("message").child(String.valueOf(count)).setValue(temp);
                send_text.setText("");
            }
        }
    }

    public static class text_message {
        public String text;
        public String type;
        public String who;
        public text_message() {

        }

        public text_message(String text, String token) {
            this.text = text;
            this.who = token;
            this.type = "text";
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<Message.RecycleAdapter.ViewHolder> {

        @Override
        public Message.RecycleAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_message, parent, false);
            return new Message.RecycleAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final Message.RecycleAdapter.ViewHolder holder, final int position) {
            if (position == 6) {
                holder.txt_who.setText(who_message[position]);
                holder.txt_image.setImageResource(R.mipmap.dahyun_0033);
                holder.txt.setText("");
            }
            else{
                holder.txt.setText(message[position]);
                holder.txt_who.setText(who_message[position]);
                holder.txt_image.setImageDrawable(null);
            }
            Log.d("setText", message[position]);
            Log.d("who_Text", who_message[position]);
        }

        @Override
        public int getItemCount() {
            Log.d("message_count", String.valueOf(count));
            return count;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView txt_who;
            public TextView txt;
            public ImageView txt_image;

            public ViewHolder(final View itemView) {
                super(itemView);
                txt = (TextView) itemView.findViewById(R.id.text_message);
                txt_who = (TextView) itemView.findViewById(R.id.text_who);
                txt_image = (ImageView) itemView.findViewById(R.id.text_image);
            }
        }
    }
}
