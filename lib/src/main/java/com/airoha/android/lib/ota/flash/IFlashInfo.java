package com.airoha.android.lib.ota.flash;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public interface IFlashInfo {
    byte MafID();
    byte MemoryType();
    byte MemoryDesity();
    int Size();
}
