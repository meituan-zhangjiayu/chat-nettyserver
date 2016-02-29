package com.sankuai.chatserver.datas;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelCache {

	private static ConcurrentHashMap<String, Channel> channelCache = new ConcurrentHashMap<String, Channel>();

	public static void addToCache(String channelId, Channel channel) {
		channelCache.put(channelId, channel);
	}

	public static void removeFromCache(String channelId) {
		channelCache.remove(channelId);
	}
}
