package com.netsdk.test;

import com.alibaba.fastjson.JSON;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;

import java.util.Calendar;
import java.util.Date;

public class GateModule {
    /**
     * 查询刷卡记录，获取查询句柄
     * @return
     */
    public static NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[] findRecords(NetSDKLib.NET_TIME startTime, NetSDKLib.NET_TIME endTime) {
        // 接口入参
        NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX findCondition = new NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX();
        findCondition.bCardNoEnable = 0;
        findCondition.stStartTime = startTime;
        findCondition.stEndTime = endTime;
        // CLIENT_FindRecord 接口入参
        NetSDKLib.NET_IN_FIND_RECORD_PARAM stIn = new NetSDKLib.NET_IN_FIND_RECORD_PARAM();
        stIn.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARDREC_EX;
        stIn.pQueryCondition = findCondition.getPointer();

        // CLIENT_FindRecord 接口出参
        NetSDKLib.NET_OUT_FIND_RECORD_PARAM stOut = new NetSDKLib.NET_OUT_FIND_RECORD_PARAM();
        findCondition.write();

        NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[] pstRecordEx = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[0];
        // 获取查询句柄
        if(LoginModule.netsdk.CLIENT_FindRecord(LoginModule.m_hLoginHandle, stIn, stOut, 5000)) {
            findCondition.read();

            // 用于申请内存，假定2000次刷卡记录
            int nFindCount = 2000;
            NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[] pstRecord = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[nFindCount];
            for(int i = 0; i < nFindCount; i++) {
                pstRecord[i] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC();
            }

            NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM stNextIn = new NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM();
            stNextIn.lFindeHandle = stOut.lFindeHandle;
            stNextIn.nFileCount = nFindCount;

            NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM stNextOut = new NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM();
            stNextOut.nMaxRecordNum = nFindCount;
            // 申请内存
            stNextOut.pRecordList = new Memory(pstRecord[0].dwSize * nFindCount);
            stNextOut.pRecordList.clear(pstRecord[0].dwSize * nFindCount);

            // 将数组内存拷贝给指针
            ToolKits.SetStructArrToPointerData(pstRecord, stNextOut.pRecordList);

            if(LoginModule.netsdk.CLIENT_FindNextRecord(stNextIn, stNextOut, 5000)) {
                if(stNextOut.nRetRecordNum == 0) {
                    return pstRecordEx;
                }
                // 获取卡信息
                ToolKits.GetPointerDataToStructArr(stNextOut.pRecordList, pstRecord);

                // 获取有用的信息
                pstRecordEx = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[stNextOut.nRetRecordNum];
                for(int i = 0; i < stNextOut.nRetRecordNum; i++) {
                    pstRecordEx[i] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC();
                    pstRecordEx[i] = pstRecord[i];
                }
            }

            LoginModule.netsdk.CLIENT_FindRecordClose(stOut.lFindeHandle);
        }
        return pstRecordEx;
    }

    public static void main(String[] args) {
        boolean flag = LoginModule.login("192.168.101.251", 37777, "admin", "zhwl1234");
        Calendar calendar =Calendar.getInstance();
        if (flag) {
            // 检索结束时间：当前时间
            Date endDateTime = calendar.getTime();
            NetSDKLib.NET_TIME endTime = new NetSDKLib.NET_TIME();
            endTime.setTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

            // 获取检索开始时间：当前时间 - 5分钟
            calendar.add(Calendar.MINUTE, -5);
            Date startDateTime = calendar.getTime();
            NetSDKLib.NET_TIME startTime = new NetSDKLib.NET_TIME();
            startTime.setTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

            NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[] cardRecords = GateModule.findRecords(startTime, endTime);
            System.out.println(JSON.toJSONString(cardRecords));

            for (NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC cardRecord : cardRecords) {
                // 卡号
                String cardNo = new String(cardRecord.szCardNo).trim();
                // 如果刷卡状态不正确，或者是按键开门，就不录入数据
                if (cardRecord.bStatus != 1 || "00000000".equals(cardNo)) {
                    continue;
                }
                // 刷卡时间 转换为北京时间 +8小时
//                Date stuTime = DateUtil.convertStringToDate(cardRecord.stuTime.toStringTimeEx(), "yyyy-MM-dd HH:mm:ss");
//                Calendar stuCalendar = DateUtil.getCalendar(stuTime);
//                stuCalendar.add(Calendar.HOUR, 8);
            }
        }
    }
}