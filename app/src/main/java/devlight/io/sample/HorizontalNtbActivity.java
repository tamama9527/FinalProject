package devlight.io.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("member");
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
                return 5;
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

                        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(
                                        getBaseContext(), LinearLayoutManager.VERTICAL, false
                                )
                        );
                        recyclerView.setAdapter(new RecycleAdapter());
                        container.addView(view);
                        return view;
                    case 1:
                        txtPage.setText(String.format("Page #%d", position));
                        txtPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAuth.signOut();
                                HorizontalNtbActivity.this.finish();
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
                        getResources().getDrawable(R.drawable.ic_friend),
                        Color.parseColor(colors[0]))
                        //.selectedIcon(getResources().getDrawable(R.drawable.ic_sixth))
                        .title("Heart")
                        .badgeTitle("NTB")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[1]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("Cup")
                        .badgeTitle("with")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_third),
                        Color.parseColor(colors[2]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_seventh))
                        .title("Diploma")
                        .badgeTitle("state")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fourth),
                        Color.parseColor(colors[3]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("Flag")
                        .badgeTitle("icon")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fifth),
                        Color.parseColor(colors[4]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("Medal")
                        .badgeTitle("777")
                        .build()
        );

        navigationTabBar.setModels(models);
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
    public void SetDataBase(){
        count=getIntent().getExtras().getInt("count");
        user= FirebaseAuth.getInstance().getCurrentUser();
        Log.d("number of count",String.valueOf(count));
        temp=new String[count];
        name=new String[count];
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            int i=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot friendshot:dataSnapshot.child(user.getUid()).child("friend").getChildren()){
                    temp[i]=friendshot.getValue().toString();
                    Log.d("number of i",String.valueOf(i));
                    Log.d("friend",friendshot.getValue().toString());
                    i++;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(int i=0;i<count;i++){
                    name[i]=dataSnapshot.child(temp[i]).child("name").getValue().toString();
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
