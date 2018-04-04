package vip.zhaotao.util;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @title HttpClient工具
 * @description
 */
public class HttpClientUtil {

	/**
	 * @title 发送请求
	 * @description
	 * @param request
	 * @return
	 * @throws Exception
	 * @return String
	 */
	private static String send(HttpUriRequest request) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(request);
		String responseContent = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
		close(client, response);
		return responseContent;
	}

	/**
	 * @title 关闭连接
	 * @description 
	 * @param client
	 * @param response
	 * @throws Exception
	 * @return void
	 */
	private static void close(CloseableHttpClient client, CloseableHttpResponse response) throws Exception {
		if (response != null)
			response.close();
		if (client != null)
			client.close();
	}

	/**
	 * @title 拼装请求参数
	 * @description 
	 * @param params
	 * @return
	 * @throws Exception
	 * @return UrlEncodedFormEntity
	 */
	private static UrlEncodedFormEntity assemblyRequestParams(Map<String, String> params) throws Exception {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (params != null) {
			Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
			Entry<String, String> next = null;
			while (iterator.hasNext()) {
				next = iterator.next();
				nvps.add(new BasicNameValuePair(next.getKey(), next.getValue()));
			}
		}
		return new UrlEncodedFormEntity(nvps, Consts.UTF_8);
	}

	/**
	 * @title 发送POST请求
	 * @description 
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 * @return String
	 */
	public static String sendPost(String url, Map<String, String> params) throws Exception {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(assemblyRequestParams(params));
		return send(httpPost);
	}

	/**
	 * @title 发送GET请求 
	 * @description 
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 * @return String
	 */
	public static String sendGet(String url, Map<String, String> params) throws Exception {
		UrlEncodedFormEntity entity = assemblyRequestParams(params);
		if (entity.getContentLength() > 0) {
			url += "?" + EntityUtils.toString(entity, Consts.UTF_8);
		}
		HttpGet httpGet = new HttpGet(url);
		return send(httpGet);
	}
}
