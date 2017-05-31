package devlight.io.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import devlight.io.library.ntb.NavigationTabBar;

import java.util.ArrayList;

/**
 * Created by GIGAMOLE on 28.03.2016.
 */
public class HorizontalNtbActivity extends Activity implements Button.OnClickListener {
    private String[] temp;
    private String[] name;
    private FirebaseUser user;
    private RadioButton new_group;
    private RadioButton friend;
    private RecyclerView recyclerView;
    private RadioButton group;
    private Button add_friend;
    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private int count;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_ntb);
        mAuth = FirebaseAuth.getInstance();
        SetDataBase();
        initUI();
    }

    private void initUI() {
        Log.d("initUI_count", String.valueOf(count));
        final ViewPager viewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                View view = LayoutInflater.from(
                        getBaseContext()).inflate(R.layout.item_vp, null, false);
                final TextView txtPage = (TextView) view.findViewById(R.id.txt_vp_item_page);
                switch (position) {
                    case 0:
                        view = LayoutInflater.from(
                                getBaseContext()).inflate(R.layout.item_vp_list, null, false);

                        recyclerView = (RecyclerView) view.findViewById(R.id.rv);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(
                                        getBaseContext(), LinearLayoutManager.VERTICAL, false
                                )
                        );
                        recyclerView.setAdapter(new RecycleAdapter());
                        break;
                    case 1:
                        view = LayoutInflater.from(getBaseContext()).inflate(R.layout.add_friend, null, false);
                        add_friend = (Button) view.findViewById(R.id.button_add);
                        final EditText add_text = (EditText) view.findViewById(R.id.add_input);
                        final View finalView = view;
                        friend = (RadioButton) finalView.findViewById(R.id.radioButton);
                        group = (RadioButton) finalView.findViewById(R.id.radioButton2);
                        new_group = (RadioButton) finalView.findViewById(R.id.radioButton3);
                        setRatioButton();
                        add_friend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    int group_count;
                                    int friend_count;
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (friend.isChecked()) {
                                            friend_count = (int) dataSnapshot.child("member").child(user.getUid()).child("friend").getChildrenCount();
                                            for (DataSnapshot friend_token : dataSnapshot.child("member").getChildren()) {
                                                if (friend_token.child("name").getValue().toString().equals(add_text.getText().toString())) {
                                                    myRef.child("member").child(user.getUid()).child("friend").child(String.valueOf(friend_count)).setValue(friend_token.getKey());
                                                    add_text.setText("");
                                                    Toast.makeText(HorizontalNtbActivity.this, "已成功加入好友", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            recyclerView.setAdapter(new RecycleAdapter());
                                        } else if (group.isChecked()) {
                                            if(dataSnapshot.child("group").child(add_text.getText().toString()).exists()) {
                                                group_count = (int) dataSnapshot.child("group").child(add_text.getText().toString()).child("member").getChildrenCount();
                                                myRef.child("group").child(add_text.getText().toString()).child("member").child(String.valueOf(group_count)).setValue(user.getUid());
                                                myRef.child("member").child(user.getUid()).child("group").child(String.valueOf(dataSnapshot.child("member").child(user.getUid()).child("group").getChildrenCount())).setValue(add_text.getText().toString());
                                                Toast.makeText(HorizontalNtbActivity.this, "已成功加入群組", Toast.LENGTH_SHORT).show();
                                                recyclerView.setAdapter(new RecycleAdapter());
                                            }
                                            else{
                                                Toast.makeText(HorizontalNtbActivity.this, "群組不存在", Toast.LENGTH_SHORT).show();
                                            }
                                        } else if (new_group.isChecked()) {
                                            if(!dataSnapshot.child("group").child(add_text.getText().toString()).exists()) {
                                                group_count = (int) dataSnapshot.child("group").child(add_text.getText().toString()).child("member").getChildrenCount();
                                                myRef.child("group").child(add_text.getText().toString()).child("member").child(String.valueOf(group_count)).setValue(user.getUid());
                                                myRef.child("member").child(user.getUid()).child("group").child(String.valueOf(dataSnapshot.child("member").child(user.getUid()).child("group").getChildrenCount())).setValue(add_text.getText().toString());
                                                Toast.makeText(HorizontalNtbActivity.this, "已成功新增群組", Toast.LENGTH_SHORT).show();
                                                recyclerView.setAdapter(new RecycleAdapter());
                                            }
                                            else{
                                                Toast.makeText(HorizontalNtbActivity.this, "已有相同名稱的群組", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                        break;

                    default:
                        txtPage.setText(String.format("Page #%d", position));
                        txtPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(
                                        new Intent(HorizontalNtbActivity.this, googlemap.class)
                                );
                            }
                        });
                        break;
                }
                container.addView(view);
                return view;
            }
        });

        final String[] colors = getResources().getStringArray(R.array.vertical_ntb);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.message),
                        Color.parseColor(colors[0]))
                        //.selectedIcon(getResources().getDrawable(R.drawable.ic_sixth))
                        .title("Message")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.add_friend),
                        Color.parseColor(colors[2]))
                        .title("Add")
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setIsBadged(false);
        navigationTabBar.setBgColor(Color.rgb(36, 68, 132));
        navigationTabBar.setViewPager(viewPager, 2);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                navigationTabBar.getModels().get(position).hideBadge();
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });

        navigationTabBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                    navigationTabBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model.showBadge();
                        }
                    }, i * 100);
                }
            }
        }, 500);
    }

    @Override
    public void onClick(View v) {

    }

    public void setRatioButton() {

        new_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_friend.setText("新增");
                group.setChecked(false);
                friend.setChecked(false);
            }
        });
        friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_friend.setText("加入");
                group.setChecked(false);
                new_group.setChecked(false);
            }
        });
        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_friend.setText("加入");
                new_group.setChecked(false);
                friend.setChecked(false);
            }
        });
    }

    public void SetDataBase() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("number of count", String.valueOf(count));

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                count = (int) dataSnapshot.child("member").child(user.getUid()).child("group").getChildrenCount();
                temp = new String[count];
                name = new String[count];
                for (DataSnapshot groupshot : dataSnapshot.child("member").child(user.getUid()).child("group").getChildren()) {
                    name[i] = groupshot.getValue().toString();
                    i++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public class RecycleAdapter extends RecyclerView.Adapter<HorizontalNtbActivity.RecycleAdapter.ViewHolder> {

        @Override
        public HorizontalNtbActivity.RecycleAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_list, parent, false);
            return new HorizontalNtbActivity.RecycleAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final HorizontalNtbActivity.RecycleAdapter.ViewHolder holder, final int position) {
            holder.txt.setText(name[position]);
            holder.txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i;
                    i = new Intent(HorizontalNtbActivity.this, Message.class);
                    i.putExtra("group", name[position]);
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            Log.d("item_count", String.valueOf(count));
            return count;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView txt;

            public ViewHolder(final View itemView) {
                super(itemView);
                txt = (TextView) itemView.findViewById(R.id.txt_vp_item_list);
            }
        }
    }
}
