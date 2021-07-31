package com.netsdk.test.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netsdk.demo.module.FaceRecognitionModule;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.netsdk.test.dto.FaceInfoDto;
import com.netsdk.test.entity.FaceInfo;
import com.sun.jna.Pointer;
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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class FaceRecognitionController {
    // 设备断线通知回调
    private static DisConnect disConnect = new DisConnect();

    // 网络连接恢复
    private static HaveReConnect haveReConnect = new HaveReConnect();

    // 订阅句柄
    public static NetSDKLib.LLong m_hAttachHandle = new NetSDKLib.LLong(0);

    // 登陆句柄
    public static NetSDKLib.LLong m_hLoginHandle = new NetSDKLib.LLong(0);

    // 全景图
    private static BufferedImage globalBufferedImage = null;

    // 人脸图
    private static BufferedImage personBufferedImage = null;

    // 候选人图
    private static BufferedImage candidateBufferedImage = null;

    private FaceInfo faceInfo = new FaceInfo();
    private FaceInfoDto faceInfoDto = new FaceInfoDto();

    // 用于人脸检测
    private static int groupId = 0;

    private static int index = -1;
    private AnalyzerDataCB analyzerDataCB = new AnalyzerDataCB();

    public FaceRecognitionController() {
    }

    public void faceRecognition() {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        //初始化工程
                        LoginModule.init(disConnect, haveReConnect);
                        if (m_hLoginHandle.longValue() == 0) {
                            boolean flag = LoginModule.login("192.168.101.251", 37777, "admin", "zhwl1234");
                            System.out.println(flag);
                            //订阅
                            FaceRecognitionModule.realLoadPicture(
                                    0, analyzerDataCB);
                        }
                        try {
                            while (true) {
                                synchronized (FaceRecognitionController.AnalyzerDataCB.class) {
                                    //默认等待3s，防止设备断线时抓拍回调没有被触发，而导致死等（死锁）
                                    FaceRecognitionController.AnalyzerDataCB.class.wait(3000L);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("--->" + Thread.currentThread().getName() + "CLIENT_SnapPictureEx Success." + System.currentTimeMillis());
                        LoginModule.logout();
                        LoginModule.cleanup();
                    }
                });
    }

    /**
     * 写成静态主要是防止被回收
     */
    private class AnalyzerDataCB implements NetSDKLib.fAnalyzerDataCallBack {
        private AnalyzerDataCB() {
        }

        @Override
        public int invoke(
                NetSDKLib.LLong lAnalyzerHandle,
                int dwAlarmType,
                Pointer pAlarmInfo,
                Pointer pBuffer,
                int dwBufSize,
                Pointer dwUser,
                int nSequence,
                Pointer reserved) {
            if (lAnalyzerHandle.longValue() == 0 || pAlarmInfo == null) {
                return -1;
            }

            getFaceData(dwAlarmType, pAlarmInfo, pBuffer, dwBufSize);

            return 0;
        }

        public void getFaceData(int dwAlarmType, Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize) {
            System.out.println(dwAlarmType + "/" + NetSDKLib.EVENT_IVS_FACERECOGNITION);
            switch (dwAlarmType) {
                case NetSDKLib.EVENT_IVS_FACERECOGNITION: // /< 人脸识别事件
                {
                    // DEV_EVENT_FACERECOGNITION_INFO 结构体比较大，new对象会比较耗时， ToolKits.GetPointerData内容拷贝是不耗时的。
                    // 如果多台设备或者事件处理比较频繁，可以考虑将 static DEV_EVENT_FACERECOGNITION_INFO msg = new
                    // DEV_EVENT_FACERECOGNITION_INFO(); 改为全局。
                    // 写成全局，是因为每次new花费时间较多, 如果改为全局，此case下的处理需要加锁
                    // 加锁，是因为共用一个对象，防止数据出错

                    // 耗时800ms左右
                    NetSDKLib.DEV_EVENT_FACERECOGNITION_INFO msg = new NetSDKLib.DEV_EVENT_FACERECOGNITION_INFO();

                    System.out.println("========" + JSON.toJSONString(msg.stuFaceData));
                    // 耗时20ms左右
                    ToolKits.GetPointerData(pAlarmInfo, msg);

                    faceInfoDto.setBeardData(msg.stuFaceData.emBeard);
                    faceInfoDto.setEyeData(msg.stuFaceData.emEye);
                    faceInfoDto.setMaskData(msg.stuFaceData.emMask);
                    faceInfoDto.setMouthData(msg.stuFaceData.emMouth);
                    faceInfoDto.setRaceData((msg.stuFaceData.emRace));
                    faceInfoDto.setSexData(msg.stuFaceData.emSex);
                    faceInfoDto.setAge(msg.stuFaceData.nAge);
                    for (int i = 0; i < msg.stuFaceData.emFeature.length; i++) {
                        System.out.println("人脸特征：" + msg.stuFaceData.emFeature[i]);
                        faceInfoDto.setFeaturesData(msg.stuFaceData.emFeature[i]);
                    }
                    faceInfoDto.setAttractive(msg.stuFaceData.nAttractive);
                    FaceInfoDto.FaceCaptureAngle eangle = new FaceInfoDto.FaceCaptureAngle();
                    eangle.setNPitch(msg.stuFaceData.stuFaceCaptureAngle.nPitch);
                    eangle.setNRoll(msg.stuFaceData.stuFaceCaptureAngle.nRoll);
                    eangle.setNYaw(msg.stuFaceData.stuFaceCaptureAngle.nYaw);
                    faceInfoDto.setFace_captur_eangle(eangle);
                    faceInfoDto.setFace_quality(msg.stuFaceData.nFaceQuality);
                    faceInfoDto.setFace_align_score(msg.stuFaceData.nFaceAlignScore);
                    faceInfoDto.setFace_clarity(msg.stuFaceData.nFaceClarity);
                    faceInfoDto.setTemperature_unit(msg.stuFaceData.emTemperatureUnit);
                    faceInfoDto.setIs_overtemp(msg.stuFaceData.bIsOverTemp ? "Y" : "N");
                    faceInfoDto.setIs_undertemp(msg.stuFaceData.bIsUnderTemp ? "Y" : "N");
                    faceInfoDto.setInfo_addr("(" + msg.stuGPSInfo.nLongitude + "," + msg.stuGPSInfo.nLatidude + ")");

//                    faceInfo.setNAge(msg.stuFaceData.nAge);
//                    faceInfo.setEmSex(msg.stuFaceData.emSex);
//                    faceInfo.setEmRace(msg.stuFaceData.emRace);
//                    faceInfo.setEmEye(msg.stuFaceData.emEye);
//                    faceInfo.setEmMask(msg.stuFaceData.emMask);
//                    faceInfo.setEmMouth(msg.stuFaceData.emMouth);
//                    faceInfo.setEmBeard(msg.stuFaceData.emBeard);
                    // 保存图片，获取图片缓存
                    // 耗时20ms左右
                    try {
                        saveFaceRecognitionPic(pBuffer, dwBufSize, msg);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    httpMethod();
                    // 释放内存
                    msg = null;
                    System.gc();

                    break;
                }
                case NetSDKLib.EVENT_IVS_FACEDETECT: // /< 人脸检测
                {
                    NetSDKLib.DEV_EVENT_FACEDETECT_INFO msg = new NetSDKLib.DEV_EVENT_FACEDETECT_INFO();

                    ToolKits.GetPointerData(pAlarmInfo, msg);

                    // 保存图片，获取图片缓存
                    try {
                        saveFaceDetectPic(pBuffer, dwBufSize, msg);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    // 释放内存
                    msg = null;
                    System.gc();

                    break;
                }
                default:
                    break;
            }
        }

        /**
         * http
         * post
         */
        public void httpMethod() {
            // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
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
                params.add(new BasicNameValuePair("name", "人脸信息上报"));
                params.add(new BasicNameValuePair("code", "7010"));
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
            StringEntity entity = new StringEntity(String.valueOf(JSON.parseObject(JSON.toJSONString(faceInfoDto))), "UTF-8");

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

        /**
         * http picUpload
         * @param imgUrl  上传图片路径
         * @throws IOException
         * @throws URISyntaxException
         */
        public void imgUrl(String imgUrl) throws IOException, URISyntaxException {
            //创建连接
            CloseableHttpClient httpClient = null;
            try {
                /**
                 * 忽略证书请求连接
                 */
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

        /**
         * 保存人脸识别事件图片
         *
         * @param pBuffer             抓拍图片信息
         * @param dwBufSize           抓拍图片大小
         * @param faceRecognitionInfo 人脸识别事件信息
         */
        public void saveFaceRecognitionPic(
                Pointer pBuffer, int dwBufSize, NetSDKLib.DEV_EVENT_FACERECOGNITION_INFO faceRecognitionInfo)
                throws FileNotFoundException {
            index = -1;
            globalBufferedImage = null;
            personBufferedImage = null;
            candidateBufferedImage = null;

            File path = new File("./FaceRecognition/");
            if (!path.exists()) {
                path.mkdir();
            }

            if (pBuffer == null || dwBufSize <= 0) {
                return;
            }

            /////////////// 保存全景图 ///////////////////
            if (faceRecognitionInfo.bGlobalScenePic == 1) {

                String strGlobalPicPathName =
                        path + "\\" + faceRecognitionInfo.UTC.toStringTitle() + "_FaceRecognition_Global.jpg";
                faceInfo.setGlobalImgUrl(strGlobalPicPathName);

                byte[] bufferGlobal =
                        pBuffer.getByteArray(
                                faceRecognitionInfo.stuGlobalScenePicInfo.dwOffSet,
                                faceRecognitionInfo.stuGlobalScenePicInfo.dwFileLenth);
                ByteArrayInputStream byteArrInputGlobal = new ByteArrayInputStream(bufferGlobal);

                try {
                    globalBufferedImage = ImageIO.read(byteArrInputGlobal);
                    if (globalBufferedImage != null) {
                        File globalFile = new File(strGlobalPicPathName);
                        if (globalFile != null) {
                            ImageIO.write(globalBufferedImage, "jpg", globalFile);
                            imgUrl(strGlobalPicPathName);
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            /////////////// 保存人脸图 /////////////////////////
            if (faceRecognitionInfo.stuObject.stPicInfo != null) {
                String strPersonPicPathName =
                        path + "\\" + faceRecognitionInfo.UTC.toStringTitle() + "_FaceRecognition_Person.jpg";
                faceInfo.setPersionImgUrl(strPersonPicPathName);
                byte[] bufferPerson =
                        pBuffer.getByteArray(
                                faceRecognitionInfo.stuObject.stPicInfo.dwOffSet,
                                faceRecognitionInfo.stuObject.stPicInfo.dwFileLenth);
                ByteArrayInputStream byteArrInputPerson = new ByteArrayInputStream(bufferPerson);

                try {
                    personBufferedImage = ImageIO.read(byteArrInputPerson);
                    if (personBufferedImage != null) {
                        File personFile = new File(strPersonPicPathName);
                        if (personFile != null) {
                            ImageIO.write(personBufferedImage, "jpg", personFile);
                            imgUrl(strPersonPicPathName);
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            ///////////// 保存对比图 //////////////////////
            if (faceRecognitionInfo.nRetCandidatesExNum > 0
                    && faceRecognitionInfo.stuCandidatesEx != null) {
                int maxValue = -1;

                // 设备可能返回多张图片，这里只显示相似度最高的
                int[] nSimilary = new int[faceRecognitionInfo.nRetCandidatesExNum];
                for (int i = 0; i < faceRecognitionInfo.nRetCandidatesExNum; i++) {
                    nSimilary[i] = faceRecognitionInfo.stuCandidatesEx[i].bySimilarity & 0xff;
                }

                for (int i = 0; i < nSimilary.length; i++) {
                    if (maxValue < nSimilary[i]) {
                        maxValue = nSimilary[i];
                        index = i;
                    }
                }

                String strCandidatePicPathName =
                        path
                                + "\\"
                                + faceRecognitionInfo.UTC.toStringTitle()
                                + "_FaceRecognition_Candidate.jpg";
                faceInfo.setCandidateImgUrl(strCandidatePicPathName);
                // 每个候选人的图片个数：faceRecognitionInfo.stuCandidatesEx[index].stPersonInfo.wFacePicNum，
                // 正常情况下只有1张。如果有多张，此demo只显示第一张
                byte[] bufferCandidate =
                        pBuffer.getByteArray(
                                faceRecognitionInfo.stuCandidatesEx[index].stPersonInfo.szFacePicInfo[0].dwOffSet,
                                faceRecognitionInfo
                                        .stuCandidatesEx[index]
                                        .stPersonInfo
                                        .szFacePicInfo[0]
                                        .dwFileLenth);
                ByteArrayInputStream byteArrInputCandidate = new ByteArrayInputStream(bufferCandidate);

                try {
                    candidateBufferedImage = ImageIO.read(byteArrInputCandidate);
                    if (candidateBufferedImage != null) {
                        File candidateFile = new File(strCandidatePicPathName);
                        if (candidateFile != null) {
                            ImageIO.write(candidateBufferedImage, "jpg", candidateFile);
                            imgUrl(strCandidatePicPathName);
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }


        /**
         * 保存人脸检测事件图片
         *
         * @param pBuffer        抓拍图片信息
         * @param dwBufSize      抓拍图片大小
         * @param faceDetectInfo 人脸检测事件信息
         */
        public void saveFaceDetectPic(
                Pointer pBuffer, int dwBufSize, NetSDKLib.DEV_EVENT_FACEDETECT_INFO faceDetectInfo)
                throws FileNotFoundException {
            System.out.println("人脸检测事件===>信息：" + JSON.toJSONString(faceDetectInfo));
            File path = new File("./FaceDetection/");
            if (!path.exists()) {
                path.mkdir();
            }

            if (pBuffer == null || dwBufSize <= 0) {
                return;
            }

            // 小图的 stuObject.nRelativeID 来匹配大图的 stuObject.nObjectID，来判断是不是 一起的图片
            if (groupId != faceDetectInfo.stuObject.nRelativeID) { // /->保存全景图
                personBufferedImage = null;
                groupId = faceDetectInfo.stuObject.nObjectID;

                String strGlobalPicPathName =
                        path + "\\" + faceDetectInfo.UTC.toStringTitle() + "_FaceDetection_Global.jpg";
                byte[] bufferGlobal = pBuffer.getByteArray(0, dwBufSize);
                ByteArrayInputStream byteArrInputGlobal = new ByteArrayInputStream(bufferGlobal);

                try {
                    globalBufferedImage = ImageIO.read(byteArrInputGlobal);
                    if (globalBufferedImage != null) {
                        File globalFile = new File(strGlobalPicPathName);
                        if (globalFile != null) {
                            imgUrl(strGlobalPicPathName);
                            ImageIO.write(globalBufferedImage, "jpg", globalFile);
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            } else if (groupId == faceDetectInfo.stuObject.nRelativeID) { // /->保存人脸图
                if (faceDetectInfo.stuObject.stPicInfo != null) {
                    String strPersonPicPathName =
                            path + "\\" + faceDetectInfo.UTC.toStringTitle() + "_FaceDetection_Person.jpg";
                    byte[] bufferPerson = pBuffer.getByteArray(0, dwBufSize);
                    ByteArrayInputStream byteArrInputPerson = new ByteArrayInputStream(bufferPerson);

                    try {
                        personBufferedImage = ImageIO.read(byteArrInputPerson);
                        if (personBufferedImage != null) {
                            File personFile = new File(strPersonPicPathName);
                            if (personFile != null) {
                                imgUrl(strPersonPicPathName);
                                ImageIO.write(personBufferedImage, "jpg", personFile);
                            }
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }


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
        FaceRecognitionController faceRecognitionController = new FaceRecognitionController();
        faceRecognitionController.faceRecognition();
    }
}
