import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.apache.commons.lang.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
// 获取自定义数据====================================================================================================
String bank = vars.get("bank"); //机构名称
String reqDate = vars.get("reqDate"); //日期
String batch = vars.get("batch"); //批次
// String userNo=vars.get("userNo"); //串配号
// String userName=vars.get("userName"); //串配号的名称
// String certNo=vars.get("certNo"); //身份证号
String subChannel = vars.get("subChannel"); //二级渠道编号
String subChannel_type = vars.get("subChannel_type"); //二级渠道报备类型： 1-新增;2-修改;3-删除  删除时’二级渠道编号’必填
String subChannel_Name = vars.get("subChannel_Name"); //二级渠道中文名称
String subChannel_Person = vars.get("subChannel_Person"); //法定代表人姓名
String subChannel_CertType = vars.get("subChannel_CertType"); //法人代表证件类型: 0-身份证 1-护照 2-军官证 3-士兵证 4-港澳台居民往来通行证 5-临时身份证 6-户口本 7-其他 9-警官证 12-外国人永久居留证
String subChannel_CertNo = vars.get("subChannel_CertNo"); //法人代表证件号码
String subChannel_Company_No = vars.get("subChannel_Company_No"); //工商营业执照
String subChannel_Addr = vars.get("subChannel_Addr"); //地址
String subChannel_PostCode = vars.get("subChannel_PostCode"); //邮编
String subChannel_Capital = vars.get("subChannel_Capital"); //注册资金(万元)
String subChannel_RegDate = vars.get("subChannel_RegDate"); //签约日期 YYYYMMDD
String subChannel_ContactPerson = vars.get("subChannel_ContactPerson"); //联系人
String subChannel_ContactPhone = vars.get("subChannel_ContactPhone"); //联系电话
String subChannel_Mail = vars.get("subChannel_Mail"); //联系邮箱
String subChannel_LimitDay = vars.get("subChannel_LimitDay"); //交款限制天数
String subChannel_Check = vars.get("subChannel_Check"); //是否校验下属渠道
// String subChannelPayDate = vars.get("subChannelPayDate"); //用户交款日期
// String billAmt = vars.get("billAmount");
// String period = vars.get("period");  //帐期
// String barcode = vars.get("barcode");  //条形码
// String billPlanID = vars.get("billPlanID");  //账单计划ID
// String bossSignNo = vars.get("bossSignNo"); //第三方签约协议号
// String bossOrgCode = vars.get("bossOrgCode"); //子公司代码
// String yfProcessNo = vars.get("yfProcessNo"); //亿付业务处理号 ??
//文件本地存放路径
String baseDir = vars.get("path");
//默认数据=========================================================================================================
String tnxType = "00B31300"; //交易识别码, 二级渠道报备
String tnxTypeFFT = "B313000"; //付费通二级渠道报备代码
// String billBrh = "888880003502900"; //出账机构代码, 固定值
String totalNum = "000001"; //总记录数, 目前只支持1条数据
String commentsTotal = "000"; //备注

//=========================银行二级渠道报备设置===================================================================================
//农行
String abcCode="508290060120001"; //农行渠道机构代码
String abcFilePath="/ori-data/ftp-test/bill/abc/sub_channel_request/"; 
//招行
String cmbcCode="508290060120026"; //招行渠道机构代码
String cmbcFilePath="/ori-data/ftp-test/bill/cmbc/sub_channel_request/"; 
//中国银行
String bocCode="508290060120023"; //中国银行渠道机构代码
String bocFilePath="/ori-data/ftp-test/bill/boc/sub_channel_request/"; 
//上海银行
String bosCode="508290060120024"; //上海银行渠道机构代码
String bosFilePath="/ori-data/ftp-test/bill/bos/sub_channel_request/"; 
//工商银行
String icbcCode="508290060120002"; //上海银行渠道机构代码
String icbcFilePath="/ori-data/ftp-test/bill/icbc/sub_channel_request/";
// 付费通联机
String fftCode_online = "888888888888888";
String fftCode_onlineFilePath = "/ori-data/ftp-test/bill/fft/sub_channel_request/";
//光大联机 
String cebCode="508290060120027";
String cebFilePath="/ori-data/ftp-test/bill/ceb/sub_channel_request/";

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

	public static void subChannelRequest(String bank){
		String reqBrhCode = "";
		String filePath = "";
		String fileBatch = new SimpleDateFormat("yyyyMMddSSS").format(new Date());  //文件批号 ，11位
		String channelSerialNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()); //二级渠道报备流水号
		String fileNo = "0" + batch;
		String commentsDetail = ""; //明细的备注，只有付费通有

		if(bank.equals("abc")){
			reqBrhCode = abcCode; // 农行渠道机构代码
			filePath = abcFilePath; // 串配文件ftp路径
		}else if(bank.equals("cmbc")){
			reqBrhCode = cmbcCode; // 招行渠道机构代码
			filePath = cmbcFilePath; // 串配文件ftp路径
		}else if(bank.equals("boc")){
			reqBrhCode = bocCode; // 中国银行渠道机构代码
			filePath = bocFilePath; // 串配文件ftp路径
		}else if(bank.equals("fft_online")){  //付费通联机
			reqBrhCode = fftCode_online; 
			filePath = fftCode_onlineFilePath;
			tnxType = tnxTypeFFT;  //付费通替换交易代码
			fileNo = batch;  //付费通文件批号为10位
			commentsDetail = "000"; //付费通有的明细备注
		}else if(bank.equals("ceb_online")){  //光大联机
			reqBrhCode = cebCode;
			filePath = cebFilePath;
		}

		// 文件名组装
		String fileName = reqBrhCode + reqDate + batch + "." + tnxType;

		// 汇总 没有失败笔数
		String totalLine = reqBrhCode + reqDate + fileNo + totalNum + commentsTotal + "\n";

		// 明细  
		// 渠道签约号 农行 - 用户证号 + 身份证
		String detailLine = PrefixSpace(channelSerialNo, 20) + subChannel_type + PrefixSpace(subChannel, 15) + PrefixSpace(subChannel_Name, 60)
			+ PrefixSpace(subChannel_Person, 60) + PrefixSpace(subChannel_CertType, 3) + PrefixSpace(subChannel_CertNo, 18)
			+ PrefixSpace(subChannel_Company_No, 20) + PrefixSpace(subChannel_Addr, 60) + PrefixSpace(subChannel_PostCode, 6)
			+ PrefixSpace(subChannel_Capital, 9) + PrefixSpace(subChannel_RegDate, 8) + PrefixSpace(subChannel_ContactPerson, 60)
			+ PrefixSpace(subChannel_ContactPhone, 50) + PrefixSpace(subChannel_Mail, 50) + PrefixSpace(subChannel_LimitDay, 10)
			+ subChannel_Check;

		//付费通明细最后增加备注
		if(bank.contains("fft")){
			detailLine = detailLine + commentsDetail;
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

Tools.subChannelRequest(bank);

