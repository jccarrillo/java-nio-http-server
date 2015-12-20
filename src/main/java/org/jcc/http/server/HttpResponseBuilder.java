package org.jcc.http.server;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.Builder;

public final class HttpResponseBuilder implements Builder<HttpResponse> {

	@Override
	public HttpResponse build() {
		return new HttpResponseImpl();
	}

	private class HttpResponseImpl implements HttpResponse {

		private String version = "HTTP/1.1";
		private int responseCode = 200;
		private String responseReason = "OK";
		private Map<String, String> headers = new LinkedHashMap<String, String>();
		private byte[] content;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#addDefaultHeaders()
		 */
		@Override
		public void addDefaultHeaders() {
			this.headers.put("Date", new Date().toString());
			this.headers.put("Server", "Java HTTP Server");
			this.headers.put("Connection", "close");
			this.headers.put("Content-Length", Integer.toString(content.length));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#getResponseCode()
		 */
		@Override
		public int getResponseCode() {
			return this.responseCode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#getResponseReason()
		 */
		@Override
		public String getResponseReason() {
			return this.responseReason;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#getHeader(java.lang.String)
		 */
		@Override
		public String getHeader(String header) {
			return this.headers.get(header);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#getContent()
		 */
		@Override
		public byte[] getContent() {
			return this.content;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#setResponseCode(int)
		 */
		@Override
		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jcc.http.server.HttpResponse#setResponseReason(java.lang.String)
		 */
		@Override
		public void setResponseReason(String responseReason) {
			this.responseReason = responseReason;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#setContent(byte[])
		 */
		@Override
		public void setContent(byte[] content) {
			this.content = content;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#setHeader(java.lang.String,
		 * java.lang.String)
		 */
		@Override
		public void setHeader(String key, String value) {
			this.headers.put(key, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#getVersion()
		 */
		@Override
		public String getVersion() {
			return this.version;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpResponse#getHeaders()
		 */
		@Override
		public Map<String, String> getHeaders() {
			return this.headers;
		}
	}
}