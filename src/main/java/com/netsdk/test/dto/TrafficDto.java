package com.netsdk.test.dto;

import com.netsdk.lib.NetSDKLib;
import com.sun.jna.NativeLong;
import lombok.Data;

@Data
public class TrafficDto {
    private String event_name;
    private String plate_number;
    private String plate_type;
    private String plate_color;
    private String vehicle_color;
    private String vehicle_type;
    private String vehicle_size;
    private String file_count;
    private String file_index;
    private String group_id;
    private String illegal_place;
    private String lane_number;
    private NetSDKLib.NET_TIME_EX utc;
    private String picenable;
    private int offset;
    private int filelength;
    private NetSDKLib.DH_RECT bounding_box;
    private String plat_img_id;
    private String big_img_id;
    private String info_addr_gps;
    private String info_addr;
    private String info_equip_code;
    private String[] other_info = new String[1];

    public void setPlateTypeData(int plateType) {
        switch (plateType) {
            case 0:
                setPlate_type("PLATE_TYPE_UNKNOWN,0,未知");
                break;
            case 1:
                setPlate_type("PLATE_TYPE_NORMAL,1,蓝牌黑牌");
                break;
            case 2:
                setPlate_type("PLATE_TYPE_YELLOW,2,黄牌");
                break;
            case 3:
                setPlate_type("PLATE_TYPE_DOUBLEYELLOW,3,双层黄色牌");
                break;
            case 4:
                setPlate_type("PLATE_TYPE_POLICE,4,警牌");
                break;
            case 5:
                setPlate_type("PLATE_TYPE_ARMED,5,武警牌");
                break;
            case 6:
                setPlate_type("PLATE_TYPE_MILITARY,6,部队号牌");
                break;
            case 7:
                setPlate_type("PLATE_TYPE_DOUBLEMILITARY,7,部队双层");
                break;
            case 8:
                setPlate_type("PLATE_TYPE_SAR,8,港澳特区号牌");
                break;
            case 9:
                setPlate_type("PLATE_TYPE_TRAINNING,9,教练车号牌");
                break;
            case 10:
                setPlate_type("PLATE_TYPE_PERSONAL,10,个性号牌");
                break;
            case 11:
                setPlate_type("PLATE_TYPE_AGRI,11,农用牌");
                break;
            case 12:
                setPlate_type("PLATE_TYPE_EMBASSY,12,使馆号牌");
                break;
            case 13:
                setPlate_type("PLATE_TYPE_MOTO,13,摩托车号牌");
                break;
            case 14:
                setPlate_type("PLATE_TYPE_TRACTOR,14,拖拉机号牌");
                break;
            case 15:
                setPlate_type("PLATE_TYPE_OFFICIALCAR,15,公务车");
                break;
            case 16:
                setPlate_type("PLATE_TYPE_PERSONALCAR,16,私家车");
                break;
            case 17:
                setPlate_type("PLATE_TYPE_WARCAR,17,军用");
                break;
            case 18:
                setPlate_type("PLATE_TYPE_OTHER,18,其他号牌");
                break;
            case 19:
                setPlate_type("PLATE_TYPE_CIVILAVIATION,19,民航号牌");
                break;
            case 20:
                setPlate_type("PLATE_TYPE_BLACK,20,黑牌");
                break;
            case 21:
                setPlate_type("PLATE_TYPE_PURENEWENERGYMICROCAR,21,纯电动新能源小车");
                break;
            case 22:
                setPlate_type("PLATE_TYPE_MIXEDNEWENERGYMICROCAR,22,混合新能源小车");
                break;
            case 23:
                setPlate_type("PLATE_TYPE_PURENEWENERGYLARGECAR,23,纯电动新能源大车");
                break;
            case 24:
                setPlate_type("PLATE_TYPE_MIXEDNEWENERGYLARGECAR,24,混合新能源大车");
                break;
            default:
                break;
        }
    }

    public void setPlateColorData(int plateColor) {
        switch (plateColor) {
            case 0:
                setPlate_color("PLATE_COLOR_OTHER,0,其他颜色");
                break;
            case 1:
                setPlate_color("PLATE_COLOR_BLUE,1,蓝色");
                break;
            case 2:
                setPlate_color("PLATE_COLOR_YELLOW,2,黄色");
                break;
            case 3:
                setPlate_color("PLATE_COLOR_WHITE,3,白色");
                break;
            case 4:
                setPlate_color("PLATE_COLOR_BLACK,4,黑色");
                break;
            case 5:
                setPlate_color("PLATE_COLOR_YELLOW_BOTTOM_BLACK_TEXT,5,黄底黑字");
                break;
            case 6:
                setPlate_color("PLATE_COLOR_BLUE_BOTTOM_WHITE_TEXT,6,蓝底白字");
                break;
            case 7:
                setPlate_color("PLATE_COLOR_BLACK_BOTTOM_WHITE_TEXT,7,黑底白字");
                break;
            case 8:
                setPlate_color("PLATE_COLOR_SHADOW_GREEN,8,渐变绿");
                break;
            case 9:
                setPlate_color("PLATE_COLOR_YELLOW_GREEN,9,黄绿双拼");
                break;
            default:
                break;
        }
    }

