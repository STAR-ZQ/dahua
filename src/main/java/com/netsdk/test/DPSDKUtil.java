package com.netsdk.test;

import com.netsdk.common.LoginPanel;
import com.netsdk.lib.NetSDKLib;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

/**
 * 大华SDK工具集成类
 */
//@Slf4j
public class DPSDKUtil {
    public static NetSDKLib netsdk 	= NetSDKLib.NETSDK_INSTANCE;
    private static boolean bInit    = false;
    private static NetSDKLib.DEVICE_NET_INFO_EX deviceInfo = new NetSDKLib.DEVICE_NET_INFO_EX();

    public static void getPicturedisposition(NetSDKLib.fDisConnect disConnect,String ip, short port,
                                             String userName, String password) {
        IntByReference nError = new IntByReference(0);

        // 初始化设备
        bInit = netsdk.CLIENT_Init(disConnect, null);
        if(!bInit) {
            System.out.println("Initialize SDK failed");
        }

        NetSDKLib.LLong lLong = netsdk.CLIENT_LoginEx(ip, port, userName, password, NetSDKLib.EM_LOGIN_SPAC_CAP_TYPE.EM_LOGIN_SPEC_CAP_TCP, null, deviceInfo, nError);

        if (lLong.longValue()!=0){

        }
    }

}