package org.jcc.http.server;

public interface HttpRequest {

	String getVersion();

	String getMethod();

	String getLocation();

	String getHead(String key);

}