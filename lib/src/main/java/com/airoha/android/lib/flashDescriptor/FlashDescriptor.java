package com.airoha.android.lib.flashDescriptor;

import com.airoha.android.lib.flashDescriptor.cmd.ACL_READ;
import com.airoha.android.lib.ota.OnAirohaAclEventListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.Converter;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Evonne.Hsieh on 2017/7/26.
 */

public class FlashDescriptor {

    private static FlashDescriptor ourInstance = new FlashDescriptor();

    public static FlashDescriptor getInstance() {
        return ourInstance;
    }

    public boolean isPass = false;
    public ArrayList<SectorTableHeader> _stHeaderList = null;
    private int _reservedW60 = 0;
    private int _crc16 = 0;
    private ACL_READ _read = null;
    private AirohaLink mAirohaLink;
    private OnFlashDescriptorListener descriptorResultListener;

    public void StartDescriptorParser(AirohaLink airohaLink)
    {
        isPass = false;
        mAirohaLink = airohaLink;
        // read flash前256 bytes來parse descriptor
        _read = new ACL_READ(mAirohaLink, 0);
        mAirohaLink.setAclEventListener(mRomDescriptorListener);
        _read.SendCmd();
    }

    private final OnAirohaAclEventListener mRomDescriptorListener = new OnAirohaAclEventListener() {
        @Override
        public void OnHandleCurrentCmd(byte[] packet) {
            _read.handleResp(packet);

            if (_read.isCompleted()) {
                ArrayList<Integer> ptrList = new ArrayList<>();
                byte[] _descriptor = _read.getData();
                int idx = 0;
                for(int i = 0; i < 15; i++)
                {
                    byte[] addr = new byte[4];
                    addr[0] = _descriptor[idx];
                    addr[1] = _descriptor[idx + 1];
                    addr[2] = _descriptor[idx + 2];
                    addr[3] = _descriptor[idx + 3];
                    idx += 4;
                    ptrList.add(ByteBuffer.wrap(addr).getInt());
                }
                _reservedW60 = Converter.BytesToU16(_descriptor[idx], _descriptor[idx + 1]);
                idx += 2;
                _crc16 = Converter.BytesToU16(_descriptor[idx], _descriptor[idx + 1]);

                if(ptrList.get(PtrIndex.WorkingArea1) != 0)
                {
                    _read = new ACL_READ(mAirohaLink, addressToFlashAddress(ptrList.get(PtrIndex.WorkingArea1)));
                    mAirohaLink.setAclEventListener(mFlashDescriptorListener);
                    _read.SendCmd();
                }

            }
        }
    };

    private final OnAirohaAclEventListener mFlashDescriptorListener = new OnAirohaAclEventListener() {
        @Override
        public void OnHandleCurrentCmd(byte[] packet) {
            _read.handleResp(packet);

            if (_read.isCompleted()) {
                ArrayList<Integer> _headerList = new ArrayList<>();
                byte[] fdRaw = _read.getData();
                int idx = 0;
                for(int i = 0; i < 15; i++)
                {
                    byte[] addr = new byte[4];
                    addr[0] = fdRaw[idx];
                    addr[1] = fdRaw[idx + 1];
                    addr[2] = fdRaw[idx + 2];
                    addr[3] = fdRaw[idx + 3];
                    idx += 4;
                    _headerList.add(ByteBuffer.wrap(addr).getInt());
                }
                _reservedW60 = Converter.BytesToU16(fdRaw[idx], fdRaw[idx + 1]);
                idx += 2;
                _crc16 = Converter.BytesToU16(fdRaw[idx], fdRaw[idx + 1]);

                _stHeaderList = new ArrayList<SectorTableHeader>();
                headerProcessList.clear();
                headerProcessCnt = 0;
                addSectorTableHeaderByAddress(_headerList.get(PtrIndex.SectorHeaderConfig0));
                addSectorTableHeaderByAddress(_headerList.get(PtrIndex.SectorHeaderConfig1));
                addSectorTableHeaderByAddress(_headerList.get(PtrIndex.SectorHeaderDspData));
                addSectorTableHeaderByAddress(_headerList.get(PtrIndex.SectorHeaderBoundary));
                addSectorTableHeaderByAddress(_headerList.get(PtrIndex.SectorHeaderVoiceData));
                addSectorTableHeaderByAddress(_headerList.get(PtrIndex.SectorHeaderRuntime));
                addSectorTableHeaderByAddress(_headerList.get(PtrIndex.SectorHeaderToolMisc));
                addSectorTableHeaderByAddress(_headerList.get(PtrIndex.SectorHeaderMergeRuntime1));
                addSectorTableHeaderByAddress(_headerList.get(PtrIndex.SectorHeaderMergeRuntime2));


                int stAddress = addressToFlashAddress(headerProcessList.get(headerProcessCnt));
                _read = new ACL_READ(mAirohaLink, stAddress);
                mAirohaLink.setAclEventListener(mStHeaderListener);
                _read.SendCmd();
            }
        }
    };

