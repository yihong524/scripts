import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.apache.commons.lang.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
// 获取自定义数据====================================================================================================
// String bank = vars.get("bank"); //机构名称
String reqDate=vars.get("reqDate"); //日期
String batch=vars.get("batch"); //批次
// String userNo=vars.get("userNo"); //串配号
// String userName=vars.get("userName"); //串配号的名称
// String certNo=vars.get("certNo"); //身份证号
// String subChannel=vars.get("subChannel"); //二级请求机构代码
String billAmt = vars.get("billAmount");
String period = vars.get("period");  //帐期
String barcode = vars.get("barcode");  //条形码
String billPlanID = vars.get("billPlanID");  //账单计划ID
String bossSignNo = vars.get("bossSignNo"); //第三方签约协议号
String bossOrgCode = vars.get("bossOrgCode"); //子公司代码
//文件本地存放路径
String baseDir = vars.get("path");
// //默认数据=========================================================================================================
String tnxType="00B30500 "; //交易识别码, boss代扣文件
String billBrh="888880003502900"; //出账机构代码, 固定值
String totalNum="000001"; //总记录数, 目前只支持1条数据
String commentsTotal="000"; //备注
String bossFilePath = "/ori-data/ftp-test/bill/boss/withhold_request/"; //BOSS代扣请求文件路径

//===================================================================================================================
//保存文件成gbk， 填充0或者空格

public class Tools {
	public static void transferFile(String srcString, String destFileName) {
		FileOutputStream fos = new FileOutputStream(destFileName);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "GBK");
		osw.write(srcString);
		osw.flush();
	}

	public static String PrefixInteger(String srcString, int length) {
		/*
		 * 例如，要求：6位编号自动生成，递增，格式为“000001”。 解释：0代表前面要补的字符，6代表字符串长度，d表示参数为整数类型
		 */
		return StringUtils.leftPad(srcString, length, "0");
	}

	public static String PrefixSpace(String srcString, int length) {
		int a = (srcString.getBytes().length - srcString.length())/2;
		if (a > 0) {
			// 如果包含N个中文字符(一个中文 占两位长度)，则值域长度减少N位(length = length - N)
			length = length - a;
		}
		// 右侧补 空字符串 至达到长度length
		return StringUtils.rightPad(srcString, length, " ");
	}

	public static void bossWithholdReq(){
		String bossSerialNum = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); //boss 扣款请求流水，随机14位
		String billStart = "201801"; //计费开始月份， 写死
		String billEnd = "201812";  //计费结束月份，写死
		String billType = "0 "; //账单类型
		
		// 文件名组装
		String fileName = billBrh + reqDate + batch + "." + tnxType;

		// 汇总
		String totalLine = billBrh + reqDate + totalNum + PrefixInteger(billAmt, 14) + commentsTotal + "\n";

		// 明细
		String detailLine = PrefixSpace(bossSerialNum, 20) + period + billStart + billEnd + billType + barcode 
			+ PrefixSpace(billPlanID, 20) + "000000000000000" + PrefixSpace(billPlanID, 20) + PrefixSpace(bossSignNo, 20)
			+ PrefixInteger(billAmt, 10) + PrefixSpace(bossOrgCode, 15) + commentsTotal;
		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine+detailLine),localFilePath);
		String remoteFilePath = bossFilePath + fileName;
		//参数返回
		vars.put("localFilePath", localFilePath);
		vars.put("remoteFilePath", remoteFilePath);
	} 
}

Tools.bossWithholdReq();