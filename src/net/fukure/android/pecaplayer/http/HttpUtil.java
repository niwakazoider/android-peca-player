package net.fukure.android.pecaplayer.http;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpUtil {
	public static HttpResponse get(String uri){
		return get(uri, new Header[0]);
	}
	public static HttpResponse get(String uri, String charset){
		return get(uri, new Header[0], charset);
	}
	public static HttpResponse get(String uri, Header[] requestHeader){
		return get(uri, requestHeader, "UTF-8");
	}
	public static HttpResponse get(String uri, Header[] requestHeader, String charset){
		Header[] responseHeaders = null;
		String responseBody = null;
		int status = 0;

		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		HttpParams params = new BasicHttpParams();
		params.setParameter("http.protocol.handle-redirects",false);
		HttpConnectionParams.setConnectionTimeout(params, 5000);
					
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setParams(params);
		for (int i = 0; i < requestHeader.length; i++) {
			httpGet.addHeader(requestHeader[i]);
		}
		
		InputStream content = null;
		BufferedReader reader = null;
	    StringBuilder buf = new StringBuilder();
		try {
			org.apache.http.HttpResponse response = httpClient.execute(httpGet);
			status = response.getStatusLine().getStatusCode();
			responseHeaders = response.getAllHeaders();
			if(responseHeaders==null){
				responseHeaders = new Header[0];
			}
			
			content = response.getEntity().getContent();
			reader = new BufferedReader(new InputStreamReader(content, charset));

		    String line;
		    while ((line = reader.readLine()) != null) {
		        buf.append(line);
		        buf.append("\r\n");
		    }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ConnectionClosedException e) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
			    content.close();
			    reader.close();
            } catch(Exception e) {}
		}
	    responseBody = buf.toString();
		
		httpClient.getConnectionManager().shutdown();

		return new HttpResponse(status, responseHeaders, responseBody);
	}
}
