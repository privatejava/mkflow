package com.mkflow.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Narayan <me@ngopal.com.np> - ngm
 * Created 20/07/2020 20:10
 **/

public class LambdaRequestModel {

	private String path;

	private String httpMethod;

	private String body;

	private Map<String, String> headers;

	private Map<String,Object> multiValueHeaders;

	private boolean base64Encoded = false;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public boolean isBase64Encoded() {
		return base64Encoded;
	}

	public void setBase64Encoded(boolean base64Encoded) {
		this.base64Encoded = base64Encoded;
	}

	public Map<String, Object> getMultiValueHeaders() {
		return multiValueHeaders;
	}

	public void setMultiValueHeaders(Map<String, Object> multiValueHeaders) {
		this.multiValueHeaders = multiValueHeaders;
	}

	public void addMultiValueHeader(String name, Object val){
		if(this.multiValueHeaders == null){
			this.multiValueHeaders = new HashMap<>();
		}
		this.multiValueHeaders.put(name,val);
	}
}
