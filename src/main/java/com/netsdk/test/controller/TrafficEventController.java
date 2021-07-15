package com.netsdk.test.controller;

import com.alibaba.fastjson.JSON;
import com.netsdk.common.Res;
import com.netsdk.common.SavePath;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.demo.module.TrafficEventModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.netsdk.test.entity.TrafficInfo;
import com.netsdk.test.util.CapturePictureUtil;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.apache.commons.beanutils.BeanUtils;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

public class TrafficEventController extends AWTEvent {
    public static NetSDKLib netsdk = NetSDKLib.NETSDK_INSTANCE;
    // 设备断线通知回调
    private static DisConnect disConnect = new DisConnect();

    // 网络连接恢复
    private static HaveReConnect haveReConnect = new HaveReConnect();

    // 预览句柄
    public static NetSDKLib.LLong m_hPlayHandle = new NetSDKLib.LLong(0);

    // 登陆句柄
    public static NetSDKLib.LLong m_hLoginHandle = new NetSDKLib.LLong(0);

    // 设备信息
    public static NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();

    private AnalyzerDataCB m_AnalyzerDataCB = new AnalyzerDataCB();

    private TrafficEventController target = this;

    private TrafficInfo trafficInfo = new TrafficInfo();
    private static final long serialVersionUID = 1L;
    public static final int EVENT_ID = AWTEvent.RESERVED_ID_MAX + 1;
    private BufferedImage snapImage = null;
    private BufferedImage plateImage = null;

    public TrafficEventController(Object target,
                                  BufferedImage snapImage,
                                  BufferedImage plateImage,
                                  TrafficInfo info) {
        super(target, EVENT_ID);

        this.snapImage = snapImage;
        this.plateImage = plateImage;
        this.trafficInfo = info;
    }

    public TrafficEventController(Object target) {
        super(target, EVENT_ID);
    }

    @PostConstruct
    public void trafficPic(String ip, int port, String userName, String password) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginModule.init(disConnect, haveReConnect);
                if (m_hLoginHandle.longValue() == 0) {
                    Native.setCallbackThreadInitializer(m_AnalyzerDataCB,
                            new CallbackThreadInitializer(false, false, "traffic callback thread"));
                    boolean flag = LoginModule.login("192.168.101.251", 37777, "admin", "zhwl1234");
                    System.out.println(flag);
                    TrafficEventModule.attachIVSEvent(0,
                            m_AnalyzerDataCB);
                }

                try {
                    while (true) {
                        synchronized (AnalyzerDataCB.class) {
                            //默认等待3s，防止设备断线时抓拍回调没有被触发，而导致死等（死锁）
                            AnalyzerDataCB.class.wait(3000L);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("--->" + Thread.currentThread().getName() + "CLIENT_SnapPictureEx Success." + System.currentTimeMillis());
                logout();
                LoginModule.cleanup();
            }
        });
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

    /*
     * 智能报警事件回调
     */
    private class AnalyzerDataCB implements NetSDKLib.fAnalyzerDataCallBack {
        @Override
        public int invoke(NetSDKLib.LLong lAnalyzerHandle, int dwAlarmType,
                          Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize,
                          Pointer dwUser, int nSequence, Pointer reserved) {
            if (lAnalyzerHandle.longValue() == 0) {
                return -1;
            }

            if (dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFICJUNCTION
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_RUNREDLIGHT
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_OVERLINE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_RETROGRADE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_TURNLEFT
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_TURNRIGHT
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_UTURN
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_OVERSPEED
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_UNDERSPEED
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_PARKING
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_WRONGROUTE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_CROSSLANE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_OVERYELLOWLINE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_YELLOWPLATEINLANE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_PEDESTRAINPRIORITY
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_MANUALSNAP
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_VEHICLEINROUTE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_VEHICLEINBUSROUTE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_BACKING
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_PARKINGSPACEPARKING
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_PARKINGSPACENOPARKING
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_WITHOUT_SAFEBELT) {

                // 获取识别对象 车身对象 事件发生时间 车道号等信息
                GetStuObject(dwAlarmType, pAlarmInfo);

                // 保存图片，获取图片缓存
                savePlatePic(pBuffer, dwBufSize, trafficInfo);

                System.out.println("==========================" + JSON.toJSONString(trafficInfo));

//                // 列表、图片界面显示
//                EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
//                if (eventQueue != null) {
//                    eventQueue.postEvent(new TrafficEventController(target,
//                            snapImage,
//                            plateImage,
//                            trafficInfo));
//                }
            }

            return 0;
        }


