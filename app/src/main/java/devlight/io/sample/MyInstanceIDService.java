package devlight.io.sample;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by zeus on 2017/5/16.
 */

public class MyInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d("FCM", "Token:"+token);
    }

}
