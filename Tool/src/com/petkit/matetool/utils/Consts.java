package com.petkit.matetool.utils;

public class Consts {

	/**
	 MATE版本说明：

	 v1.11说明：
	 1. 修复空指针问题，MATE发送的wifi列表中mac可能为空

	 */
	public static final String TOOL_MATE_VERSION	= "1.1";

	/**
	 GO版本说明：

	 */
	public static final String TOOL_GO_VERSION	= "1.0";

	/**
	 喂食器版本说明：

	 v1.11说明：
	 1. 设备信息中新增chipid信息及相关处理
	 2. 增加测试者账号名显示

	 */
	public static final String TOOL_FEEDER_VERSION	= "1.11";


	/**
	 宠物窝版本说明：

	 v1.0说明:
	 1. 添加猫窝产测的全流程

	 v1.1说明：
	 1. 修改制冷、制热的成功判定；
	 2. 温度传感器测试修改为先自动判断是否异常，再人工判定；
	 3. 去除wifi按键的相关测试内容；
	 4. 添加老化数据处理和上传；
	 5. 支持设置打印机的参数；

	 v1.2说明
	 1. 修改制冷、制热的判断条件，电压区间从大于5000修改为大于5000小于7000
	 2. 修改制冷、制热的判断条件，电流区间从150~650修改为150~800
	 3. 设备信息中新增chipid信息及相关处理，不影响测试流程

	 v1.3说明：
	 1. 修改风扇的判断条件，改为自动判定
	 2. 连接设备后，166指令增加风扇判定的参数设定
	 */
	public static final String TOOL_COZY	= "1.3";
	
}
