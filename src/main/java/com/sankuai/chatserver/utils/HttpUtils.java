package com.sankuai.chatserver.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class HttpUtils {

	public static Map<String, String> getRequestParamsByUri(String uri) {
		Map<String, String> params = new HashMap<String, String>();
		if (StringUtils.isEmpty(uri) || uri.indexOf("?") <= 0) {
			return params;
		}
		String paramStr = uri.split("?")[1];
		String[] paramKVs = paramStr.split("&");
		for (String kv : paramKVs) {
			String[] kvArr = kv.split("=");
			params.put(kvArr[0], kvArr[1]);
		}
		return params;
	}
}
