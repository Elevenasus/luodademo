package com.airoha.android.lib.flashDescriptor;

/**
 * Created by Daniel.Lee on 2017/8/10.
 */

public class SectorIndex {

    public enum Config0{
        SECTOR_AFE_INIT_STRU,
        SECTOR_SFR_INIT_STRU,
        SECTOR_RF_MISC_CTL_STRU,
        SECTOR_HW_MISC_CTL_STRU,
        UART_PARAMETER_STRU_ENG,
    }

    public enum Config1{
        SYS_LOCAL_DEVICE_INFO_STRU,
        SYS_LOCAL_DEVICE_EIR_STRU,
        SYS_LOCAL_DEVICE_CONTROL_STRU,
        LC_MISC_CONTROL,
        LM_PARAMETER_STRU,
        HC_PARAMETER_TYPE,
        UART_PARAMETER_STRU,
        DSP_NVRAM_CTL_TYPE,
        A2DP_NVRAM_CTL_TYPE,
        DRIVER_LED_DATA_TYPE,
        DRIVER_BUZZER_DATA_TYPE,
        DRIVER_RINGTONE_DATA_TYPE,
        MMI_DRIVER_NVRAM_BACKUP_TYPE,
        MMI_NVRAM_TYPE,
        MMI_NVRAM_KEYMAP,
        SYS_MEMORY_CONFIG_STRU,
        SM_NVRAM_TYPE,
        GAP_NVRAM_TYPE,
        DRIVER_CTL_TYPE,
        //MMI_LE_NVRAM_TYPE,
        APPLICATION_STRU,
        IAP2_SYNC_PAYLOAD,
        IAP2_IDEN_PARAM,
        LE_MISC_CONTROL_STRU,
        PATCH_CODE_INIT_STRU,
        SPI_CONFIG_STRU,
    }

    public enum DspData{
        DSP_ROM,
        DSP_VP_NB_STRU,
    }

    public enum Boundary{
        MP_PARAMETER_STRU,
        DSP_FUNC_PARA_CTL_STRU,
        DSP_PEQ_PARAMETER_STRU,
        DSP_HPF_PARAMETER_STRU,
        SECTOR_MP_PARAMETER_F,
    }

    public enum Voice{
        VoicePromptLangCtl,
        DRIVER_VOICE_COMMAND_STRU,
    }

    public enum Runtime{
        APP_CALLERNAME_DATA_STRU,
    }

    public enum ToolMisc{
        AE_INFO_STRU,
        TOOL_INFO_STRU,
    }

    public enum RuntimeToggle1{
        MMI_DRIVER_VARIATION_NVRAM_STRU,
        NVRAM_MMI_LINK_DATA_TYPE,
        DUAL_MIC_DATA_STRU,
        MMI_CUSTOMIZE_DATA_TYPE,
    }

    public enum RuntimeToggle2{
        MMI_DRIVER_VARIATION_NVRAM_STRU2,
        NVRAM_MMI_LINK_DATA_TYPE2,
        DUAL_MIC_DATA_STRU2,
        MMI_CUSTOMIZE_DATA_TYPE2,
    }

    // INVALID_SECTOR = 0xFF
}
