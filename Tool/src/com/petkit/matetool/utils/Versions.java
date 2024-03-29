package com.petkit.matetool.utils;

public class Versions {

	/**
	 MATE版本说明：

	 v1.1说明：
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

	 v1.12说明：
	 1. 喂食器增加老化数据的处理逻辑

	 v1.13说明：
	 1. 优化成品测试中的信息存储逻辑

	 v1.14说明：
	 1. 优化账号管理机制；

	 v1.15说明：
	 1. 解决写入SN的问题

	 v1.16说明：
	 1. 新增工具内切换WiFi的功能，按设备类型自动过滤WiFi
	 2. 修复自动横屏问题

	 */
	public static final String TOOL_FEEDER_VERSION	= "1.16";


	/**
	 宠物窝版本说明：

	 1.0说明:
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

	 v1.4说明：
	 1. 修改制冷、制热的判断条件，电流区间从150~800修改为150~1500

	 v1.5说明：
	 1. 修改接口调用的bug
	 2. 可以对半成品、成品进行维修

	 v1.6说明：
	 1. 宠物窝测试的首页去除老化数据展示；
	 2. 成品测试、维修和抽检增加老化结果的测试项，需要人工判断结果。

	 v1.7说明：
	 1. 调整Socket心跳包发送间隔，改为3秒一次；

	 v1.8说明：
	 1. 优化成品测试中的信息存储逻辑

	 v1.9说明：
	 1. 优化账号管理机制；

	 v1.10说明：
	 1. 升级为宠物窝new，SN规则的产品位标识从"B改为C"

	 v1.11说明：
	 1. 新增工具内切换WiFi的功能，按设备类型自动过滤WiFi
	 2. 修复自动横屏问题
	 */
	public static final String TOOL_COZY	= "1.11";


	/**
	 喂食器Mini版本说明：

	 v1.1说明：
	 1. 修改门测试、叶轮马达测试的过程，增加手动判断；
	 2. 维修支持半成品状态、成品状态和出货状态的设备；
	 3. 优化账号管理机制；

	 v1.2说明：
	 1. 新增临时数据，防止写入SN时设备写入成功，但是没通知App的情况；
	 * 当该情况发生时，需按照如下步骤进行补救：1. 重启设备；2. 进入抽检，连接设备AP；3. 连接AP后，工具自动识别出异常设备，并保存设备数据；4. 打印标签。
	 * 注意这个过程中不能杀掉产测App，否则临时数据将丢失，以后无法再自动保存设备数据了。

	 v1.3说明：
	 1. 新增工具内切换WiFi的功能，按设备类型自动过滤WiFi
	 2. 修复自动横屏问题
	 */
	public static final String TOOL_FEEDER_MINI_VERSION	= "1.3";


	/**
	 *
	 1.0说明:
	 1. 添加K2产测的全流程

	 1.1说明:
	 1. 放宽电压判定范围
	 2. 自动项测试增加保护

	 v1.2说明：
	 1. 新增工具内切换WiFi的功能，按设备类型自动过滤WiFi
	 2. 修复自动横屏问题

	 v1.3说明：
	 1. 写入SN成功以后增加读取校验过程，确保写入和读取完全一致。
	 2. 维修中增加重写SN

	 v1.4说明：
	 1. 写入SN改成扫码
	 2. 修改SN规则
	 *
	 */
	public static final String TOOL_K2_VERSION	= "1.4";


	/**
	 *
	 * 1.0说明:
	 * 1. 支持小佩全自动猫厕所（T3）产测全流程
	 *
	 * 1.1说明：
	 * 1. 调整秤校准和马达测试的顺序
	 * 2. 优化秤校准过程，区分半成品、成品
	 * 3. WiFi选择界面增加信号强度显示
	 *
	 * 1.2说明：
	 * 1. 修复部分机型偶现的闪退问题
	 * 2. 修复偶现的错误数据上传问题
	 *
	 *
	 v1.3说明：
	 1. 写入SN改成扫码
	 2. 修改SN规则
	 *
	 */
	public static final String TOOL_T3_VERSION	= "1.3";


