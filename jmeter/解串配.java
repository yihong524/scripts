import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.apache.commons.lang.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
// 获取自定义数据====================================================================================================
String bank = vars.get("bank"); //机构名称
String reqDate=vars.get("reqDate"); //日期
String batch=vars.get("batch"); //批次
String userNo=vars.get("userNo"); //串配号
String userName=vars.get("userName"); //串配号的名称
String certNo=vars.get("certNo"); //身份证号
String billPlanID = vars.get("billPlanID"); // 账单计划ID 上海银行需要
String subChannel=vars.get("subChannel"); //二级请求机构代码
//文件本地存放路径
String baseDir = vars.get("path");
//默认数据=========================================================================================================
String tnxType="00B30300"; //交易识别码, 解串配
String tnxTypeBOS="01B30300"; //上海银行交易识别码, 解串配
String tnxTypeFFT = "B102000"; //付费通交易代码，串配
String billBrh="888880003502900"; //出账机构代码, 固定值
// String fileNo=String.valueOf(${__time(yyyyMMddss,)}); //文件批号 YYYYMMDDNN
String totalNum="000001"; //总记录数, 目前只支持1条数据
String commentsTotal="000"; //备注
String certType="01"; // 默认身份证类型

//=========================银行设置===================================================================================
//农行
String abcCode="508290060120001"; //农行渠道机构代码
String abcFilePath="/ori-data/ftp-test/bill/abc/unsign_request/"; //农行结果文件ftp路径
//招行
String cmbcCode="508290060120026"; //招行渠道机构代码
String cmbcFilePath="/ori-data/ftp-test/bill/cmbc/unsign_request/"; //招行结果文件ftp路径
//中国银行
String bocCode="508290060120023"; //中国银行渠道机构代码
String bocFilePath="/ori-data/ftp-test/bill/boc/unsign_request/"; //中国银行代扣结果文件ftp路径
//上海银行
String bosCode="508290060120024"; //上海银行渠道机构代码
String bosFilePath="/ori-data/ftp-test/bill/bos/unsign_request/"; //结果文件ftp路径
//工商银行
String icbcCode="508290060120002"; //工商银行渠道机构代码
String icbcFilePath="/ori-data/ftp-test/bill/icbc/sign_request/"; //结果文件ftp路径  工商银行解串配和串配是同一个目录
//付费通老代扣
String fftFilePath="/ori-data/ftp-test/bill/fft/sign_request/"; //结果文件ftp路径  付费通解串配和串配是同一个目录
//浦发银行 浦发没有文件串解配
String spdbCode="508290060120025"; //浦发外部渠道机构代码
String spdbFilePath="/ori-data/ftp-test/bill/spdb/sign_request/"; //结果文件ftp路径
//民生银行 民生没有文件串解配
String minshengCode="508290060120004"; //民生外部渠道机构代码
String minshengFilePath="/ori-data/ftp-test/bill/minsheng/sign_request/"; //结果文件ftp路径

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
	//农行、招行、中国银行报文组装
	public static void unsign(String bank){
		String reqBrhCode = "";
		String filePath = "";
		
		if(bank.equals("abc")){
			 reqBrhCode = abcCode; // 农行渠道机构代码
			 filePath = abcFilePath; // 串配文件ftp路径
		}else if(bank.equals("cmbc")){
			 reqBrhCode = cmbcCode; // 招行渠道机构代码
			 filePath = cmbcFilePath; // 串配文件ftp路径
		}else if(bank.equals("boc")){
			 reqBrhCode = bocCode; // 中国银行渠道机构代码
			 filePath = bocFilePath; // 串配文件ftp路径
		}

		// 文件名组装
		String fileName = reqBrhCode + reqDate + batch + "." + tnxType;

		// 汇总
		String totalLine = reqBrhCode + reqDate + batch + totalNum + commentsTotal + "\n";

		// 明细
		String detailLine = billBrh + PrefixSpace(userNo, 20) + PrefixSpace(userName, 10)
		+ PrefixSpace((userNo + certNo), 40) + PrefixSpace("", 20);

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine+detailLine),localFilePath);
		String remoteFilePath = filePath + fileName;
		//参数返回
		vars.put("localFilePath", localFilePath);
		vars.put("remoteFilePath", remoteFilePath);
	}
	//上海银行报文组装
	public static void unsignBOS(){
		String reqBrhCode = bosCode; // 上海银行渠道机构代码
		String filePath = bosFilePath; // 上海银行串配文件ftp路径
		String serialNum = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + "0001"; // 随机21位,上海银行串配凭证

		// 文件名组装
		String fileName = reqBrhCode + reqDate + batch + "." + tnxTypeBOS;

		// 汇总
		String totalLine = reqBrhCode + reqDate + batch + totalNum + commentsTotal + "\n";

		// 明细 上海银行串配报文特殊
		String detailLine = billBrh + PrefixSpace(userNo, 20) + PrefixSpace(userName, 10)
				+ PrefixSpace(serialNum, 40) + PrefixSpace(userNo, 60);

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine+detailLine),localFilePath);
		String remoteFilePath = filePath + fileName;
		//参数返回
		vars.put("localFilePath", localFilePath);
		vars.put("remoteFilePath", remoteFilePath);
	}
	//工商银行串配报文组装
	public static void unsignICBC(){

		String reqBrhCode = icbcCode; // 渠道机构代码
		String filePath = icbcFilePath; // 串配文件ftp路径
		String month = PrefixInteger(String.valueOf(${__time(MM,)}), 2); //月份
		String serialNo = new SimpleDateFormat("SSSS").format(new Date()); //文件序列号 HHmm
		//**************工商银行特色设置****************
		String companyCode = "0837003"; // 公司代码
		String bankAcct = "6212261001084770335"; // 扣款银行账号，银行为工行时必填，19位
		String customerID = "266003641158"; // 客户编号，默认,12位
		String postID = "200042"; // 客户邮编, 设为默认值
		String returnAddr = "无";// 公司返回地址
		String returnName = ""; // 公司返回姓名
		String bankCode = "007"; // 办理授权支行代码
		String bankBranchCode = "266"; // 办理授权储蓄所号
		String status = "20"; // 10 串配； 20 解串配******************************

		// 文件名组装
		String fileName = "SQ" + month + serialNo + reqDate + "." + "003";

		// 汇总
		String totalLine = "E" + reqDate + totalNum + "\n";

		// 明细
		String detailLine = companyCode + PrefixInteger(userNo, 13) + bankAcct + customerID + PrefixSpace(userName, 40)
				+ PrefixSpace(userName, 8) + postID + PrefixSpace(returnAddr, 50) + PrefixSpace(returnName, 8) + bankCode
				+ bankBranchCode + status + PrefixSpace("", 20) + "000000000000";

		//判断subChannel不为空时，明细里面增加二级渠道报备字段
		if (subChannel.length() != 0) {
			detailLine = detailLine + PrefixSpace(subChannel, 15);
		}

		// 文件内容存入本地
		String localFilePath = baseDir + fileName;
		transferFile((totalLine+detailLine),localFilePath);
		String remoteFilePath = filePath + fileName;
		//参数返回
		vars.put("localFilePath", localFilePath);
		vars.put("remoteFilePath", remoteFilePath);
	}

	//付费通解串配报文组装
	public static void unsignFFT(String fftType){

		// String reqBrhCode = fftCode; // 渠道机构代码
		String filePath = fftFilePath; // 串配文件ftp路径
		String month = PrefixInteger(String.valueOf(${__time(MM,)}), 2); //月份
		String serialNo = PrefixInteger(String.valueOf(${__time(mmss,)}), 4); //文件序列号 HHmm
		//**************付费通特色设置****************
		String authType = "1";  //授权类型：0 - 申请； 1 - 撤销
		String authNoType = "0";  //授权号码类型： 0 - 合同号；1 - 设备号
		String fftCode = "888880201000023"; //付费通新代扣机构号， 新代扣串解配使用这个
		String fftCodeOld = "777777777777777"; //付费通老代扣的机构号，老代扣串解配使用这个
		String fftAcct = "1000" + (int)((Math.random()*9+1)*100000); //随机付费通账号， 10位
		String fftTelphone = "138000000" + (int)((Math.random()*9+1)*10); //联系电话，随机
		String deviceNo = "200" + (int)((Math.random()*9+1)*100000);  //付费通设备号吗，随机9位

		//如果是付费通老代扣，把fftCode值替换成“"777777777777777"
		if (fftType.equals("old")){
			fftCode = fftCodeOld;
		}

		// 文件名组装
		String fileName = billBrh + reqDate + batch + "." + tnxTypeFFT;

		// 汇总
		String totalLine = billBrh + reqDate + batch + totalNum + commentsTotal + "\n";

		// 明细
		String detailLine = authType + billBrh + authNoType + PrefixSpace(userNo, 30) + reqDate + fftCode + PrefixSpace(fftAcct, 40)
			+ PrefixSpace(userName, 40) + PrefixSpace("", 110) + PrefixSpace(certNo, 20) + PrefixSpace("", 20)
			+ PrefixSpace(fftTelphone, 20) + PrefixSpace(deviceNo, 30) + commentsTotal;

		//判断subChannel不为空时，明细里面增加二级渠道报备字段
		if (subChannel.length() != 0) {
			detailLine = detailLine + PrefixSpace(subChannel, 15);
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

if (bank.equals("abc") || bank.equals("cmbc") || bank.equals("boc")) {
	Tools.unsign(bank);
} else if (bank.equals("bos")) {
	Tools.unsignBOS();
} else if (bank.equals("icbc")) {
	Tools.unsignICBC();
} else if (bank.equals("fft_old")) {
	Tools.unsignFFT("old");  //付费通老代扣的解串配
} else if (bank.equals("fft_new")) {
	Tools.unsignFFT("new");  //付费通新代扣的解串配
}

