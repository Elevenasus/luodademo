package com.airoha.android.lib.acl;

/**
 * Created by MTK60279 on 2018/3/26.
 */

public interface OnAclExternalFlashRespListener {
    /**
     * Resp. for {@link AirohaAclMgr#unlockExternalFlash()}
     * @param status
     */
    void OnUnlockExternalFlashResp(byte status);

    /**
     * Resp. for {@link AirohaAclMgr#eraseExternalFlash(int)}
     * @param status
     */
    void OnEraseExternalFlashResp(byte status);

    /**
     * Resp. for {@link AirohaAclMgr#writePageExternalFlash(int, byte[])}
     * @param status
     * @param address
     *
     */
    void OnWritePageExternalFlashResp(byte status, int address);

    /**
     * Resp. for {@link AirohaAclMgr#readPageExternalFlash(int)}
     * @param status
     * @param address
     * @param data
     */
    void OnReadPageExternalFlashResp(byte status, int address, byte[] data);

    /**
     * Resp. for {@link AirohaAclMgr#lockExternalFlash()}
     * @param status
     */
    void OnLockPageExternalFlashResp(byte status);
}