        /*
         * 保存车牌小图:大华早期交通抓拍机，设备不传单独的车牌小图文件，只传车牌在大图中的坐标;由应用来自行裁剪。
         * 2014年后，陆续有设备版本，支持单独传车牌小图，小图附录在pBuffer后面。
         */
        private void savePlatePic(Pointer pBuffer, int dwBufferSize, TrafficInfo trafficInfo) {

            String bigPicture; // 大图
            String platePicture; // 车牌图

            if (pBuffer == null || dwBufferSize <= 0) {
                return;
            }

            // 保存大图
            byte[] buffer = pBuffer.getByteArray(0, dwBufferSize);
            ByteArrayInputStream byteArrInput = new ByteArrayInputStream(buffer);

            bigPicture = SavePath.getSavePath().getSaveTrafficImagePath() + "Big_" + trafficInfo.getM_Utc().toStringTitle() + "_" +
                    trafficInfo.getM_FileCount() + "-" + trafficInfo.getM_FileIndex() + "-" + trafficInfo.getM_GroupID() + ".jpg";
            trafficInfo.setBigImgUrl(bigPicture);
            try {
                snapImage = ImageIO.read(byteArrInput);
                if (snapImage == null) {
                    return;
                }
                ImageIO.write(snapImage, "jpg", new File(bigPicture));
            } catch (IOException e2) {
                e2.printStackTrace();
            }

            if (bigPicture == null || bigPicture.equals("")) {
                return;
            }

            if (trafficInfo.getM_bPicEnble() == 1) {
                //根据pBuffer中数据偏移保存小图图片文件
                if (trafficInfo.getM_FileLength() > 0) {
                    platePicture = SavePath.getSavePath().getSaveTrafficImagePath() + "plate_" + trafficInfo.getM_Utc().toStringTitle() + "_" +
                            trafficInfo.getM_FileCount() + "-" + trafficInfo.getM_FileIndex() + "-" + trafficInfo.getM_GroupID() + ".jpg";
                    trafficInfo.setPlatImgUrl(platePicture);
                    int size = 0;
                    if (dwBufferSize <= trafficInfo.getM_OffSet()) {
                        return;
                    }

                    if (trafficInfo.getM_FileLength() <= dwBufferSize - trafficInfo.getM_OffSet()) {
                        size = trafficInfo.getM_FileLength();
                    } else {
                        size = dwBufferSize - trafficInfo.getM_OffSet();
                    }
                    byte[] bufPlate = pBuffer.getByteArray(trafficInfo.getM_OffSet(), size);
                    ByteArrayInputStream byteArrInputPlate = new ByteArrayInputStream(bufPlate);
                    try {
                        plateImage = ImageIO.read(byteArrInputPlate);
                        if (plateImage == null) {
                            return;
                        }
                        ImageIO.write(plateImage, "jpg", new File(platePicture));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (trafficInfo.getM_BoundingBox() == null) {
                    return;
                }
                //根据大图中的坐标偏移计算显示车牌小图

                NetSDKLib.DH_RECT dhRect = trafficInfo.getM_BoundingBox();
                //1.BoundingBox的值是在8192*8192坐标系下的值，必须转化为图片中的坐标
                //2.OSD在图片中占了64行,如果没有OSD，下面的关于OSD的处理需要去掉(把OSD_HEIGHT置为0)
                final int OSD_HEIGHT = 0;

                long nWidth = snapImage.getWidth(null);
                long nHeight = snapImage.getHeight(null);

                nHeight = nHeight - OSD_HEIGHT;
                if ((nWidth <= 0) || (nHeight <= 0)) {
                    return;
                }

                NetSDKLib.DH_RECT dstRect = new NetSDKLib.DH_RECT();

                dstRect.left.setValue((long) ((double) (nWidth * dhRect.left.longValue()) / 8192.0));
                dstRect.right.setValue((long) ((double) (nWidth * dhRect.right.longValue()) / 8192.0));
                dstRect.bottom.setValue((long) ((double) (nHeight * dhRect.bottom.longValue()) / 8192.0));
                dstRect.top.setValue((long) ((double) (nHeight * dhRect.top.longValue()) / 8192.0));

                int x = dstRect.left.intValue();
                int y = dstRect.top.intValue() + OSD_HEIGHT;
                int w = dstRect.right.intValue() - dstRect.left.intValue();
                int h = dstRect.bottom.intValue() - dstRect.top.intValue();

                if (x == 0 || y == 0 || w <= 0 || h <= 0) {
                    return;
                }

                try {
                    plateImage = snapImage.getSubimage(x, y, w, h);
                    platePicture = SavePath.getSavePath().getSaveTrafficImagePath() + "plate_" + trafficInfo.getM_Utc().toStringTitle() + "_" +
                            trafficInfo.getM_FileCount() + "-" + trafficInfo.getM_FileIndex() + "-" + trafficInfo.getM_GroupID() + ".jpg";
                    if (plateImage == null) {
                        return;
                    }
                    ImageIO.write(plateImage, "jpg", new File(platePicture));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 获取识别对象 车身对象 事件发生时间 车道号等信息
        private void GetStuObject(int dwAlarmType, Pointer pAlarmInfo) {
            if (pAlarmInfo == null) {
                return;
            }

            switch (dwAlarmType) {
                case NetSDKLib.EVENT_IVS_TRAFFICJUNCTION: ///< 交通卡口事件
                {
                    NetSDKLib.DEV_EVENT_TRAFFICJUNCTION_INFO msg = new NetSDKLib.DEV_EVENT_TRAFFICJUNCTION_INFO();
                    ToolKits.GetPointerData(pAlarmInfo, msg);

                    trafficInfo.setM_EventName(Res.string().getEventName(NetSDKLib.EVENT_IVS_TRAFFICJUNCTION));
                    try {
                        trafficInfo.setM_PlateNumber(new String(msg.stuObject.szText, "GBK").trim());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    trafficInfo.setM_PlateType(new String(msg.stTrafficCar.szPlateType).trim());
                    trafficInfo.setM_FileCount(String.valueOf(msg.stuFileInfo.bCount));
                    trafficInfo.setM_FileIndex(String.valueOf(msg.stuFileInfo.bIndex));
                    trafficInfo.setM_GroupID(String.valueOf(msg.stuFileInfo.nGroupId));
                    trafficInfo.setM_IllegalPlace(ToolKits.GetPointerDataToByteArr(msg.stTrafficCar.szDeviceAddress));
                    trafficInfo.setM_LaneNumber(String.valueOf(msg.nLane));
                    trafficInfo.setM_PlateColor(new String(msg.stTrafficCar.szPlateColor).trim());
                    trafficInfo.setM_VehicleColor(new String(msg.stTrafficCar.szVehicleColor).trim());
                    trafficInfo.setM_VehicleType(new String(msg.stuVehicle.szObjectSubType).trim());
                    trafficInfo.setM_VehicleSize(Res.string().getTrafficSize(msg.stTrafficCar.nVehicleSize));
                    trafficInfo.setM_Utc(msg.UTC);
                    trafficInfo.setM_bPicEnble(msg.stuObject.bPicEnble);
                    trafficInfo.setM_OffSet(msg.stuObject.stPicInfo.dwOffSet);
                    trafficInfo.setM_FileLength(msg.stuObject.stPicInfo.dwFileLenth);
                    trafficInfo.setM_BoundingBox(msg.stuObject.BoundingBox);

                    break;
                }
                default:
                    break;
            }
        }

    }

    /**
     * 登录
     *
     * @param m_strIp       ip
     * @param m_nPort       端口
     * @param m_strUser     账号
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

    public TrafficInfo buildInfo() {
        return trafficInfo;
    }

    // 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
    private static class DisConnect implements NetSDKLib.fDisConnect {
        @Override
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
        }
    }

    // 网络连接恢复，设备重连成功回调
    // 通过 CLIENT_SetAutoReconnect 设置该回调函数，当已断线的设备重连成功时，SDK会调用该函数
    private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
        @Override
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);

        }
    }

    public static void main(String[] args) {
        final TrafficEventController trafficEventController = new TrafficEventController("");
        trafficEventController.trafficPic("192.168.101.251", 37777, "admin", "zhwl1234");
    }
}
