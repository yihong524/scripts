import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.apache.commons.lang.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
// 获取自定义数据====================================================================================================
String bank = vars.get("bank"); //机构名称
String reqDate = vars.get("reqDate"); //日期
String batch = vars.get("batch"); //批次
String userNo=vars.get("userNo"); //串配号
String userName=vars.get("userName"); //串配号的名称
String certNo=vars.get("certNo"); //身份证号
String subChannel=vars.get("subChannel"); //二级请求机构代码
String subChannelPayDate = vars.get("subChannelPayDate"); //用户交款日期
String billAmt = vars.get("billAmount");
String period = vars.get("period");  //帐期
String barcode = vars.get("barcode");  //条形码
String billPlanID = vars.get("billPlanID");  //账单计划ID
String bossSignNo = vars.get("bossSignNo"); //第三方签约协议号
String bossOrgCode = vars.get("bossOrgCode"); //子公司代码
String yfProcessNo = vars.get("yfProcessNo"); //亿付业务处理号 ??
//文件本地存放路径
String baseDir = vars.get("path");
// //默认数据=========================================================================================================
String tnxType = "00B30510"; //交易识别码, boss代扣结果文件
String tnxTypeFFT = "L101000"; //付费通老代扣结果： 成功销账
String billBrh = "888880003502900"; //出账机构代码, 固定值
String totalNum = "000001"; //总记录数, 目前只支持1条数据
String commentsTotal = "000"; //备注

