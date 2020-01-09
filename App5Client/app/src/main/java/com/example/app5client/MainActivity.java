package com.example.app5client;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.common.IMyAidlInterface;

public class MainActivity extends AppCompatActivity {

    private boolean mIsBound = false;
    private static final String START_FOREGROUND_SERVICE = "start foreground service";
    private static final String STOP_FOREGROUND_ACTION = "stop foreground service";
    private static final String UNBIND = "unbind";

    int selectedSong = 1;
    private IMyAidlInterface mMusicService;
    boolean isMusicStarted = false;
    boolean mIsStarted = false;
    IntentFilter mFilter;
    BrdcstRcvr mReceiver;

    Button startServiceButton;
    Button playSongButton;
    Button pauseSongButton;
    Button resumeSongButton;
    Button stopSongButton;
    Button stopServiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Stop song")
                .setTitle("Song will stop playing");
        builder.setPositiveButton("OK",null);
        builder.setCancelable(true);
        final AlertDialog alert11 = builder.create();

        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"Song 1", "Song 2", "Song 3", "Song 4", "Song 5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        startServiceButton = findViewById(R.id.button);
        playSongButton = findViewById(R.id.button2);
        pauseSongButton = findViewById(R.id.button3);
        resumeSongButton = findViewById(R.id.button4);
        stopSongButton = findViewById(R.id.button5);
        stopServiceButton = findViewById(R.id.button6);

        playSongButton.setEnabled(false);
        pauseSongButton.setEnabled(false);
        resumeSongButton.setEnabled(false);
        stopSongButton.setEnabled(false);
        stopServiceButton.setEnabled(false);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSong = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View src) {

                Intent startIntent = new Intent(IMyAidlInterface.class.getName());
                ResolveInfo info = getPackageManager().resolveService(startIntent, 0);
                startIntent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
                startIntent.setAction(START_FOREGROUND_SERVICE);
                startForegroundService(startIntent);

                mIsStarted = true;

                startServiceButton.setEnabled(false);
                playSongButton.setEnabled(true);    //if misBound??
                stopServiceButton.setEnabled(true);
            }
        });

        playSongButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View src) {
                if(mIsBound) {
                    try {
                        mMusicService.playSong(selectedSong);
                        isMusicStarted = true;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    playSongButton.setEnabled(false);
                    pauseSongButton.setEnabled(true);
                    stopSongButton.setEnabled(true);
                }
            }
        });

        pauseSongButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View src) {
                try {
                    mMusicService.pauseSong();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                pauseSongButton.setEnabled(false);
                resumeSongButton.setEnabled(true);
            }
        });

        resumeSongButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View src) {
                try {
                    mMusicService.resumeSong();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                resumeSongButton.setEnabled(false);
                pauseSongButton.setEnabled(true);

             }
        });

        stopSongButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View src) {
                try {
                    mMusicService.stopSong();
                    if (mIsBound) {
                        unbindService(mConnection);
                        mIsBound = false;
                        isMusicStarted = false;

                        stopSongButton.setEnabled(false);
                        resumeSongButton.setEnabled(false);
                        pauseSongButton.setEnabled(false);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View src) {
                try {
                    if(isMusicStarted){
                        alert11.show();
                        mMusicService.stopSong();
                        isMusicStarted = false;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if(mIsBound) {
                    unbindService(mConnection);
                    mIsBound = false;
                }
                startServiceButton.setEnabled(true);
                stopSongButton.setEnabled(false);
                stopServiceButton.setEnabled(false);
                playSongButton.setEnabled(false);
                pauseSongButton.setEnabled(false);
                resumeSongButton.setEnabled(false);

                mIsStarted =false;

                Intent startIntent = new Intent(IMyAidlInterface.class.getName());
                ResolveInfo info = getPackageManager().resolveService(startIntent, 0);
                startIntent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
                startIntent.setAction(STOP_FOREGROUND_ACTION);

                startForegroundService(startIntent);
            }
        });

        mFilter = new IntentFilter(UNBIND) ;
        mFilter.setPriority(100);
        mReceiver = new BrdcstRcvr() ;
        registerReceiver(mReceiver, mFilter) ;

    }

    @Override
    public void onResume() {
        super.onResume();

        if(!mIsBound) {
            Intent i = new Intent(IMyAidlInterface.class.getName());
            ResolveInfo info = getPackageManager().resolveService(i, 0);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
            bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            if(!isMusicStarted && mIsStarted) playSongButton.setEnabled(true);
        }

    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {
            mMusicService = IMyAidlInterface.Stub.asInterface(iservice);
            System.out.println("9");
            System.out.println(mMusicService.toString());
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            System.out.println("8");
            mMusicService = null;
            mIsBound = false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mIsBound) {
            if(isMusicStarted) {
                try {
                    mMusicService.stopSong();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            unbindService(this.mConnection);
            mIsBound = false;
        }
    }

    class BrdcstRcvr extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            System.out.println("onReceive");
            if(mIsBound) {
                unbindService(mConnection);
                mIsBound = false;
                isMusicStarted = false;

                pauseSongButton.setEnabled(false);
                stopSongButton.setEnabled(false);
                playSongButton.setEnabled(false);
            }
        }
    }
}
