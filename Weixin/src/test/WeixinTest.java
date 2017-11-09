package test;

import net.sf.json.JSONObject;
import op.AccessToken;
import util.WeixinUtil;

public class WeixinTest {

	public static void main(String[] args) {
		try {
			AccessToken token = WeixinUtil.getAccessToken();
			System.out.println("票据"+token.getToken()+"\r\n有效使时间"+token.getExpiresIn());
			//图文信息
			/*String filePath = "D:/coc.jpg";
			String title = "coc";
			String author = "lzx";
			String content_source_url = "http://www.qq.com";
			String digest = "";
			String content = "123adsasdasdasdasdasdas";*/
		//上传图片素材
			//String url = WeixinUtil.uploadImg(path, token.getToken());
			//System.out.println(url);
			//群发
			//WeixinSendToAll.sendMsgToAll(filePath, title, author, content_source_url, digest, content);
			//String mediaId = WeixinUtil.upload(path, token.getToken(), "image");
			//System.out.println(mediaId);*/
			//String menu = JSONObject.fromObject(WeixinUtil.initMenu()).toString();
			//System.out.println(menu);
			//int result = WeixinUtil.createMenu(token.getToken(), menu);
			
//			JSONObject jsonObj = WeixinUtil.queryMenu(token.getToken());
//			System.out.println(jsonObj);
//			if(result==0){
//				System.out.println("创建菜单成功");
//			}else{
//				System.out.println("错误码:"+result);
//			}
			
			//int result = WeixinUtil.deleteMenu(token.getToken());
			//System.out.println(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
