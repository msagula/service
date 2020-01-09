// IMyAidlInterface.aidl
package com.example.common;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    void playSong(int songNum);
    void pauseSong();
    void resumeSong();
    void stopSong();
}
