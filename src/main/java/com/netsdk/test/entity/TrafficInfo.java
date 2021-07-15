package com.netsdk.test.entity;

import com.netsdk.lib.NetSDKLib;
import lombok.Data;

@Data
public class TrafficInfo {
    private String m_EventName;         	  // 事件名称
    private String m_PlateNumber;       	  // 车牌号
    private String m_PlateType;               // 车牌类型
    private String m_PlateColor;      	  	  // 车牌颜色
    private String m_VehicleColor;    	  	  // 车身颜色
    private String m_VehicleType;       	  // 车身类型
    private String m_VehicleSize;     	  	  // 车辆大小
    private String m_FileCount;				  // 文件总数
    private String m_FileIndex;				  // 文件编号
    private String m_GroupID;				  // 组ID
    private String m_IllegalPlace;			  // 违法地点
    private String m_LaneNumber;              // 通道号
    private NetSDKLib.NET_TIME_EX m_Utc;      // 事件时间
    private int m_bPicEnble;       	  		  // 车牌对应信息，BOOL类型
    private int m_OffSet;          	  		  // 车牌偏移量
    private int m_FileLength;                 // 文件大小
    private NetSDKLib.DH_RECT m_BoundingBox;  // 包围盒
    /**
     * 车牌图片路径
     */
    private String platImgUrl;
    /**
     * 全景图路径
     */
    private String BigImgUrl;
}
