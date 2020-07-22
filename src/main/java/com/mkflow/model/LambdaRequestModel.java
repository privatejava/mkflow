/*
 * Copyright 2020 Mkflow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.mkflow.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Narayan <me@ngopal.com.np> - ngm
 * Created 20/07/2020 20:10
 **/

@RegisterForReflection
public class LambdaRequestModel {

	public String path;

	public String httpMethod;

	public String body;

	public Map<String, String> headers;

	public Map<String,Object> multiValueHeaders;

	public boolean base64Encoded = false;

	public LambdaRequestModel() {
	}

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