	/**
	 *
	 * 1.0说明:
	 * 1. 支持智能鱼缸的整机测试
	 *
	 v1.2说明：
	 1. 写入SN改成扫码
	 2. 修改SN规则
	 *
	 */
	public static final String TOOL_AQ_VERSION	= "1.1";


	/**
	 *
	 * 1.0说明:
	 * 1. 支持小佩行星喂食器（D3）产测全流程
	 *
	 * 1.1说明：
	 * 1. 调整测试项顺序
	 * 2. 抽检中去除打印SN
	 * 3. 马达测试中增加光栅步数显示
	 * 4. WiFi列表增加信号排序
	 *
	 *
		 v1.2说明：
		 1. 写入SN改成扫码
		 2. 修改SN规则
	 *
	 */
	public static final String TOOL_D3_VERSION	= "1.2";





	/**
	 *
	 * 1.0说明:
	 * 1. 支持小佩喂食器SOLO（D4）产测全流程
	 *
	 *
		 v1.1说明：
		 1. 写入SN改成扫码
		 2. 修改SN规则
	 */
	public static final String TOOL_D4_VERSION	= "1.1";

	/**
	 *
	 * 1.0说明:
	 * 1. 支持智能饮水机（W5）产测全流程
	 *
	 * 2.0说明：
	 * 1. W5和W5C拆分成两个入口
	 * 2. 修复SN或MAC重复时无法重置的问题
	 *
	 * 2.1说明：
	 * 1. W5/W5C使用独立账号
	 * 2. 移除W5产测功能
	 *
	 *
	 * v2.2说明：
	 * 1. 写入SN改成扫码
	 * 2. 修改SN规则
	 *
	 */
	public static final String TOOL_W5_VERSION	= "2.2";

	/**
	 *
	 * 1.0说明:
	 * 1. 支持智能猫狗牌（P3）产测全流程
	 *
	 * 2.0说明：
	 * 1. 拆分为P3C和P3D，区分猫牌和狗牌
	 *
	 * 2.1说明：
	 * 1. P3C/P3D使用独立账户
	 *
	 * 2.2说明：
	 * 1. 解决MAC/SN重复无法处理的问题
	 *
	 * 2.3说明：
	 * 1. 解决P3D中无法搜索到P3C名称的设备，P3C是默认名称
	 *
	 * 2.4说明：
	 * 1. 解决MAC/SN重复无法处理的问题
	 *
	 *
		v2.5说明：
		1. 写入SN改成扫码
		2. 修改SN规则
	 	3. 增加版本号显示
	 	4. 增加SN写入校验

	 */
	public static final String TOOL_P3_VERSION	= "2.5";

	/**
	 *
	 * 1.0说明:
	 * 1. 支持智能猫厕所（T4）产测全流程
	 *
	 * 1.1说明：
	 * 1. 解决T4设备的账号问题
	 *
	 * 1.2说明：
	 * 1. 解决MAC/SN重复无法处理的问题
	 *
	 */
	public static final String TOOL_T4_VERSION	= "1.3";


	/**
	 *
	 * 1.0说明:
	 * 1. 支持智能净味器（K3）产测全流程
	 *
	 * 1.1说明：
	 * 1. 解决MAC/SN重复无法处理的问题
	 *
	 * 1.2说明：
	 * 1. 解决MAC/SN重复无法处理的问题
	 *
	 * 1.3说明：
	 * 1. 增加SN写入结果校验
	 */
	public static final String TOOL_K3_VERSION	= "1.3";


	/**
	 *
	 * 1.0说明:
	 * 1. 支持智能鱼缸（AQR）产测全流程
	 *
	 * 1.1说明：
	 * 1. 解决MAC/SN重复无法处理的问题
	 *
	 * 1.2说明：
	 * 1. 解决MAC重复无法处理的问题
	 */
	public static final String TOOL_AQR_VERSION	= "1.3";


	/**
	 *
	 * 1.0说明:
	 * 1. 支持智能鱼缸（AQ1S）产测全流程
	 *
	 * 1.1说明：
	 * 1. 解决MAC/SN重复无法处理的问题
	 *
	 * 1.2说明：
	 * 1. 写入SN改成扫描二维码的方式
	 *
	 * *
	 * 		 v1.3说明：
	 * 		 1. 写入SN改成扫码
	 * 		 2. 修改SN规则
	 * 		 3. 增加版本号显示
	 * 		 4. 增加SN写入校验
	 */
	public static final String TOOL_AQ1S_VERSION	= "1.3";



