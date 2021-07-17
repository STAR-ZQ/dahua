package com.netsdk.test.dto;

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
    private String utc;
    private String picenable;
    private String offset;
    private String filelength;
    private String bounding_box;
    private String plat_img_id;
    private String big_img_id;
    private String info_addr_gps;
    private String info_addr;
    private String info_equip_code;
    private String[] other_info = new String[100];

    public void setPlateTypeData(int plateType) {
        switch (plateType) {
            case 0:
                setPlate_type("未知");
                break;
            case 1:
                setPlate_type("蓝牌黑牌");
                break;
            case 2:
                setPlate_type("黄牌");
                break;
            case 3:
                setPlate_type("双层黄色牌");
                break;
            case 4:
                setPlate_type("警牌");
                break;
            case 5:
                setPlate_type("武警牌");
                break;
            case 6:
                setPlate_type("部队号牌");
                break;
            case 7:
                setPlate_type("部队双层");
                break;
            case 8:
                setPlate_type("港澳特区号牌");
                break;
            case 9:
                setPlate_type("教练车号牌");
                break;
            case 10:
                setPlate_type("个性号牌");
                break;
            case 11:
                setPlate_type("农用牌");
                break;
            case 12:
                setPlate_type("使馆号牌");
                break;
            case 13:
                setPlate_type("摩托车号牌");
                break;
            case 14:
                setPlate_type("拖拉机号牌");
                break;
            case 15:
                setPlate_type("公务车");
                break;
            case 16:
                setPlate_type("私家车");
                break;
            case 17:
                setPlate_type("军用");
                break;
            case 18:
                setPlate_type("其他号牌");
                break;
            case 19:
                setPlate_type("民航号牌");
                break;
            case 20:
                setPlate_type("黑牌");
                break;
            case 21:
                setPlate_type("纯电动新能源小车");
                break;
            case 22:
                setPlate_type("混合新能源小车");
                break;
            case 23:
                setPlate_type("纯电动新能源大车");
                break;
            case 24:
                setPlate_type("混合新能源大车");
                break;
            default:
                break;
        }
    }

    public void setPlateColorData(int plateColor) {
        switch (plateColor) {
            case 0:
                setPlate_color("其他颜色");
                break;
            case 1:
                setPlate_color("蓝色");
                break;
            case 2:
                setPlate_color("黄色");
                break;
            case 3:
                setPlate_color("白色");
                break;
            case 4:
                setPlate_color("黑色");
                break;
            case 5:
                setPlate_color("黄底黑字");
                break;
            case 6:
                setPlate_color("蓝底白字");
                break;
            case 7:
                setPlate_color("黑底白字");
                break;
            case 8:
                setPlate_color("渐变绿");
                break;
            case 9:
                setPlate_color("黄绿双拼");
                break;
            default:
                break;
        }
    }

    public void setVehileType(int vehicleType) {
        switch (vehicleType) {
            case 0:
                setVehicle_type("未知类型");
                break;
            case 1:
                setVehicle_type("机动车");
                break;
            case 2:
                setVehicle_type("非机动车");
                break;
            case 3:
                setVehicle_type("公交车");
                break;
            case 4:
                setVehicle_type("自行车");
                break;
            case 5:
                setVehicle_type("摩托车");
                break;
            case 6:
                setVehicle_type("无牌机动车");
                break;
            case 7:
                setVehicle_type("大型汽车");
                break;
            case 8:
                setVehicle_type("小型汽车");
                break;
            case 9:
                setVehicle_type("使馆汽车");
                break;
            case 10:
                setVehicle_type("领馆汽车");
                break;
            case 11:
                setVehicle_type("境外汽车");
                break;
            case 12:
                setVehicle_type("外籍汽车");
                break;
            case 13:
                setVehicle_type("两、三轮摩托车");
                break;
            case 14:
                setVehicle_type("轻便摩托车");
                break;
            case 15:
                setVehicle_type("使馆摩托车");
                break;
            case 16:
                setVehicle_type("领馆摩托车");
                break;
            case 17:
                setVehicle_type("境外摩托车");
                break;
            case 18:
                setVehicle_type("外籍摩托车");
                break;
            case 19:
                setVehicle_type("农用运输车");
                break;
            case 20:
                setVehicle_type("拖拉机");
                break;
            case 21:
                setVehicle_type("挂车");
                break;
            case 22:
                setVehicle_type("教练汽车");
                break;
            case 23:
                setVehicle_type("教练摩托车");
                break;
            case 24:
                setVehicle_type("试验汽车");
                break;
            case 25:
                setVehicle_type("试验摩托车");
                break;
            case 26:
                setVehicle_type("临时入境汽车");
                break;
            case 27:
                setVehicle_type("临时入境摩托车");
                break;
            case 28:
                setVehicle_type("临时行驶车");
                break;
            case 29:
                setVehicle_type("客车");
                break;
            case 30:
                setVehicle_type("大货车");
                break;
            case 31:
                setVehicle_type("中货车");
                break;
            case 32:
                setVehicle_type("轿车");
                break;
            case 33:
                setVehicle_type("面包车");
                break;
            case 34:
                setVehicle_type("小货车");
                break;
            case 35:
                setVehicle_type("三轮车");
                break;
            case 36:
                setVehicle_type("行人");
                break;
            default:
                break;
        }
    }

    public void setVehicleColorData(int vehicleColor) {
        switch (vehicleColor) {
            case 0:
                setVehicle_color("其他颜色");
                break;
            case 1:
                setVehicle_color("白色");
                break;
            case 2:
                setVehicle_color("黑色");
                break;
            case 3:
                setVehicle_color("红色");
                break;
            case 4:
                setVehicle_color("黄色");
                break;
            case 5:
                setVehicle_color("灰色");
                break;
            case 6:
                setVehicle_color("蓝色");
                break;
            case 7:
                setVehicle_color("绿色");
                break;
            case 8:
                setVehicle_color("粉红色");
                break;
            case 9:
                setVehicle_color("紫色");
                break;
            case 10:
                setVehicle_color("棕色");
                break;
            default:
                break;
        }
    }

}
