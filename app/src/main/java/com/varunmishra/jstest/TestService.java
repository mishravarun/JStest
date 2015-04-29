package com.varunmishra.jstest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;
import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;

public class TestService extends Service {
    private PowerManager.WakeLock mWakeLock = null;
    String TAG="Test";
    static boolean isRunning=true;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager manager =
                (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);


        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive(" + intent + ")");

            if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    Log.i(TAG, "Runnable executing.");
                }
            };

            new Handler().postDelayed(runnable, 500);
        }
    };
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        Intent intent1 = new Intent(this, MyActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("JS Running")
                .setContentText("Sampling")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)

                .build();





        startForeground(Process.myPid(), n);

        mWakeLock.acquire();

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
    public void calc(final Context con, final int iteration){
        isRunning=true;
        JsEvaluator jsEvaluator = new JsEvaluator(con);
        jsEvaluator.callFunction("function myFunction(a, b, c, d) { var x = val(a,b);return x;}" +
                        "function val(c,d){" +
                        "for(i=0;i<100;i++){" +
                        " for(j=0;j<9999999;j++);}" +
                        "return i;}",
                new JsCallback() {

                    @Override
                    public void onResult(final String result) {
                        // get result here
                        new MyActivity().refresh(""+iteration);
                        if(iteration==3){
                            isRunning=false;

                            stopSelf();
                        }
                    }
                }, "myFunction", "parameter 1", "parameter 2", 912, 101.3);

    }
}