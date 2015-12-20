package org.jcc.http.server;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.builder.Builder;

public class HttpRequestBuilder implements Builder<HttpRequest> {

	private String raw;

	public HttpRequestBuilder(String raw) {
		this.raw = raw;
	}

	@Override
	public HttpRequest build() {
		HttpRequestImpl httpRequest = new HttpRequestImpl();
		httpRequest.parse(this.raw);

		return httpRequest;
	}

	private class HttpRequestImpl implements HttpRequest {

		private String method;
		private String location;
		private String version;
		private Map<String, String> headers = new HashMap<String, String>();

		private HttpRequestImpl() {
		}

		private void parse(String raw) {
			// parse first line
			StringTokenizer tokenizer = new StringTokenizer(raw);
			this.method = tokenizer.nextToken().toUpperCase();
			this.location = tokenizer.nextToken();
			this.version = tokenizer.nextToken();
			// parse the headers
			String[] lines = raw.split("\r\n");
			for (int i = 1; i < lines.length; i++) {
				String[] keyVal = lines[i].split(":", 2);
				String key = keyVal[0];
				String value = keyVal[1];

				this.headers.put(key, value);
			}
		}

		@Override
		public String getVersion() {
			return this.version;
		}

		@Override
		public String getMethod() {
			return this.method;
		}

		@Override
		public String getLocation() {
			return this.location;
		}

		@Override
		public String getHead(String key) {
			return this.headers.get(key);
		}
	}
}