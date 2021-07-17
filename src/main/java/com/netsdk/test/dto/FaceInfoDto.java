package com.netsdk.test.dto;

import lombok.Data;

@Data
public class FaceInfoDto {
    private String sex;
    private int age;
    private String[] face_feature = new String[15];
    private String race;
    private String eye;
    private String mouth;
    private String mask;
    private String beard;
    private int attractive;
    private FaceCaptureAngle face_captur_eangle;
    private int face_quality;
    private int face_align_score;
    private int face_clarity;
    private int temperature_unit;
    private String is_overtemp;
    private String is_undertemp;
    private String info_addr_gps;
    private String info_addr;
    private String info_equip_code;
    private String info_image_id;
    /**
     * 扩展字段  暂定长度为1
     */
    private String[] other_props = new String[1];

    /**
     * 根据编号code替换性别具体信息
     *
     * @param sex
     */
    public void setSexData(int sex) {
        switch (sex) {
            case 0:
                setSex("SEX_UNKNOWN,0,未知");
                break;
            case 1:
                setSex("SEX_MAN,1,男性");
                break;
            case 2:
                setSex("SEX_WOMAN,2,女性");
                break;
            default:
                break;
        }
    }

    /**
     * 肤色
     *
     * @param race
     */
    public void setRaceData(int race) {
        switch (race) {
            case 0:
                setRace("RACE_UNKNOWN,0,未知");
                break;
            case 1:
                setRace("RACE_NODISTI,1,未识别");
                break;
            case 2:
                setRace("RACE_YELLOW,2,黄");
                break;
            case 3:
                setRace("RACE_BLACK,3,黑");
                break;
            case 4:
                setRace("RACE_WHITE,4,白");
                break;
            default:
                break;
        }
    }

    /**
     * 眼睛
     *
     * @param eye
     */
    public void setEyeData(int eye) {
        switch (eye) {
            case 0:
                setEye("EYE_UNKNOWN,0,未知");
                break;
            case 1:
                setEye("EYE_NODISTI,1,未识别");
                break;
            case 2:
                setEye("EYE_CLOSE,2,闭眼");
                break;
            case 3:
                setEye("EYE_OPEN,3,睁眼");
                break;
            default:
                break;
        }
    }

    /**
     * 嘴巴
     *
     * @param mouth
     */
    public void setMouthData(int mouth) {
        switch (mouth) {
            case 0:
                setMouth("MOUTH_UNKNOWN,0,未知");
                break;
            case 1:
                setMouth("MOUTH_NODISTI,1,未识别");
                break;
            case 2:
                setMouth("MOUTH_CLOSE,2,闭嘴");
                break;
            case 3:
                setMouth("MOUTH_OPEN,3,张嘴");
                break;
            default:
                break;
        }
    }

    /**
     * 口罩
     *
     * @param mask
     */
    public void setMaskData(int mask) {
        switch (mask) {
            case 0:
                setMask("MASK_UNKNOWN,0,未知");
                break;
            case 1:
                setMask("MASK_NODISTI,1,未识别");
                break;
            case 2:
                setMask("MASK_NOMASK,2,没戴口罩");
                break;
            case 3:
                setMask("MASK_WEAR,3,戴口罩");
                break;
            default:
                break;
        }
    }

    /**
     * 胡子
     *
     * @param beard
     */
    public void setBeardData(int beard) {
        switch (beard) {
            case 0:
                setBeard("BEARD_UNKNOWN,0,未知");
                break;
            case 1:
                setBeard("BEARD_NODISTI,1,未识别");
                break;
            case 2:
                setBeard("BEARD_NOBEARD,2,没胡子");
                break;
            case 3:
                setBeard("BEARD_HAVEBEARD,3,有胡子");
                break;
            default:
                break;
        }
    }

    /**
     * 人脸特征
     *
     * @param features
     */
    public void setFeaturesData(int features) {
        System.out.println(features);
        String featuresData = "";
        switch (features) {
            case 0:
                featuresData = "FEATURE_UNKNOWN,0,未知";
                break;
            case 1:
                featuresData = "FEATURE_WEAR_GLASSES,1,戴眼镜";
                break;
            case 2:
                featuresData = "FEATURE_SMILE,2,微笑";
                break;
            case 3:
                featuresData = "FEATURE_ANGER,3,愤怒";
                break;
            case 4:
                featuresData = "FEATURE_SADNESS,4,悲伤";
                break;
            case 5:
                featuresData = "FEATURE_DISGUST,5,厌恶";
                break;
            case 6:
                featuresData = "FEATURE_FEAR,6,害怕";
                break;
            case 7:
                featuresData = "FEATURE_SURPRISE,7,惊讶";
                break;
            case 8:
                featuresData = "FEATURE_NEUTRAL,8,正常";
                break;
            case 9:
                featuresData = "FEATURE_LAUGH,9,大笑";
                break;
            case 10:
                featuresData = "FEATURE_NOGLASSES,10,没戴眼镜";
                break;
            case 11:
                featuresData = "FEATURE_HAPPY,11,高兴";
                break;
            case 12:
                featuresData = "FEATURE_CONFUSED,12,困惑";
                break;
            case 13:
                featuresData = "FEATURE_SCREAM,13,尖叫";
                break;
            case 14:
                featuresData = "FEATURE_WEAR_SUNGLASSES,14,戴太阳眼镜";
                break;
            default:
                break;
        }
        getFace_feature()[features] = featuresData;
    }

    @Data
    public static class FaceCaptureAngle {
        private int nPitch;
        private int nYaw;
        private int nRoll;
    }
}
