package com.airoha.android.lib.ota.flash;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel.Lee on 2017/5/25.
 */

public class FlashSizeLookup {

    private static MD25D80 md25d80 = new MD25D80();
    private static MX25L8035E mx25l8035e = new MX25L8035E();
    private static MX25R1635F mx25r1635f = new MX25R1635F();
    private static MX25R3235F mx25r3235f = new MX25R3235F();
    private static MX25U8033E mx25u8033e = new MX25U8033E();
    private static GD25Q32C gd25q32c = new GD25Q32C();
    private static GD25Q64C gd25q64c = new GD25Q64C();
    private static GD25Q128C gd25q128c = new GD25Q128C();
    private static FM25Q08A fm25q8a = new FM25Q08A();
    private static W25Q32JV w25Q32JV = new W25Q32JV();

    // 2018.08.07 Daniel: new external flash
    private static BH25D16A bh25D16A = new BH25D16A();
    private static FM25Q16A fm25Q16A = new FM25Q16A();
    private static P25Q16H p25Q16H = new P25Q16H();

    private static List<IFlashInfo> flashList = new ArrayList<>();

    private static FlashSizeLookup mInst;

    public static FlashSizeLookup Inst(){
        if(mInst == null){
            mInst = new FlashSizeLookup();
        }

        return mInst;
    }

    private FlashSizeLookup(){
        flashList = new ArrayList<>();
        flashList.add(md25d80);
        flashList.add(mx25l8035e);
        flashList.add(mx25r1635f);
        flashList.add(mx25r3235f);
        flashList.add(mx25u8033e);
        flashList.add(gd25q32c);
        flashList.add(gd25q64c);
        flashList.add(gd25q128c);
        flashList.add(fm25q8a);
        flashList.add(w25Q32JV);
        flashList.add(bh25D16A);
        flashList.add(fm25Q16A);
        flashList.add(p25Q16H);
    }

//    public int GetFlashSize(FLASH_STRU flashStruc)
//    {
//        for(int i = 0; i < flashList.size(); i++)
//        {
//            if(flashList.get(i).MafID() == flashStruc.MafID && flashList.get(i).MemoryDesity() == flashStruc.MemoryDesity)
//                return flashList.get(i).Size();
//        }
//
//        return FlashSize.UNKNOWN;
//    }

    public int GetFlashSize(byte mafID, byte memoryDesity)
    {
        for(int i = 0; i < flashList.size(); i++)
        {
            if(flashList.get(i).MafID() == mafID && flashList.get(i).MemoryDesity() == memoryDesity)
                return flashList.get(i).Size();
        }

        return FlashSize.UNKNOWN;
    }
}
