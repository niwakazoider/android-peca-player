package net.fukure.android.pecaplayer.http;

import org.apache.http.Header;

public class HttpResponse{
	public int status;
	public Header headers[];
	public String body;
	HttpResponse(int status, Header[] headers, String response){
		this.status = status;
		this.headers = headers;
		this.body = response;
	}
}