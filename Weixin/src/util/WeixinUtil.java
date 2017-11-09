package util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.io.BufferedReader;  
import java.io.InputStream;  
import java.io.InputStreamReader;  
import java.io.OutputStream;  
import java.net.ConnectException;  
import java.net.URL;  
  
import javax.net.ssl.HttpsURLConnection;  
import javax.net.ssl.SSLContext;  
import javax.net.ssl.SSLSocketFactory;  
import javax.net.ssl.TrustManager;  
  
import net.sf.json.JSONException;  
import net.sf.json.JSONObject;  
  
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;


import Menu.Button;
import Menu.ClickButton;
import Menu.Menu;
import Menu.ViewButton;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import op.AccessToken;
import op.MyX509TrustManager;

public class WeixinUtil {
	private static final String APPID="wx8bedd4689fd4526f";
	private static final String APPSECRET="4f05aaf775fb22274225c3afd091f1db";
	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	private static final String UPLOAD_URL = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
	private static final String CREATE_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
	private static final String QUERY_MENU_URL="https://api.weixin.qq.com/cgi-bin/menu/get?access_token=ACCESS_TOKEN";
	private static final String DELETE_MENU_URL="https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=ACCESS_TOKEN";
	private static final String IMG_UPLOAD_URL = "https://api.weixin.qq.com/cgi-bin/media/uploadimg?access_token=ACCESS_TOKEN";
	/**
	 * get请求
	 */
	public static JSONObject doGetStr(String url){
		@SuppressWarnings("deprecation")
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		JSONObject jsonObject = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if(entity != null){
				String result = EntityUtils.toString(entity,"utf-8");
				jsonObject = JSONObject.fromObject(result);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}
	/**
	 * post请求
	 * @param url
	 * @param outStr
	 * @return
	 */
	
	public static JSONObject doPostStr(String url,String outStr){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		JSONObject jsonObject = null;
		try {
			httpPost.setEntity(new StringEntity(outStr,"utf-8"));
			HttpResponse response = httpClient.execute(httpPost);
			String result = EntityUtils.toString(response.getEntity(),"utf-8");
			jsonObject = JSONObject.fromObject(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}
	/**
	 * 文件上传的方法
	 * @param filePath
	 * @param accessToken
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static String upload(String filePath,String accessToken,String type) throws IOException{
		File file = new File(filePath);
		if(!file.exists()||!file.isFile()){
			throw new IOException("文件不存在");
		}
		String url = UPLOAD_URL.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);
		URL urlObj = new URL(url);
		//连接
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		//设置请求头信息
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", "UTF-8");
		//设置边界
		String BOUNDARY ="----------"+System.currentTimeMillis();
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);
		
		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
		sb.append("Content-Type:application/octet-stream\r\n\r\n");
		byte[] head = sb.toString().getBytes("utf-8");
		//获取输出流
		OutputStream out = new DataOutputStream(con.getOutputStream());
		//输出表头
		out.write(head);
		//文件正文部分
		//把文件以流文件的方式 推入到url中
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while((bytes=in.read(bufferOut))!=-1){
			out.write(bufferOut,0,bytes);
		}
		in.close();
		byte[] foot = ("\r\n--" +BOUNDARY +"--\r\n").getBytes("utf-8");//定义最后数据分隔线
		out.write(foot);
		out.flush();
		out.close();
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		String result = null;
		//定义bufferedreader输入流来读取url的响应
		try {
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;
			while((line= reader.readLine())!=null){
				buffer.append(line);
			}
			if(result==null){
				result = buffer.toString();
			}
		} catch (IOException e) {
		
			e.printStackTrace();
		} finally{
			if(reader!=null){
				reader.close();
			}
		}
		JSONObject jsonObj = JSONObject.fromObject(result);
		System.out.println(jsonObj);
		String mediaId = jsonObj.getString("media_id");
		return mediaId;
		
	}
	/**
	 * 获取access_token
	 * @return
	 */
	public static AccessToken getAccessToken(){
		AccessToken token = new AccessToken();
		String url = ACCESS_TOKEN_URL.replace("APPID", APPID).replace("APPSECRET", APPSECRET);
		JSONObject jsonObject = doGetStr(url);
		if(jsonObject!=null){
			token.setToken(jsonObject.getString("access_token"));
			token.setExpiresIn(jsonObject.getInt("expires_in"));
		}
		return token;
	}
	/**
	 * 组装菜单
	 * @return
	 */
	public static Menu initMenu(){
		Menu menu = new Menu();
		ClickButton cb = new ClickButton();
		cb.setName("click菜单");
		cb.setType("click");
		cb.setKey("11");
		
		ViewButton vb = new ViewButton();
		vb.setName("view菜单");
		vb.setType("view");
		vb.setUrl("http://luozhix.github.io");
		
		ClickButton cb2 = new ClickButton();
		cb2.setName("扫码click");
		cb2.setType("scancode_push");
		cb2.setKey("21");
		
		ClickButton cb3 = new ClickButton();
		cb3.setName("地理位置");
		cb3.setType("location_select");
		cb3.setKey("32");
		
		Button button = new Button();
		button.setName("菜单");
		button.setSub_button(new Button[]{cb2,cb3});
		
		menu.setButton(new Button[]{cb,vb,button});
		return menu;
	}
	
	public static int createMenu(String token,String menu){
		int result = 0;
		String url = CREATE_MENU_URL.replace("ACCESS_TOKEN", token);
		JSONObject jsonObj = doPostStr(url, menu);
		if(jsonObj!=null){
			result = jsonObj.getInt("errcode");
		}
		return result;
	}
	public static JSONObject queryMenu(String token){
		String url = QUERY_MENU_URL.replace("ACCESS_TOKEN", token);
		JSONObject jsonObj = doGetStr(url);
		return jsonObj;
	}
	public static int deleteMenu(String token){
		String url = DELETE_MENU_URL.replace("ACCESS_TOKEN", token);
		JSONObject jsonObj = doGetStr(url);
		int result = 0;
		if(jsonObj != null){
			result = jsonObj.getInt("errcode");
		}
		return result;
	}
	/*public static String uploadImg1(String token,String filePath) {
		String url = IMG_UPLOAD_URL.replace("ACCESS_TOKEN",token );
		String requestRes = HttpKit.post(url, filePath, null);
		Map<String, String> requestJson = JsonKit.json2Map(requestRes);

		return (String) requestJson.get("url");
	}*/
	public static String uploadImg(String filePath,String accessToken) throws IOException{
		File file = new File(filePath);
		if(!file.exists()||!file.isFile()){
			throw new IOException("文件不存在");
		}
		String url = IMG_UPLOAD_URL.replace("ACCESS_TOKEN", accessToken);
		URL urlObj = new URL(url);
		//连接
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		//设置请求头信息
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", "UTF-8");
		//设置边界
		String BOUNDARY ="----------"+System.currentTimeMillis();
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);
		
		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
		sb.append("Content-Type:application/octet-stream\r\n\r\n");
		byte[] head = sb.toString().getBytes("utf-8");
		//获取输出流
		OutputStream out = new DataOutputStream(con.getOutputStream());
		//输出表头
		out.write(head);
		//文件正文部分
		//把文件以流文件的方式 推入到url中
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while((bytes=in.read(bufferOut))!=-1){
			out.write(bufferOut,0,bytes);
		}
		in.close();
		byte[] foot = ("\r\n--" +BOUNDARY +"--\r\n").getBytes("utf-8");//定义最后数据分隔线
		out.write(foot);
		out.flush();
		out.close();
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		String result = null;
		//定义bufferedreader输入流来读取url的响应
		try {
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;
			while((line= reader.readLine())!=null){
				buffer.append(line);
			}
			if(result==null){
				result = buffer.toString();
			}
		} catch (IOException e) {
		
			e.printStackTrace();
		} finally{
			if(reader!=null){
				reader.close();
			}
		}
		JSONObject jsonObj = JSONObject.fromObject(result);
		System.out.println(jsonObj);
		String mediaId = jsonObj.getString("url");
		return mediaId;
		
	}
	public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
		Logger log = Logger.getLogger(WeixinUtil.class);  
        JSONObject jsonObject = null;  
        StringBuffer buffer = new StringBuffer();  
        try {  
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化  
            TrustManager[] tm = { new MyX509TrustManager() };  
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");  
            sslContext.init(null, tm, new java.security.SecureRandom());  
            // 从上述SSLContext对象中得到SSLSocketFactory对象  
            SSLSocketFactory ssf = sslContext.getSocketFactory();  
  
            URL url = new URL(requestUrl);  
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();  
            httpUrlConn.setSSLSocketFactory(ssf);  
  
            httpUrlConn.setDoOutput(true);  
            httpUrlConn.setDoInput(true);  
            httpUrlConn.setUseCaches(false);  
            // 设置请求方式（GET/POST）  
            httpUrlConn.setRequestMethod(requestMethod);  
  
            if ("GET".equalsIgnoreCase(requestMethod))  
                httpUrlConn.connect();  
  
            // 当有数据需要提交时  
            if (null != outputStr) {  
                OutputStream outputStream = httpUrlConn.getOutputStream();  
                // 注意编码格式，防止中文乱码  
                outputStream.write(outputStr.getBytes("UTF-8"));  
                outputStream.close();  
            }  
  
            // 将返回的输入流转换成字符串  
            InputStream inputStream = httpUrlConn.getInputStream();  
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  
  
            String str = null;  
            while ((str = bufferedReader.readLine()) != null) {  
                buffer.append(str);  
            }  
            bufferedReader.close();  
            inputStreamReader.close();  
            // 释放资源  
            inputStream.close();  
            inputStream = null;  
            httpUrlConn.disconnect();  
            jsonObject = JSONObject.fromObject(buffer.toString());  
        } catch (ConnectException ce) {  
            log.error("Weixin server connection timed out.");  
        } catch (Exception e) {  
            log.error("https request error:{}", e);  
        }  
        return jsonObject;  
    }  
	 /**
	  * 获取openid
	  * @return
	  */
    public static JSONArray  getOpenids(String token){
        JSONArray array =null;
        String urlstr ="https://api.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN&next_openid=NEXT_OPENID";
        urlstr =urlstr.replace("ACCESS_TOKEN", token);
        urlstr =urlstr.replace("NEXT_OPENID", "");
        URL url;
        try {
            url = new URL(urlstr);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();        
            http.setRequestMethod("GET");        
            http.setRequestProperty("Content-Type","application/x-www-form-urlencoded");    
            http.setDoInput(true);
            InputStream is =http.getInputStream();
            int size =is.available();
            byte[] buf=new byte[size];
            is.read(buf);
            String resp =new String(buf,"UTF-8");
            JSONObject jsonObject =JSONObject.fromObject(resp);
            System.out.println("resp:"+jsonObject.toString());
            array =jsonObject.getJSONObject("data").getJSONArray("openid");
            return array;
        } catch (MalformedURLException e) {
            e.printStackTrace();
             return array;
             
        } catch (IOException e) {
            e.printStackTrace();
             return array;
         
        }
    }

}
