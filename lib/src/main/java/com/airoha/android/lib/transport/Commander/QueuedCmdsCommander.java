package com.airoha.android.lib.transport.Commander;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Daniel.Lee on 2016/3/28.
 */
public class QueuedCmdsCommander {
    // commands need to be send
    // collections of AirohaMMICmd
    private final BlockingQueue<byte[]> cmds;

    public QueuedCmdsCommander() {

        cmds = new LinkedBlockingQueue<>();
    }

    public byte[] getNextCmd() {
        return cmds.isEmpty() ? null : cmds.remove();
    }

    public void enqueneCmd(byte[] cmd) {
        try {
            cmds.put(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isQueueEmpty(){
        return cmds.isEmpty();
    }

    // is sync done
    public boolean isResponded = true;

    public void clearQueue(){
        cmds.clear();
    }

}
