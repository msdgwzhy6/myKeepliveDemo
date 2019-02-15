package example.luoling.com.keepliveprocess;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telecom.*;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Administrator on 2019/2/14.
 */

public class LocalService extends Service {

    private String TAG = "luoling";
    private MyBinder binder;
    private MyServiceConnection connection;

    @Override
    public void onCreate() {
        super.onCreate();
        if (binder == null){
            binder = new MyBinder();
        }
        connection = new MyServiceConnection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocalService.this.bindService(new Intent(LocalService.this,RemoteService.class),connection,Context.BIND_IMPORTANT);

        PendingIntent contentIntent = PendingIntent.getService(this, 0,intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"localServiceId");
        builder.setTicker("360")
                .setContentIntent(contentIntent)
                .setContentTitle("我是360，我怕谁！")
                .setAutoCancel(true)
                .setContentText("hehehe")
                .setWhen(System.currentTimeMillis());

        //把service设置为前台运行，避免手机系统自动杀掉改服务。
        startForeground(startId,builder.build());
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class MyBinder extends RemoteConnection.Stub{

        @Override
        public String getProcessName() throws RemoteException {
            return "LocalService";
        }
    }

    class MyServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG,"与RemoteService建立连接成功！");
            try {
                RemoteConnection remoteConnection = RemoteConnection.Stub.asInterface(service);
                remoteConnection.getProcessName();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "RemoteService服务被干掉了~~~~断开连接！");
            Toast.makeText(LocalService.this, "断开连接", Toast.LENGTH_SHORT).show();

            LocalService.this.startService(new Intent(LocalService.this,RemoteService.class));
            LocalService.this.bindService(new Intent(LocalService.this,RemoteService.class),connection, Context.BIND_IMPORTANT);
        }
    }

}