	/**
	 *
	 * 1.0说明:
	 * 1. 支持智能鱼缸（R2）产测全流程
	 *
	 * 1.1说明：
	 * 1. 调整电压范围，1.2 ~ 2.0
	 *
	 * 1.2说明：
	 * 1. 修复MAC重复无法处理的问题
	 *
	 * 1.3说明：
	 * 1. 修复自动测试时指令紊乱的问题
	 *
	 * *
	 * 		 v1.4说明：
	 * 		 1. 写入SN改成扫码
	 * 		 2. 修改SN规则
	 * 		 3. 增加版本号显示
	 * 		 4. 增加SN写入校验
	 *
	 */
	public static final String TOOL_R2_VERSION	= "1.3";



	/**
	 *
	 * 1.0说明:
	 * 1. 支持智能鱼缸（W5N/W4X）产测全流程
	 *
	 * 1.1说明：
	 * 1. 修复MAC重复无法处理的问题
	 *
	 * *
	 * 		 v1.2说明：
	 * 		 1. 写入SN改成扫码
	 * 		 2. 修改SN规则
	 * 		 3. 增加版本号显示
	 * 		 4. 增加SN写入校验
	 *
	 *
	 * v1.3说明：
	 * 1. 支持W4X-UV
	 */
	public static final String TOOL_W5N_VERSION	= "1.3";


	/**
	 * 1.0说明:
	 *
	 * 1. 支持智能变频加热棒（AQ-H1）产测全功能
	 *
	 * 1.2:
	 * 1. 新增温度单位设置
	 * 2. 区分国内和海外版本
	 *
	 */
	public static final String TOOL_AQH1_VERSION	= "1.2";



	/**
	 * 1.0说明：
	 *
	 * 1. 支持小佩智能饮水机SOLO 2（CTW2）产测全功能
	 *
	 *
		 v1.1说明：
		 1. 写入SN改成扫码
		 2. 修改SN规则
		 3. 增加版本号显示
		 4. 增加SN写入校验
	 */
	public static final String TOOL_CTW2_VERSION	= "1.1";


	/**
	 * 1.0说明：
	 *
	 * 1. 支持小佩智能喂食器SOLO NEW（D4-1）产测全功能
	 *
	 *
	 */
	public static final String TOOL_D4_1_VERSION	= "1.0";


	/**
	 *
	 * v1.0
	 * 1. D3-1废弃，沿用D3即可。
	 *
	 */
	public static final String TOOL_D3_1_VERSION	= "1.0";


	/**
	 *
	 * v1.0
	 * 1. 支持D4S产测全功能
	 *
	 */
	public static final String TOOL_D4S_VERSION	= "1.0";


	/**
	 *
	 * v1.0
	 * 1. 支持HG产测全功能
	 *
	 * v1.1
	 * 1. 新增激活状态测试项
	 * 2. HG220v中新增抽检功能
	 *
	 * v1.2
	 * 1. 新增老化测试
	 *
	 * v1.3
	 * 1. 新增HG-p
	 *
	 */
	public static final String TOOL_HG_VERSION	= "1.3";


	/**
	 *
	 * v1.0
	 * 1. 支持CTW3产测全功能
	 *
	 */
	public static final String TOOL_CTW3_VERSION	= "0.1";

	/**
	 *
	 * v1.0
	 * 1. 支持D4SH产测全功能
	 *
	 * v1.1
	 * 1. 支持D4SH海外
	 *
	 */
	public static final String TOOL_D4SH_VERSION	= "1.1";

	/**
	 * 1.0说明：
	 *
	 * 1. 支持小佩智能喂食器SOLO NEW（D4-2）产测全功能
	 *
	 *
	 */
	public static final String TOOL_D4_2_VERSION	= "1.0";

	/**
	 *
	 * v1.0
	 * 1. 支持D4H产测全功能
	 *
	 * v1.1
	 * 1. 支持D4H海外
	 *
	 */
	public static final String TOOL_D4H_VERSION	= "1.1";

}
