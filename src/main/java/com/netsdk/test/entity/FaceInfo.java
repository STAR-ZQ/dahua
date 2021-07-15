package com.netsdk.test.entity;

import com.netsdk.lib.NetSDKLib;
import lombok.Data;

import java.awt.image.BufferedImage;

@Data
public class FaceInfo {
    public static final int NET_MAX_FACEDETECT_FEATURE_NUM = 32;   // 人脸特征最大个数

    public int emSex;                            // 性别, 参考 EM_DEV_EVENT_FACEDETECT_SEX_TYPE
    public int nAge;                            // 年龄,-1表示该字段数据无效
    public int nFeatureValidNum;            // 人脸特征数组有效个数,与 emFeature 结合使用
    public int[] emFeature = new int[NET_MAX_FACEDETECT_FEATURE_NUM];   // 人脸特征数组,与 nFeatureValidNum 结合使用, 参考 EM_DEV_EVENT_FACEDETECT_FEATURE_TYPE
    public int emRace;                            // 肤色, 参考 EM_RACE_TYPE
    public int emEye;                            // 眼睛状态, 参考 EM_EYE_STATE_TYPE
    public int emMouth;                        // 嘴巴状态, 参考 EM_MOUTH_STATE_TYPE
    public int emMask;                            // 口罩状态, 参考 EM_MASK_STATE_TYPE
    public int emBeard;                        // 胡子状态, 参考 EM_BEARD_STATE_TYPE
    public int nAttractive;                    // 魅力值, -1表示无效, 0未识别，识别时范围1-100,得分高魅力高
    public byte[] bReserved1 = new byte[4];       // 保留字节
    public NetSDKLib.NET_EULER_ANGLE stuFaceCaptureAngle;            // 人脸在抓拍图片中的角度信息, nPitch:抬头低头的俯仰角, nYaw左右转头的偏航角, nRoll头在平面内左偏右偏的翻滚角
    // 角度值取值范围[-90,90], 三个角度值都为999表示此角度信息无效
    public int nFaceQuality;                    // 人脸抓拍质量分数
    public int nFaceAlignScore;                // 人脸对齐得分分数,范围 0~10000,-1为无效值
    public int nFaceClarity;                   // 人脸清晰度分数,范围 0~10000,-1为无效值

    public double dbTemperature;                    // 温度, bAnatomyTempDetect 为TRUE时有效
    public int bAnatomyTempDetect;                // 是否人体测温
    public int emTemperatureUnit;              // 温度单位, bAnatomyTempDetect 为TRUE时有效
    public boolean bIsOverTemp;                    // 是否超温, bAnatomyTempDetect 为TRUE时有效
    public boolean bIsUnderTemp;                   // 是否低温, bAnatomyTempDetect 为TRUE时有效
    public byte[] bReserved = new byte[76];       // 保留字节,留待扩展.
    // 全景图
    private String globalImgUrl;

    // 人脸图
    private String persionImgUrl;

    // 候选人图
    private String candidateImgUrl;
}