    private ArrayList<Integer> headerProcessList = new ArrayList<>();
    private int headerProcessCnt = 0;
    private void addSectorTableHeaderByAddress(int address)
    {
        headerProcessList.add(address);
    }

    private int LENGTH_OF_SECTOR_INFO = 9; // 4-byte address + 4-byte length + 1 byte attribute
    private final OnAirohaAclEventListener mStHeaderListener = new OnAirohaAclEventListener() {
        @Override
        public void OnHandleCurrentCmd(byte[] packet) {
            _read.handleResp(packet);

            if (_read.isCompleted()) {
                byte[] raw = _read.getData();
                int sectorCnt = raw[0];
                // count + count * 9 + CRC16
                int size = 1 + sectorCnt * LENGTH_OF_SECTOR_INFO + 2;
                byte[] stRaw = new byte[size];
                System.arraycopy(raw, 0, stRaw, 0, stRaw.length);
                _stHeaderList.add(new SectorTableHeader(stRaw));
                headerProcessCnt++;
                if(headerProcessCnt == 9)
                {
                    isPass = true;
                    descriptorResultListener.OnResult();

                    // 2017.12.04 Daniel: bug fix, should return or index out of bound
                    return;
                }

                int stAddress = addressToFlashAddress(headerProcessList.get(headerProcessCnt));
                _read = new ACL_READ(mAirohaLink, stAddress);
                _read.SendCmd();
            }
        }
    };

    private int addressToFlashAddress(int address)
    {
        return address - 0x800000;
    }

    public void setDescriptorListener(OnFlashDescriptorListener listener){
        descriptorResultListener = listener;
    }

    public static class PtrIndex
    {
        public static int FlashInitEntry = 0;
        public static int SectorsCheckFuncEntry = 1;
        public static int FlashCodeDescriptor = 2;
        public static int SectorHeaderConfig0 = 3;
        public static int SectorHeaderConfig1 = 4;
        public static int SectorHeaderDspData = 5;
        public static int SectorHeaderBoundary = 6;
        public static int SectorHeaderVoiceData = 7;
        public static int SectorHeaderRuntime = 8;
        public static int SectorHeaderToolMisc = 9;
        public static int WorkingArea1 = 10;
        public static int WorkingArea2 = 11;
        public static int EngineerInitFuncEntry = 12;

        public static int McuHcontEnd = 10;
        public static int SectorHeaderMergeRuntime1 = 11;
        public static int SectorHeaderMergeRuntime2 = 12;
    }

    public class SectorTableHeader
    {
        public ArrayList<SectorInfo> _sectorInfoList = null;
        private int _crc16;
        public SectorTableHeader(byte[] raw)
        {
            _sectorInfoList = new ArrayList<SectorInfo>();
            int sectorCnt = raw[0];
            int idx = 1;
            int address, length;
            byte attribute;
            for (int i = 0; i < sectorCnt; ++i)
            {
                byte[] addr = new byte[4];
                addr[0] = raw[idx];
                addr[1] = raw[idx + 1];
                addr[2] = raw[idx + 2];
                addr[3] = raw[idx + 3];
                address = ByteBuffer.wrap(addr).getInt();
                idx += 4;
                byte[] len = new byte[4];
                len[0] = raw[idx];
                len[1] = raw[idx + 1];
                len[2] = raw[idx + 2];
                len[3] = raw[idx + 3];
                length = ByteBuffer.wrap(len).getInt();
                idx += 4;
                attribute = raw[idx];
                _sectorInfoList.add(new SectorInfo(address, addressToFlashAddress(address), length, attribute));
                ++idx;
            }
            _crc16 = Converter.BytesToU16(raw[idx], raw[idx + 1]);
        }
    }

    public class SectorInfo
    {
        public int address;
        public int flashAddress;
        public int length;
        public byte attribute;
        public SectorInfo(int addr, int flashAddr, int len, byte attr)
        {
            address = addr;
            flashAddress = flashAddr;
            length = len;
            attribute = attr;
        }
    }
}
