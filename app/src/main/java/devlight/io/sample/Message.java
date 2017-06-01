package devlight.io.sample;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.Manifest;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Created by zeus on 2017/5/27.
 */

public class Message extends Activity implements Button.OnClickListener {
    private RecyclerView recyle;
    private StorageReference mStorageRef;
    private int count;
    private Intent i;
    private UUID uuid;
    private String group_name;
    private String[] type;
    private String[] message;
    private String[] who_message;
    private Double[] lat;
    private Double[] lng;
    private int member_count;
    private Button help_button;
    private Button map_button;
    private Button send_button;
    private Button file_button;
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                String imgPath = getPath(Message.this, uri);
                if (imgPath != null && !imgPath.equals("")) {
                    uuid = UUID.randomUUID();
                    //Toast.makeText(Message.this, uri.getLastPathSegment()+"    "+imgPath, Toast.LENGTH_SHORT).show();
                    StorageReference riversRef = mStorageRef.child(uuid.toString());
                    UploadTask uploadTask = riversRef.putFile(Uri.fromFile(new File(imgPath)));
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(Message.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("Fail", exception.getMessage());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(Message.this, "success", Toast.LENGTH_SHORT).show();
                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d("downloadURL", downloadUrl.toString());
                            text_message temp = new text_message(downloadUrl.toString(), user.getUid().toString(), "image");
                            myRef.child("group").child(group_name).child("message").child(String.valueOf(count)).setValue(temp);
                        }
                    });
                } else {
                    Toast.makeText(Message.this, R.string.load_img_fail, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initUI() {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        send_button = (Button) findViewById(R.id.send_text);
        send_button.setOnClickListener(this);
        send_text = (TextView) findViewById(R.id.input_text);
        send_text.setSelected(false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        file_button = (Button) findViewById(R.id.button_file);
        file_button.setOnClickListener(this);
        map_button = (Button) findViewById(R.id.map_button);
        map_button.setOnClickListener(this);
        help_button=(Button) findViewById(R.id.button_help);
        help_button.setOnClickListener(this);
    }


    public void setDataBase() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                String token;
                count = (int) dataSnapshot.child("group").child(group_name).child("message").getChildrenCount();
                Log.d("message_count", String.valueOf(count));
                type = new String[count];
                message = new String[count];
                who_message = new String[count];
                for (DataSnapshot text : dataSnapshot.child("group").child(group_name).child("message").getChildren()) {
                    if (text.child("text").getValue() != null) {
                        message[i] = text.child("text").getValue().toString();
                        type[i] = text.child("type").getValue().toString();
                    }
                    token = text.child("who").getValue().toString();
                    who_message[i] = dataSnapshot.child("member").child(token).child("name").getValue().toString();
                    i++;
                }
                recyle.swapAdapter(new RecycleAdapter(), false);
                recyle.smoothScrollToPosition(count);
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
                text_message temp = new text_message(send_text.getText().toString(), user.getUid().toString(), "text");
                Log.d("in the button", "button");
                myRef.child("group").child(group_name).child("message").child(String.valueOf(count)).setValue(temp);
                send_text.setText("");
            }
        } else if (v.getId() == file_button.getId()) {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 無權限，向使用者請求
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            getLocalImg();
        } else if (v.getId() == map_button.getId()) {
            i = new Intent(Message.this,googlemap.class);
            i.putExtra("group",group_name);
            startActivity(i);
        } else if(v.getId()==help_button.getId()){
            post();
        }
    }

    public void post() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://tamama.com.tw/project";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("response",response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("response","error");
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                Log.d("group",group_name);
                Log.d("token",user.getUid());
                params.put("group", group_name);
                params.put("token", user.getUid());

                return params;
            }

        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    public static class text_message {
        public String text;
        public String type;
        public String who;

        public text_message() {

        }

        public text_message(String text, String token, String type) {
            this.text = text;
            this.who = token;
            this.type = type;
        }
    }

    private void getLocalImg() {
        Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
        picker.setType("image/*");
        picker.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        Intent destIntent = Intent.createChooser(picker, null);
        startActivityForResult(destIntent, 1);
    }

    public class RecycleAdapter extends RecyclerView.Adapter<Message.RecycleAdapter.ViewHolder> {

        @Override
        public Message.RecycleAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_message, parent, false);
            return new Message.RecycleAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final Message.RecycleAdapter.ViewHolder holder, final int position) {
            Log.d("message", message[position]);
            Log.d("who", who_message[position]);
            Log.d("type", type[position]);
            if (type[position].equals("image")) {
                holder.txt_who.setText(who_message[position]);
                Glide.with(Message.this)
                        .load(message[position])
                        .into(holder.txt_image);
                holder.txt.setText("");
                Log.d("image success", "image");
            } else if (type[position].equals("text")) {
                holder.txt.setText(message[position]);
                holder.txt_who.setText(who_message[position]);
                holder.txt_image.setImageDrawable(null);
            }

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

    //get the file path
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    //permission
}
