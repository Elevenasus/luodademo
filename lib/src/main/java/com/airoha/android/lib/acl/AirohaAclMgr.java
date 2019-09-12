package com.airoha.android.lib.acl;

import android.content.Context;
import android.support.annotation.NonNull;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.OnAirohaAclEventListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.CRC8;
import com.airoha.android.lib.acl.OnAclExternalFlashRespListener;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MTK60279 on 2018/3/23.
 */

public class AirohaAclMgr {

    private final byte CRC_INITIAL = 0;

    private final AirohaLink mAirohaLink;
    private final Context mCtx;

    private ConcurrentHashMap<String, com.airoha.android.lib.acl.OnAclExternalFlashRespListener> mClientMaps;

    public AirohaAclMgr(@NonNull AirohaLink airohaLink){
        mAirohaLink = airohaLink;
        mCtx = mAirohaLink.getContext();

        mClientMaps = new ConcurrentHashMap<>();

        mAirohaLink.setAclEventListener(mAclEventListener);
    }

    public void registerClientListener(@NonNull String tag, @NonNull com.airoha.android.lib.acl.OnAclExternalFlashRespListener listener){
        mClientMaps.put(tag, listener);
    }


    private OnAirohaAclEventListener mAclEventListener = new OnAirohaAclEventListener() {
        @Override
        public void OnHandleCurrentCmd(byte[] packet) {

            byte status = packet[9];

            if (isUnlockCmd(packet)) {
                for (OnAclExternalFlashRespListener listener : mClientMaps.values()) {
                    if (listener != null) {
                        listener.OnUnlockExternalFlashResp(status);
                    }
                }
                return;
            }

            if (isExternalEraseCmd(packet)) {
                if(status == 0x00) {
                    pollEraseDone();
                }else {
                    for(OnAclExternalFlashRespListener listener: mClientMaps.values()){
                        if(listener != null){
                            listener.OnEraseExternalFlashResp((byte) 0xFF);
                        }
                    }
                }
            }

            if (isPollingCmd(packet)) {
                byte write_operation = packet[10];
                for(OnAclExternalFlashRespListener listener: mClientMaps.values()){
                    if(listener != null){
                        if (status == 0x00 && write_operation == 0x00) {
                            listener.OnEraseExternalFlashResp((byte) 0x00);
                        }else {
                            listener.OnEraseExternalFlashResp((byte) 0xFF);
                        }
                    }
                }
            }

            if (isExternalProgramCmd(packet)) {
                int addr = ((packet[10]&0xFF)<<16) | ((packet[11]&0xFF)<<8) | (packet[12] & 0xFF);
                for(OnAclExternalFlashRespListener listener: mClientMaps.values()){
                    if(listener != null){
                        listener.OnWritePageExternalFlashResp(status, addr);
                    }
                }
            }

            if (isExternalReadCmd(packet)) {
                int addr = ((packet[10]&0xFF)<<16) | ((packet[11]&0xFF)<<8) | (packet[12] & 0xFF);
                byte[] pageData = new byte[256];

                System.arraycopy(packet, 13, pageData, 0, packet.length);

                for(OnAclExternalFlashRespListener listener: mClientMaps.values()){
                    if(listener != null){
                        listener.OnReadPageExternalFlashResp(status, addr, pageData);
                    }
                }
            }


            if (isLockCmd(packet)) {
                for(OnAclExternalFlashRespListener listener: mClientMaps.values()){
                    if(listener != null){
                        listener.OnLockPageExternalFlashResp(status);
                    }
                }
            }
        }
    };

    /**
     * unlock external flash before erase/write
     * @see OnAclExternalFlashRespListener#OnUnlockExternalFlashResp(byte)
     */
    public void unlockExternalFlash(){
        AclPacket packet = new AclPacket(ACL_OCF.HCI_ACL_OCF_SPIFLASH_UNLOCK_ALL, null);
        mAirohaLink.sendCommand(packet.getRaw());
    }

    /**
     * lock external flash after erase/write
     * @see OnAclExternalFlashRespListener#OnLockPageExternalFlashResp(byte)
     */
    public void lockExternalFlash(){
        AclPacket packet = new AclPacket(ACL_OCF.HCI_ACL_OCF_SPIFLASH_LOCK_ALL, null);
        mAirohaLink.sendCommand(packet.getRaw());
    }

