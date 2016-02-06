package action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TuringRobot {
	/**
	 * 使用图灵机器人接口获取回答
	 * 
	 * @param apikey API认证
	 * @param info 想要请求的问题
	 * @return 获取的回复
	 * */
	public static String getResponse(String apikey,String info){
		String httpUrl;
		try {
			httpUrl = "http://www.tuling123.com/openapi/api?key=" + apikey + "&info=" + URLEncoder.encode(info,"UTF-8");
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setReadTimeout(5000);
			connection.setConnectTimeout(5000);
			
			InputStream inputStream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
			String line = "";
			String reg = "\"text\":\"(.*)?\"}";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher;
			while((line = reader.readLine()) != null){
				matcher = pattern.matcher(line);
				if(matcher.find()){
					connection.disconnect();
					return matcher.group(1);
				}
			}
			connection.disconnect();	
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 使用百度接口获取回答
	 * 
	 * @param key 默认值：879a6cb3afb84dbf4fc84a1df2ab7319
	 * @param ApiKey 在APIStore调用服务所需要的API密钥，申请地址：http://apistore.baidu.com
	 * @param info 想要请求的问题
	 * @param userid 用户id 默认值：eb2edb736
	 * 
	 * @return 获取的回复
	 * */
	public static String getResponse(String key,String ApiKey,String info,String userid){
		String httpUrl = "http://apis.baidu.com/turing/turing/turing?";
		try {
			info = URLEncoder.encode(info,"UTF-8");  
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String httpArg = "key=" + key + "&info=" + info + "&userid=" + userid;
		try {
			URL url = new URL(httpUrl + httpArg);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("apikey", ApiKey);
			
			InputStream inputStream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
			String line = "";
			String reg = "\"text\":\"(.*)?\",\"code\"";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher;
			while((line = reader.readLine()) != null){
				matcher = pattern.matcher(line);
				if(matcher.find()){
					connection.disconnect();
					return matcher.group(1);
				}
			}	
			connection.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
		
	}

}
