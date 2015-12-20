package org.jcc.http.server;

import java.util.Map;

public interface HttpResponse {

	void addDefaultHeaders();

	int getResponseCode();

	String getResponseReason();

	String getHeader(String header);

	byte[] getContent();

	void setResponseCode(int responseCode);

	void setResponseReason(String responseReason);

	void setContent(byte[] content);

	void setHeader(String key, String value);

	String getVersion();

	Map<String, String> getHeaders();

}