    private static boolean isUnlockCmd(byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [12] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        return  (packet[7] == ACL_OCF.HCI_ACL_OCF_SPIFLASH_UNLOCK_ALL && packet[8] == ACL_OGF.getAclVcmd());
    }

    private static boolean isLockCmd(byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [12] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        return  (packet[7] == ACL_OCF.HCI_ACL_OCF_SPIFLASH_LOCK_ALL && packet[8] == ACL_OGF.getAclVcmd());
    }

    /**
     * erase a page(256 bytes) from the given address
     * @param address
     * @see OnAclExternalFlashRespListener#OnEraseExternalFlashResp(byte)
     */
    public void eraseExternalFlash(int address){
        byte[] addr = {(byte)((address >>16) & 0xFF), (byte)((address>>8) & 0xFF), (byte)address};

        AclPacket packet = new AclPacket(ACL_OCF.ACL_VCMD_SPIFLASH_SECTOR_ERASE, addr);

        mAirohaLink.sendCommand(packet.getRaw());
    }

    private static boolean isExternalEraseCmd(byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [1A] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        return packet[7] == ACL_OCF.ACL_VCMD_SPIFLASH_SECTOR_ERASE && packet[8] == ACL_OGF.getAclVcmd();
    }

    private void pollEraseDone(){
        AclPacket packet = new AclPacket(ACL_OCF.ACL_VCMD_POLLING_FOR_ERASE_DONE, null);
        mAirohaLink.sendCommand(packet.getRaw());
    }

    private static boolean isPollingCmd(byte[] packet) {
        // [02] [00] [0F] [06] [00] [01] [00] [20] [04] [00] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [operation]
        return packet[7] == ACL_OCF.ACL_VCMD_POLLING_FOR_ERASE_DONE && packet[8] == ACL_OGF.getAclVcmd();
    }


    /**
     * write data in 256 bytes from the given address
     * @param address
     * @param data 256 bytes
     * @see OnAclExternalFlashRespListener#OnWritePageExternalFlashResp(byte, int)
     */
    public void writePageExternalFlash(int address, @NonNull byte[] data){
        byte[] payload = new byte[2 + 3 + 256];

        // copy crc
        CRC8 crc8 = new CRC8(CRC_INITIAL);
        crc8.update(data);

        payload[0] = (byte) crc8.getValue();
        payload[1] = (byte) 0x00;

        payload[2] = (byte)((address>>16) & 0xFF);
        payload[3] = (byte)((address>>8) & 0xFF);
        payload[4] = (byte) (address & 0xFF);

        System.arraycopy(data, 0, payload, 5, data.length);

        AclPacket packet = new AclPacket(ACL_OCF.ACL_VCMD_SPIFLASH_PAGE_PROGRM, payload);

        mAirohaLink.sendCommand(packet.getRaw());
    }

    private static boolean isExternalProgramCmd(byte[] packet) {
        // [02] [00] [0F] [09] [00] [01] [00] [1B] [04] [00] [   ] [   ] [   ] [   ]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [addr_b2] [addr_b1] [addr_b0]
        return packet[7] == ACL_OCF.ACL_VCMD_SPIFLASH_PAGE_PROGRM && packet[8] == ACL_OGF.getAclVcmd();
    }

    /**
     * read data in 256 bytes from the given address
     * @param address
     * @see OnAclExternalFlashRespListener#OnReadPageExternalFlashResp(byte, int, byte[])
     */
    public void readPageExternalFlash(int address) {
        byte[] addr = {(byte)((address >>16) & 0xFF), (byte)((address>>8) & 0xFF), (byte)address};

        AclPacket packet = new AclPacket(ACL_OCF.HCI_ACL_OCF_SPIFLASH_PAGE_READ, addr);

        mAirohaLink.sendCommand(packet.getRaw());
    }

    private static boolean isExternalReadCmd(byte[] packet) {
        return packet[7] == ACL_OCF.HCI_ACL_OCF_SPIFLASH_PAGE_READ && packet[8] == ACL_OGF.getAclVcmd();
    }

}
