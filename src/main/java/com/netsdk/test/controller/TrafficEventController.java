package com.netsdk.test.controller;

import com.alibaba.fastjson.JSON;
import com.netsdk.common.Res;
import com.netsdk.common.SavePath;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.demo.module.TrafficEventModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.netsdk.test.dto.TrafficDto;
import com.netsdk.test.entity.TrafficInfo;
import com.netsdk.test.util.CapturePictureUtil;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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
    private TrafficDto trafficDto = new TrafficDto();
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
                imgUrl(bigPicture);
            } catch (Exception e2) {
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
                        imgUrl(platePicture);
                    } catch (Exception e) {
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


                    trafficDto.setEvent_name(Res.string().getEventName(NetSDKLib.EVENT_IVS_TRAFFICJUNCTION));
                    try {
                        trafficDto.setPlate_number(new String(msg.stuObject.szText, "GBK").trim());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    trafficDto.setPlate_type(new String(msg.stTrafficCar.szPlateType).trim());
                    trafficDto.setFile_count(String.valueOf(msg.stuFileInfo.bCount));
                    trafficDto.setFile_index(String.valueOf(msg.stuFileInfo.bIndex));
                    trafficDto.setGroup_id(String.valueOf(msg.stuFileInfo.nGroupId));
                    trafficDto.setIllegal_place(ToolKits.GetPointerDataToByteArr(msg.stTrafficCar.szDeviceAddress));
                    trafficDto.setLane_number(String.valueOf(msg.nLane));
                    trafficDto.setPlate_color(new String(msg.stTrafficCar.szPlateColor).trim());
                    trafficDto.setVehicle_color(new String(msg.stTrafficCar.szVehicleColor).trim());
                    trafficDto.setVehicle_type(new String(msg.stuVehicle.szObjectSubType).trim());
                    trafficDto.setVehicle_size(Res.string().getTrafficSize(msg.stTrafficCar.nVehicleSize));
                    trafficDto.setUtc(msg.UTC);
                    trafficDto.setPicenable(String.valueOf(msg.stuObject.bPicEnble).trim());
                    trafficDto.setOffset(msg.stuObject.stPicInfo.dwOffSet);
                    trafficDto.setFilelength(msg.stuObject.stPicInfo.dwFileLenth);
                    trafficDto.setBounding_box(msg.stuObject.BoundingBox);
                    httpMethod();
                    break;
                }
                default:
                    break;
            }
        }

        public void httpMethod() {// 创建链接  绕过证书校验
            CloseableHttpClient httpClient = null;
            try {
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustStrategy() {
                    // 证书校验忽略
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                });
                httpClient = HttpClients.custom().setSSLContext(builder.build())
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                e.printStackTrace();
            }

            // 创建Post请求
            // 参数
            URI uri = null;
            try {
                // 将参数放入键值对类NameValuePair中,再放入集合中
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("name", "车辆信息上报"));
                params.add(new BasicNameValuePair("code", "7020"));
                params.add(new BasicNameValuePair("token", "7DF72F9A741DFD817906A063EBF9548F"));
                // 设置uri信息,并将参数集合放入uri;
                // 注:这里也支持一个键值对一个键值对地往里面放setParameter(String key, String value)
                uri = new URIBuilder().setScheme("http").setHost("localhost").setPort(18081)
                        .setPath("/test").setParameters(params).build();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }

            HttpPost httpPost = new HttpPost(uri);


            // 将user对象转换为json字符串，并放入entity中
            StringEntity entity = new StringEntity(String.valueOf(JSON.parseObject(JSON.toJSONString(trafficDto))), "UTF-8");

            // post请求是将参数放在请求体里面传过去的;这里将entity放入post请求体中
            httpPost.setEntity(entity);

            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");

            // 响应模型
            CloseableHttpResponse response = null;
            try {
                // 由客户端执行(发送)Post请求
                response = httpClient.execute(httpPost);
                // 从响应模型中获取响应实体
                HttpEntity responseEntity = response.getEntity();

                System.out.println("响应状态为:" + response.getStatusLine());
                if (responseEntity != null) {
                    System.out.println("响应内容长度为:" + responseEntity.getContentLength());
                    System.out.println("响应内容为:" + EntityUtils.toString(responseEntity));
                }
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // 释放资源
                    if (httpClient != null) {
                        httpClient.close();
                    }
                    if (response != null) {
                        response.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void imgUrl(String imgUrl) throws IOException, URISyntaxException {
            CloseableHttpClient httpClient = null;
            try {
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustStrategy() {
                    // 证书校验忽略
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                });
                httpClient = HttpClients.custom().setSSLContext(builder.build())
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                e.printStackTrace();
            }
            CloseableHttpResponse httpResponse = null;
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000000).build();
            URI uri = new URIBuilder().setScheme("http").setHost("localhost").setPort(18081)
                    .setPath("/savePicByFormData").build();
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setConfig(requestConfig);
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

            File file = new File(imgUrl);

            //multipartEntityBuilder.addBinaryBody("file", file,ContentType.create("image/png"),"abc.pdf");
            //当设置了setSocketTimeout参数后，以下代码上传PDF不能成功，将setSocketTimeout参数去掉后此可以上传成功。上传图片则没有个限制
            //multipartEntityBuilder.addBinaryBody("file",file,ContentType.create("application/octet-stream"),"abd.pdf");
            multipartEntityBuilder.addBinaryBody("file", file);
            //multipartEntityBuilder.addPart("comment", new StringBody("This is comment", ContentType.TEXT_PLAIN));
            multipartEntityBuilder.addTextBody("comment", "this is comment");
            HttpEntity httpEntity = multipartEntityBuilder.build();
            httpPost.setEntity(httpEntity);

            httpResponse = httpClient.execute(httpPost);
            HttpEntity responseEntity = httpResponse.getEntity();
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
                StringBuffer buffer = new StringBuffer();
                String str = "";
                while (!StringUtils.isEmpty(str = reader.readLine())) {
                    buffer.append(str);
                }

                System.out.println(buffer.toString());
            }

            httpClient.close();
            if (httpResponse != null) {
                httpResponse.close();
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
