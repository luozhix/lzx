package test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;
import op.AccessToken;
import util.WeixinUtil;

public class WeixinSendToAll {

	private static Logger logger = Logger.getLogger(WeixinSendToAll.class);
	private static Long EXPIRES = 0l;

	/**
	 * @param companyCode
	 * @param companyAppId
	 * @param companyAppSecret
	 * @param filePath
	 *            首页缩略图文件路径
	 * @param title
	 * @param author
	 *            文章作者
	 * @param content_source_url
	 *            阅读原文的链接
	 * @param digest
	 *            消息简介
	 * @param content
	 *            1、html属性值只能用单引号，某些标签不支持。
	 *            2、如果有图片，先用WeChatMsgKit.uploadImg上传得到url
	 * @param preFlag
	 *            是否先预览
	 * @param prOpenId
	 *            单独发给该用户进行预览
	 * @throws Exception
	 */

	public static void sendMsgToAll(String filePath,
			String title, String author, String content_source_url, String digest, String content) throws Exception {

		AccessToken access_token = null;
		String media_id = "";
		String msg = "";

		// 1、得到token===============================================
		access_token = WeixinUtil.getAccessToken();
		logger.debug(access_token);

		// 2、上传消息的缩略图==========================================
		//String post0 = send("image", filePath, access_token.getToken());
		
		// 3、得到缩略图的media_id==========================================
		media_id = WeixinUtil.upload(filePath, access_token.getToken(), "image");
		System.out.println("media_id:"+media_id);

		// 4、收集文章所需参数，并转码==========================================
		title = new String(title.getBytes("utf-8"), "ISO-8859-1");
		author = new String(author.getBytes("utf-8"), "ISO-8859-1");
		digest = new String(digest.getBytes("utf-8"), "ISO-8859-1");
		content = new String(content.getBytes("utf-8"), "ISO-8859-1");

		// 5、上传图文消息==========================================
		String url = "https://api.weixin.qq.com/cgi-bin/media/uploadnews?access_token=" + access_token.getToken();
		msg = "{\"articles\": [{\"thumb_media_id\":\"" + media_id + "\", \"author\":\"" + author + "\",\"title\":\""
				+ title + "\",\"content_source_url\":\"" + content_source_url + "\", \"content\":\"" + content
				+ "\",\"digest\":\"" + digest + "\"}]}";

		//String post = HttpKit.post(url, msg);
		//Map json2Map1 = JsonKit.json2Map(post);
		// 6、得到图文消息media_id==========================================
		//media_id = (String) json2Map1.get("media_id");
		JSONObject json = WeixinUtil.httpRequest(url, "POST", msg);  
		media_id=json.getString("media_id");

		/*if (preFlag) {
			// 7、 单独发给该用户进行预览==========================================

			String sendPreUrl = "https://api.weixin.qq.com/cgi-bin/message/mass/preview?access_token=" + access_token.getToken();
			String sendPreMsg = "{\"touser\":\"" + prOpenId + "\",\"mpnews\":{\"media_id\":\"" + media_id
					+ "\"},\"msgtype\":\"mpnews\"}";
			String sendPreRes = HttpKit.post(sendPreUrl, sendPreMsg);
			logger.debug(sendPreRes);

		} else {*/

			// 8、群发==========================================
			String sendAllUrl = "https://api.weixin.qq.com/cgi-bin/message/mass/sendall?access_token=" + access_token.getToken();
			String sendAllMsg = "{\"filter\":{\"is_to_all\":true},\"mpnews\":{ \"media_id\":\"" + media_id
					+ "\"}, \"msgtype\":\"mpnews\"}";

			//String sendAllRes = HttpKit.post(sendAllUrl, sendAllMsg);
			JSONObject json1 = WeixinUtil.httpRequest(sendAllUrl, "POST", sendAllMsg);  
			System.out.println(json1.toString());
			logger.debug(json1.toString());
		//}

	}
	/**
	 * 发送图文
	 */
	/*private static String send(String fileType, String filePath, String token) throws Exception {
		String result = null;
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new IOException("文件不存在");
		}
		*//**
		 * 第一部分
		 *//*
		URL urlObj = new URL(
				"http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token=" + token + "&type=" + fileType + "");
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
		con.setRequestMethod("POST"); // 以Post方式提交表单，默认get方式
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false); // post方式不能使用缓存
		// 设置请求头信息
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", "UTF-8");
		// 设置边界
		String BOUNDARY = "----------" + System.currentTimeMillis();
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
		// 请求正文信息
		// 第一部分：
		StringBuilder sb = new StringBuilder();
		sb.append("--"); // 必须多两道线
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
		sb.append("Content-Type:application/octet-stream\r\n\r\n");
		byte[] head = sb.toString().getBytes("utf-8");
		// 获得输出流
		OutputStream out = new DataOutputStream(con.getOutputStream());
		// 输出表头
		out.write(head);
		// 文件正文部分
		// 把文件已流文件的方式 推入到url中
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while ((bytes = in.read(bufferOut)) != -1) {
			out.write(bufferOut, 0, bytes);
		}
		in.close();
		// 结尾部分
		byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
		out.write(foot);
		out.flush();
		out.close();
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		try {
			// 定义BufferedReader输入流来读取URL的响应
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				buffer.append(line);
			}
			if (result == null) {
				result = buffer.toString();
			}
		} catch (IOException e) {
			System.out.println("发送POST请求出现异常！" + e);
			e.printStackTrace();
			throw new IOException("数据读取异常");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return result;
	}*/
}
	
