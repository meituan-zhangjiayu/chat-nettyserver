/**
 * 
 */
package com.sankuai.chatserver.datas;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangjiayu
 * @date 2016年3月2日
 * @version 0.1
 */
public class ChannelCache {

	private static ConcurrentHashMap<String, Channel> channelCache = new ConcurrentHashMap<String, Channel>();

	public static void addToCache(String channelId, Channel channel) {
		channelCache.put(channelId, channel);
	}

	public static void removeFromCache(String channelId) {
		channelCache.remove(channelId);
	}

	public static Channel getByChannelId(String channelId) {
		return channelCache.get(channelId);
	}
}