//=========================银行设置===================================================================================
//农行
String abcCode="508290060120001"; //农行渠道机构代码
String abcFilePath="/ori-data/ftp-test/bill/abc/withhold_result/"; //农行代扣结果文件ftp路径
//招行
String cmbcCode="508290060120026"; //招行渠道机构代码
String cmbcFilePath="/ori-data/ftp-test/bill/cmbc/withhold_result/"; //招行代扣结果文件ftp路径
//中国银行
String bocCode="508290060120023"; //中国银行渠道机构代码
String bocFilePath="/ori-data/ftp-test/bill/boc/withhold_result/"; //中国银行代扣结果文件ftp路径
//上海银行
String bosCode="508290060120024"; //上海银行渠道机构代码
String bosFilePath="/ori-data/ftp-test/bill/bos/withhold_result/"; //代扣结果文件ftp路径
//工商银行
String icbcCode="508290060120002"; //工商银行渠道机构代码
String icbcFilePath="/ori-data/ftp-test/bill/icbc/withhold_result/"; //代扣结果文件ftp路径
//付费通老代扣
String fftFilePath="/ori-data/ftp-test/bill/fft/withhold_result/"; //代扣结果文件ftp路径
//浦发银行
String spdbCode="508290060120025"; //浦发外部渠道机构代码
String spdbFilePath="/ori-data/ftp-test/bill/spdb/withhold_result/"; //代扣结果文件ftp路径
//民生银行
String minshengCode="508290060120004"; //民生外部渠道机构代码
String minshengFilePath="/ori-data/ftp-test/bill/minsheng/withhold_result/"; //代扣结果文件ftp路径

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

	public static void withHold(String bank){
		String reqBrhCode = "";
		String filePath = "";
		// String fileBatch = new SimpleDateFormat("yyyyMMddSSS").format(new Date());  //文件批号 ，11位
		String channelPayDate = reqDate;  //渠道扣款日期
		String channelSettleDate = reqDate; //渠道清算日期
		String channelSerialNo = PrefixSpace("", 20); //渠道扣款流水号
		String channelResult = "1"; //渠道扣款结果: 1 - 成功； 0 - 失败
		String reasonCode = PrefixSpace("", 4); //退票原因, 默认空白
		
		if(bank.equals("abc")){
			reqBrhCode = abcCode; // 农行渠道机构代码
			filePath = abcFilePath; // 串配文件ftp路径
		}else if(bank.equals("cmbc")){
			reqBrhCode = cmbcCode; // 招行渠道机构代码
			filePath = cmbcFilePath; // 串配文件ftp路径
		}else if(bank.equals("boc")){
			reqBrhCode = bocCode; // 中国银行渠道机构代码
			filePath = bocFilePath; // 串配文件ftp路径
		}else if(bank.equals("spdb")){  //浦发代扣
			reqBrhCode = spdbCode;
			filePath = spdbFilePath;
		}else if(bank.equals("minsheng")){ //民生代扣
			reqBrhCode = minshengCode;
			filePath = minshengFilePath;
		}else if(bank.equals("bos")){  //上海银行
			reqBrhCode = bosCode;
			filePath = bosFilePath;
		}

		// 文件名组装
		String fileName = reqBrhCode + reqDate + "00" + batch + "." + tnxType;

		// 汇总 没有失败笔数
		String totalLine = reqBrhCode + reqDate+ "0" + batch + totalNum + PrefixInteger(billAmt, 14) + totalNum + PrefixInteger(billAmt, 14)
			+ "000000" + "00000000000000" + commentsTotal + "\n";

		// 明细  
		// 渠道签约号 农行 - 用户证号 + 身份证
		String detailLine = billBrh + PrefixSpace(userNo, 20) + PrefixSpace(userName, 10)
		+ PrefixSpace((userNo + certNo), 40) + PrefixSpace(yfProcessNo, 20) + PrefixSpace(userNo, 40) + PrefixInteger(billAmt, 10)
		+ period + channelPayDate + channelSettleDate + channelSerialNo + channelResult + reasonCode;

		//判断subChannel不为空时，明细里面增加二级渠道报备字段
		if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
			detailLine = detailLine + PrefixSpace(subChannel, 15) + subChannelPayDate + commentsTotal;
		} else {
			detailLine = detailLine + commentsTotal;
		}

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine+detailLine),localFilePath);
		String remoteFilePath = filePath + fileName;
		//参数返回
		vars.put("localFilePath", localFilePath);
		vars.put("remoteFilePath", remoteFilePath);
	} 

	public static void withHoldICBC(){
		String reqBrhCode = icbcCode;
		// String filePath = icbcFilePath;  //工行代扣结果文件要手动上传到运营
		// // String fileBatch = new SimpleDateFormat("yyyyMMddSSS").format(new Date());  //文件批号 ，11位
		String channelPayDate = reqDate;  //渠道扣款日期
		// String channelSettleDate = reqDate; //渠道清算日期
		// String channelSerialNo = PrefixSpace("", 20); //渠道扣款流水号
		// String channelResult = "1"; //渠道扣款结果: 1 - 成功； 0 - 失败
		// String reasonCode = PrefixSpace("", 4); //退票原因, 默认空白
		String successCode = "2"; //成功=2
		String payOrderNo = yfProcessNo.substring(1); // 扣款银行账号,19位, 对应为亿付支付号
		String fileNo = "0" + batch; 

		
		// 文件名组装
		String fileName = "DSFB" + "0837" + "." + "003" + "." + fileNo;

		// 明细
		String detailLine = PrefixSpace(successCode, 1) + PrefixSpace(payOrderNo, 19) + "2" + PrefixSpace(reqDate, 8)
			+ PrefixInteger(billAmt, 12) + "1" + "0837003" + PrefixInteger(fileNo, 5) + "0" + "000" + period
			+ PrefixInteger(userNo, 13);

		// //判断subChannel不为空时，明细里面增加二级渠道报备字段
		// if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
		// 	detailLine = detailLine + PrefixSpace(subChannel, 15) + subChannelPayDate + commentsTotal;
		// } else {
		// 	detailLine = detailLine + commentsTotal;
		// }

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile(detailLine,localFilePath);
		String remoteFilePath = filePath + fileName;
		//参数返回
		// vars.put("localFilePath", localFilePath);
		// vars.put("remoteFilePath", remoteFilePath);
	}

	public static void withHoldFFT(){
		// String reqBrhCode = "";
		String filePath = fftFilePath;
		// String fileBatch = new SimpleDateFormat("yyyyMMddSSS").format(new Date());  //文件批号 ，11位
		String channelPayDate = reqDate;  //渠道扣款日期
		// String channelSettleDate = reqDate; //渠道清算日期
		// String channelSerialNo = PrefixSpace("", 20); //渠道扣款流水号
		// String channelResult = "1"; //渠道扣款结果: 1 - 成功； 0 - 失败
		// String reasonCode = PrefixSpace("", 4); //退票原因, 默认空白
		String withHoldCategory = "1"; //销账种类 0－代收销帐 1－代扣销帐 2－充值
		String withHoldType = "1"; //销账方式: 0－根据条码销帐  1－根据号码种类＋号码＋帐期销帐  2－根据号码种类＋号码充值
		String bankAcct =new SimpleDateFormat("yyyyMMmmss").format(new Date()); // 扣款银行账号,10位
		String payType = "00"; //缴费方式  00－现金  01－银行卡/帐户  02－行业卡



		// 文件名组装
		String fileName = billBrh + reqDate + batch + "." + tnxTypeFFT;

		// 汇总 没有失败笔数
		String totalLine = billBrh + reqDate + batch + totalNum + PrefixInteger(billAmt, 14) + commentsTotal + "\n";

		// 明细  
		String detailLine = withHoldCategory + withHoldType + billBrh + PrefixSpace(barcode, 34) + PrefixSpace(period, 6)
			+ "00" + "0" + PrefixSpace(yfProcessNo, 30) + PrefixInteger(billAmt, 10) + PrefixSpace(reqDate, 8)
			+ "777777777777777" + PrefixSpace("", 8) + PrefixSpace(bankAcct, 40) + PrefixSpace(payType, 2);

		//判断subChannel不为空时，明细里面增加二级渠道报备字段
		if (subChannel.length() != 0 && subChannelPayDate.length() != 0) {
			detailLine = detailLine + PrefixSpace(subChannel, 15) + subChannelPayDate + commentsTotal;
		} else {
			detailLine = detailLine + commentsTotal;
		}

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine+detailLine),localFilePath);
		String remoteFilePath = filePath + fileName;
		//参数返回
		vars.put("localFilePath", localFilePath);
		vars.put("remoteFilePath", remoteFilePath);
	}
}

if (bank.equals("abc") || bank.equals("cmbc") || bank.equals("boc") || bank.equals("spdb") || bank.equals("minsheng") || bank.equals("bos")) {
	Tools.withHold(bank);
} else if (bank.equals("icbc")) {
	Tools.withHoldICBC();
} else if (bank.equals("fft")) {
	Tools.withHoldFFT();
}
