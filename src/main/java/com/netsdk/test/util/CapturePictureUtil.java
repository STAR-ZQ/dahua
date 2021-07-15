package com.netsdk.test.util;

import com.alibaba.fastjson.JSON;
import com.netsdk.common.SavePath;
import com.netsdk.demo.frame.CapturePicture;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author ZQ
 */
public class CapturePictureUtil {
    private static NetSDKLib netsdk = NetSDKLib.NETSDK_INSTANCE;

    private static NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();

    private static NetSDKLib.LLong m_hLoginHandle = new NetSDKLib.LLong(0);

    private static Disconnect disconnect = new Disconnect();

    private static HaveReconnect haveReconnect = new HaveReconnect();

    private static fCaptureReceiveCB m_CaptureReceiveCB = new fCaptureReceiveCB();

    /**
     * 封装抓图方法
     *
     * @param m_strIp  ip
     * @param m_nPort 端口
     * @param m_strUser 账号
     * @param m_strPassword 密码
     * @param chn 通道id
     * @param mode 请求一帧 默认0
     * @param interival 时间单位秒 默认0
     */
    public static void capturePicture(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, int chn, int mode, int interival) {
        //初始化sdk
        LoginModule.init(disconnect, haveReconnect);
        //登录
        if (m_hLoginHandle.longValue() == 0) {
            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }
        //截图
        if (m_hLoginHandle.longValue() != 0) {
            snapPicture(chn, mode, interival);
        }
        try {
            synchronized (fCaptureReceiveCB.class) {
                //默认等待3s，防止设备断线时抓拍回调没有被触发，而导致死等（死锁）
                fCaptureReceiveCB.class.wait(3000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("--->" + Thread.currentThread().getName() + "CLIENT_SnapPictureEx Success." + System.currentTimeMillis());
        logout();
        LoginModule.cleanup();
    }

    /**
     * 登录
     *
     * @param m_strIp ip
     * @param m_nPort 端口
     * @param m_strUser 账号
     * @param m_strPassword 密码
     * @return
     */
    public static boolean login(String m_strIp, int m_nPort, String m_strUser, String m_strPassword) {
        //IntByReference nError = new IntByReference(0);
        //入参
        NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam = new NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstInParam.nPort = m_nPort;
        pstInParam.szIP = m_strIp.getBytes();
        pstInParam.szPassword = m_strPassword.getBytes();
        pstInParam.szUserName = m_strUser.getBytes();
        //出参
        NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam = new NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstOutParam.stuDeviceInfo = m_stDeviceInfo;
        m_hLoginHandle = netsdk.CLIENT_LoginWithHighLevelSecurity(pstInParam, pstOutParam);
        System.out.println(JSON.toJSONString(netsdk.getClass()) + "==========================");
        if (m_hLoginHandle.longValue() == 0) {
            System.err.printf("Login Device[%s] Port[%d]Failed. %s\n", m_strIp, m_nPort, ToolKits.getErrorCodePrint());
        } else {
            System.out.println(JSON.toJSONString(pstOutParam) + "============================");
            System.out.println("Login Success [ " + m_strIp + " ]");
        }
        return m_hLoginHandle.longValue() != 0;
    }

    /**
     * 退出登录
     *
     * @return
     */
    private static boolean logout() {
        if (m_hLoginHandle.longValue() == 0) {
            return false;
        }

        boolean bRet = netsdk.CLIENT_Logout(m_hLoginHandle);
        if (bRet) {
            m_hLoginHandle.setValue(0);
        }

        return bRet;
    }

    /**
     * 抓图方法
     * @param chn 通道号
     * @param mode 默认0
     * @param interval 默认0
     * @return  成功返回true
     */
    private static boolean snapPicture(int chn, int mode, int interval) {
        setSnapRevCallBack(m_CaptureReceiveCB);
        NetSDKLib.SNAP_PARAMS snap_params = new NetSDKLib.SNAP_PARAMS();
        snap_params.Channel = chn;
        snap_params.mode = mode;
        snap_params.Quality = 3;
        snap_params.InterSnap = interval;
        snap_params.CmdSerial = 0;

        IntByReference reference = new IntByReference(0);
        if (!netsdk.CLIENT_SnapPictureEx(m_hLoginHandle, snap_params, reference)) {
            System.out.print("CLIENT_SnapPictureEx Failed!" + ToolKits.getErrorCodePrint());
            return false;
        } else {
            System.out.println("CLIENT_SnapPictureEx success");
        }
        return true;
    }


    /**
     * 保存图片
     */
    private static class fCaptureReceiveCB implements NetSDKLib.fSnapRev {
        BufferedImage bufferedImage = null;

        @Override
        public void invoke(NetSDKLib.LLong lLoginID, Pointer pBuf, int RevLen, int EncodeType, int CmdSerial, Pointer dwUser) {
            if (pBuf != null && RevLen > 0) {
                String strFileName = SavePath.getSavePath().getSaveCapturePath();

                System.out.println("strFileName = " + strFileName);

                byte[] buf = pBuf.getByteArray(0, RevLen);
                ByteArrayInputStream byteArrInput = new ByteArrayInputStream(buf);
                try {
                    bufferedImage = ImageIO.read(byteArrInput);
                    if (bufferedImage == null) {
                        return;
                    }
                    ImageIO.write(bufferedImage, "jpg", new File(strFileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 抓图回调函数
     *
     * @param cbSnapReceive
     */
    private static void setSnapRevCallBack(NetSDKLib.fSnapRev cbSnapReceive) {
        netsdk.CLIENT_SetSnapRevCallBack(cbSnapReceive, null);
    }

    /**
     * 设备断线回调：通过CLEIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
     */
    private static class Disconnect implements NetSDKLib.fDisConnect {
        @Override
        public void invoke(NetSDKLib.LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
        }
    }

    /**
     * 网络连接恢复，设备重连成功回调
     * 通过CLIENT_SetAutoReconnect设置该回调函数，当已断线的设备重连成功时，SDK会调用该函数
     */
    private static class HaveReconnect implements NetSDKLib.fHaveReConnect {
        @Override
        public void invoke(NetSDKLib.LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
        }
    }

    public static void main(String[] args) {
        CapturePictureUtil.capturePicture("192.168.101.251",37777,"admin","zhwl1234",1,0,0);
    }
}
