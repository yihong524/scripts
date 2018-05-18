import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.apache.commons.lang.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
// // 获取自定义数据====================================================================================================
String bank = vars.get("bank"); //机构名称
String reqDate = vars.get("reqDate"); //日期
String batch = vars.get("batch"); //批次
// String userNo=vars.get("userNo"); //串配号
// String userName=vars.get("userName"); //串配号的名称
// String certNo=vars.get("certNo"); //身份证号
String subChannel=vars.get("subChannel"); //二级请求机构代码
String subChannelPayDate = vars.get("subChannelPayDate"); //用户交款日期
String barcode = vars.get("barcode");  //条形码
String billAmt = vars.get("billAmount");
String barcode2 = vars.get("barcode2");  //第二张条形码
String billAmt2 = vars.get("billAmount2"); //第二张账单金额
// String period = vars.get("period");  //帐期
// String billPlanID = vars.get("billPlanID");  //账单计划ID
// String bossSignNo = vars.get("bossSignNo"); //第三方签约协议号
// String bossOrgCode = vars.get("bossOrgCode"); //子公司代码
// String yfProcessNo = vars.get("yfProcessNo"); //亿付业务处理号 ??
//文件本地存放路径
String baseDir = vars.get("path");
// // //默认数据=========================================================================================================
// String tnxType = "00B30510"; //交易识别码, boss代扣结果文件
// String tnxTypeFFT = "L101000"; //付费通老代扣结果： 成功销账
// String billBrh = "888880003502900"; //出账机构代码, 固定值
String totalNum = "000001"; //总记录数, 默认一条
// String commentsTotal = "000"; //备注

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

	public static void collectICBC() {
		String orgCode = "9002032"; //工行代收
		String tnxType = "CSH"; //文件类型
		String failedNum = "000000"; //失败数
		String failedAmt = "00000000000"; //失败金额
		String supplyOrgCode = "000000180510000"; //15位
		String serialNo = new SimpleDateFormat("HHmmSSS").format(new Date()); //7位随机数
		String serialNo2 = new SimpleDateFormat("HHmmSSS").format(new Date()); //7位随机数
		String totalAmt = "";
		if (barcode2.length() != 0) {
			totalAmt = String.valueOf(Integer.parseInt(billAmt) + Integer.parseInt(billAmt2)); //总额
			totalNum = "000002"; //记录数改成2
		} else {
			totalAmt = billAmt; 
		}

		// 文件名组装
		String fileName = batch + orgCode + reqDate.substring(2) + "." + tnxType;

		// 汇总 没有失败笔数
		String totalLine = "S" + batch + orgCode + reqDate + totalNum + PrefixInteger(totalAmt, 11) + failedNum + failedAmt;

		// 明细  
		String detailLine = "000000" + reqDate.substring(2) + "000" + PrefixInteger(serialNo, 7) + "00000000"
			+ orgCode + "00000000000000000" + reqDate.substring(0, 6) + "0101" + PrefixInteger(billAmt, 9) + "000000000"
			+ PrefixSpace("", 8) + "00" + orgCode + PrefixInteger(barcode, 34);

		//判断subChannel不为空时，明细里面增加二级渠道报备字段
		if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
			detailLine = detailLine + PrefixSpace(subChannel, 15) + subChannelPayDate;
		}

		//判断是否有第二条数据
		if (barcode2.length() != 0) {
			String detailLine2 = "000000" + reqDate.substring(2) + "000" + PrefixInteger(serialNo2, 7) + "00000000"
			+ orgCode + "00000000000000000" + reqDate.substring(0, 6) + "0101" + PrefixInteger(billAmt2, 9) + "000000000"
			+ PrefixSpace("", 8) + "00" + orgCode + PrefixInteger(barcode2, 34);

			//判断subChannel不为空时，明细里面增加二级渠道报备字段
			if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
				detailLine2 = detailLine2 + PrefixSpace(subChannel, 15) + subChannelPayDate;
			}
			detailLine = detailLine + "\n" + detailLine2;
		}

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((detailLine + "\n" + totalLine),localFilePath);
		SampleResult.setDataEncoding("UTF-8");
		SampleResult.setResponseData("工行代收文件生成成功!\n" + "文件名:  " + fileName + "\n" + "文件路径:  " + baseDir);
	}

	public static void collectCEB() {
		String chlSubCode = "WY";
		String totalAmt = "";
		if (barcode2.length() != 0) {
			totalAmt = String.valueOf(Integer.parseInt(billAmt) + Integer.parseInt(billAmt2)); //总额
			totalNum = "000002"; //记录数改成2
		} else {
			totalAmt = billAmt; 
		}

		// 文件名组装
		String fileName = "GDYH" + reqDate + "036" + batch + ".CSH";

		// 汇总 没有失败笔数
		String totalLine = "F" + reqDate + "821000" + "036" + batch + totalNum + PrefixInteger(totalAmt, 14);

		//明细
		String detailLine = "T" + reqDate + barcode + chlSubCode;

		//判断subChannel不为空时，明细里面增加二级渠道报备字段
		if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
			detailLine = detailLine + PrefixSpace(subChannel, 15) + subChannelPayDate;
		}

		//判断是否有第二条数据
		if (barcode2.length() != 0) {
			String detailLine2 = "T" + reqDate + barcode2 + chlSubCode;

			//判断subChannel不为空时，明细里面增加二级渠道报备字段
			if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
				detailLine2 = detailLine2 + PrefixSpace(subChannel, 15) + subChannelPayDate;
			}
			detailLine = detailLine + "\n" + detailLine2;
		}

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine + "\n" + detailLine),localFilePath);
		SampleResult.setDataEncoding("UTF-8");
		SampleResult.setResponseData("光大代收文件生成成功!\n" + "文件名:  " + fileName + "\n" + "文件路径:  " + baseDir);
	}

	public static void collectFFT() {
		String billBrh = "888880003502900"; //出账机构代码, 固定值
		String totalAmt = "";
		if (barcode2.length() != 0) {
			totalAmt = String.valueOf(Integer.parseInt(billAmt) + Integer.parseInt(billAmt2)); //总额
			totalNum = "000002"; //记录数改成2
		} else {
			totalAmt = billAmt; 
		}

		// 文件名组装
		String fileName = billBrh + reqDate + batch + ".B101000";

		// 汇总 没有失败笔数
		String totalLine = billBrh + reqDate + batch + totalNum + PrefixInteger(totalAmt, 14) + "000";

		//明细
		String detailLine = "00" + billBrh + barcode + PrefixSpace("", 6) + batch + PrefixSpace("", 31)
			+ PrefixInteger(billAmt, 10) + reqDate + "888880201000020" + PrefixSpace("1998", 8)
			+ PrefixSpace("", 40) + "00" + "000";

		//判断subChannel不为空时，明细里面增加二级渠道报备字段
		if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
			detailLine = detailLine + PrefixSpace(subChannel, 15) + subChannelPayDate;
		}

		//判断是否有第二条数据
		if (barcode2.length() != 0) {
			String detailLine2 = "00" + billBrh + barcode2 + PrefixSpace("", 6) + batch + PrefixSpace("", 31)
			+ PrefixInteger(billAmt2, 10) + reqDate + "888880201000020" + PrefixSpace("1998", 8)
			+ PrefixSpace("", 40) + "00" + "000";

			//判断subChannel不为空时，明细里面增加二级渠道报备字段
			if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
				detailLine2 = detailLine2 + PrefixSpace(subChannel, 15) + subChannelPayDate;
			}
			detailLine = detailLine + "\n" + detailLine2;
		}

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine + "\n" + detailLine),localFilePath);
		SampleResult.setDataEncoding("UTF-8");
		SampleResult.setResponseData("付费通代收文件生成成功!\n" + "文件名:  " + fileName + "\n" + "文件路径:  " + baseDir);
	}

	public static void collectYouju() {
		String totalAmt = "";
		if (barcode2.length() != 0) {
			totalAmt = String.valueOf(Integer.parseInt(billAmt) + Integer.parseInt(billAmt2)); //总额
			totalNum = "000002"; //记录数改成2
		} else {
			totalAmt = billAmt; 
		}

		// 文件名组装
		String fileName = "DFYX" + reqDate + "023" + batch + ".CSH";

		// 汇总 没有失败笔数
		String totalLine = "F" + reqDate + "084001" + "023" + batch + totalNum + PrefixInteger(totalAmt, 14);

		//明细
		String detailLine = "T" + reqDate + barcode;

		//判断subChannel不为空时，明细里面增加二级渠道报备字段
		if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
			detailLine = detailLine + PrefixSpace(subChannel, 15) + subChannelPayDate;
		}

		//判断是否有第二条数据
		if (barcode2.length() != 0) {
			String detailLine2 = "T" + reqDate + barcode2;

			//判断subChannel不为空时，明细里面增加二级渠道报备字段
			if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
				detailLine2 = detailLine2 + PrefixSpace(subChannel, 15) + subChannelPayDate;
			}
			detailLine = detailLine + "\n" + detailLine2;
		}

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine + "\n" + detailLine),localFilePath);
		SampleResult.setDataEncoding("UTF-8");
		SampleResult.setResponseData("邮局代收文件生成成功!\n" + "文件名:  " + fileName + "\n" + "文件路径:  " + baseDir);
	}

	public static void collectLianhua() {
		String totalAmt = "";
		if (barcode2.length() != 0) {
			totalAmt = String.valueOf(Integer.parseInt(billAmt) + Integer.parseInt(billAmt2)); //总额
			totalNum = "000002"; //记录数改成2
		} else {
			totalAmt = billAmt; 
		}

		// 文件名组装
		String fileName = "LHKK" + reqDate + ".CSH";

		// 汇总 没有失败笔数
		String totalLine = "F" + reqDate + "084002" + "023" + batch + totalNum + PrefixInteger(totalAmt, 14);

		//明细
		String detailLine = "T" + reqDate + barcode;

		//判断subChannel不为空时，明细里面增加二级渠道报备字段
		if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
			detailLine = detailLine + PrefixSpace(subChannel, 15) + subChannelPayDate;
		}

		//判断是否有第二条数据
		if (barcode2.length() != 0) {
			String detailLine2 = "T" + reqDate + barcode2;

			//判断subChannel不为空时，明细里面增加二级渠道报备字段
			if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
				detailLine2 = detailLine2 + PrefixSpace(subChannel, 15) + subChannelPayDate;
			}
			detailLine = detailLine + "\n" + detailLine2;
		}

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine + "\n" + detailLine),localFilePath);
		SampleResult.setDataEncoding("UTF-8");
		SampleResult.setResponseData("联华快客代收文件生成成功!\n" + "文件名:  " + fileName + "\n" + "文件路径:  " + baseDir);
	}

}

if (bank.equals("icbc")) {  //工行代收
	Tools.collectICBC();
} else if (bank.equals("ceb")) {  //光大代收
	Tools.collectCEB();
} else if (bank.equals("fft")) {  //付费通代收
	Tools.collectFFT();
} else if (bank.equals("youju")) {  //邮局代收
	Tools.collectYouju();
} else if (bank.equals("lianhua")) {  //联华快客代收
	Tools.collectLianhua();
}