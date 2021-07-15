package com.netsdk.test;

import com.netsdk.lib.NetSDKLib;
import com.sun.jna.Pointer;

public class TestfDisConnect implements NetSDKLib.fDisConnect {
    @Override
    public void invoke(NetSDKLib.LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
        System.out.printf("断线回调：\n", pchDVRIP, nDVRPort, lLoginID.longValue(),
                dwUser.toString());
    }
}