    public void setVehileType(int vehicleType) {
        switch (vehicleType) {
            case 0:
                setVehicle_type("VEHICLE_TYPE_UNKNOW,0,未知类型");
                break;
            case 1:
                setVehicle_type("VEHICLE_TYPE_MOTOR,1,机动车");
                break;
            case 2:
                setVehicle_type("VEHICLE_TYPE_NON_MOTOR,2,非机动车");
                break;
            case 3:
                setVehicle_type("VEHICLE_TYPE_BUS,3,公交车");
                break;
            case 4:
                setVehicle_type("VEHICLE_TYPE_BICYCLE,4,自行车");
                break;
            case 5:
                setVehicle_type("VEHICLE_TYPE_MOTORCYCLE,5,摩托车");
                break;
            case 6:
                setVehicle_type("VEHICLE_TYPE_UNLICENSEDMOTOR,6,无牌机动车");
                break;
            case 7:
                setVehicle_type("VEHICLE_TYPE_LARGECAR,7,大型汽车");
                break;
            case 8:
                setVehicle_type("VEHICLE_TYPE_MICROCAR,8,小型汽车");
                break;
            case 9:
                setVehicle_type("VEHICLE_TYPE_EMBASSYCAR,9,使馆汽车");
                break;
            case 10:
                setVehicle_type("VEHICLE_TYPE_MARGINALCAR,10,领馆汽车");
                break;
            case 11:
                setVehicle_type("VEHICLE_TYPE_AREAOUTCAR,11,境外汽车");
                break;
            case 12:
                setVehicle_type("VEHICLE_TYPE_FOREIGNCAR,12,外籍汽车");
                break;
            case 13:
                setVehicle_type("VEHICLE_TYPE_DUALTRIWHEELMOTORCYCLE,13,两、三轮摩托车");
                break;
            case 14:
                setVehicle_type("VEHICLE_TYPE_LIGHTMOTORCYCLE,14,轻便摩托车");
                break;
            case 15:
                setVehicle_type("VEHICLE_TYPE_EMBASSYMOTORCYCLE,15,使馆摩托车");
                break;
            case 16:
                setVehicle_type("VEHICLE_TYPE_MARGINALMOTORCYCLE,16,领馆摩托车");
                break;
            case 17:
                setVehicle_type("VEHICLE_TYPE_AREAOUTMOTORCYCLE,17,境外摩托车");
                break;
            case 18:
                setVehicle_type("VEHICLE_TYPE_FOREIGNMOTORCYCLE,18,外籍摩托车");
                break;
            case 19:
                setVehicle_type("VEHICLE_TYPE_FARMTRANSMITCAR,19,农用运输车");
                break;
            case 20:
                setVehicle_type("VEHICLE_TYPE_TRACTOR,20,拖拉机");
                break;
            case 21:
                setVehicle_type("VEHICLE_TYPE_TRAILER,21,挂车");
                break;
            case 22:
                setVehicle_type("VEHICLE_TYPE_COACHCAR,22,教练汽车");
                break;
            case 23:
                setVehicle_type("VEHICLE_TYPE_COACHMOTORCYCLE,23,教练摩托车");
                break;
            case 24:
                setVehicle_type("VEHICLE_TYPE_TRIALCAR,24,试验汽车");
                break;
            case 25:
                setVehicle_type("VEHICLE_TYPE_TRIALMOTORCYCLE,25,试验摩托车");
                break;
            case 26:
                setVehicle_type("VEHICLE_TYPE_TEMPORARYENTRYCAR,26,临时入境汽车");
                break;
            case 27:
                setVehicle_type("VEHICLE_TYPE_TEMPORARYENTRYMOTORCYCLE,27,临时入境摩托车");
                break;
            case 28:
                setVehicle_type("VEHICLE_TYPE_TEMPORARYSTEERCAR,28,临时行驶车");
                break;
            case 29:
                setVehicle_type("VEHICLE_TYPE_PASSENGERCAR,29,客车");
                break;
            case 30:
                setVehicle_type("VEHICLE_TYPE_LARGETRUCK,30,大货车");
                break;
            case 31:
                setVehicle_type("VEHICLE_TYPE_MIDTRUCK,31,中货车");
                break;
            case 32:
                setVehicle_type("VEHICLE_TYPE_SALOONCAR,32,轿车");
                break;
            case 33:
                setVehicle_type("VEHICLE_TYPE_MICROBUS,33,面包车");
                break;
            case 34:
                setVehicle_type("VEHICLE_TYPE_MICROTRUCK,34,小货车");
                break;
            case 35:
                setVehicle_type("VEHICLE_TYPE_TRICYCLE,35,三轮车");
                break;
            case 36:
                setVehicle_type("VEHICLE_TYPE_PASSERBY,36,行人");
                break;
            default:
                break;
        }
    }

    public void setVehicleColorData(int vehicleColor) {
        switch (vehicleColor) {
            case 0:
                setVehicle_color("VEHICLE_COLOR_OTHER,0,其他颜色");
                break;
            case 1:
                setVehicle_color("VEHICLE_COLOR_WHITE,1,白色");
                break;
            case 2:
                setVehicle_color("VEHICLE_COLOR_BLACK,2,黑色");
                break;
            case 3:
                setVehicle_color("VEHICLE_COLOR_RED,3,红色");
                break;
            case 4:
                setVehicle_color("VEHICLE_COLOR_YELLOW,4,黄色");
                break;
            case 5:
                setVehicle_color("VEHICLE_COLOR_GRAY,5,灰色");
                break;
            case 6:
                setVehicle_color("VEHICLE_COLOR_BLUE,6,蓝色");
                break;
            case 7:
                setVehicle_color("VEHICLE_COLOR_GREEN,7,绿色");
                break;
            case 8:
                setVehicle_color("VEHICLE_COLOR_PINK,8,粉红色");
                break;
            case 9:
                setVehicle_color("VEHICLE_COLOR_PURPLE,9,紫色");
                break;
            case 10:
                setVehicle_color("VEHICLE_COLOR_BROWN,10,棕色");
                break;
            default:
                break;
        }
    }

}
