package com.sankuai.chatserver.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.chatserver.datas.ChannelCache;
import com.sankuai.chatserver.utils.HttpUtils;

/**
 * 长连接请求的处理器
 * 
 * @author zhangjiayu
 * @date 2016年3月2日
 * @version 0.1
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {
	private final static Logger logger = Logger
			.getLogger(WebSocketHandler.class);

	private final static String WEB_SOCKET_PATH = "/websocket";

	private final static String TO_CHANNEL_ID_KEY = "toChannelId";

	private static final AttributeKey<String> channelIdAttrKey = AttributeKey
			.valueOf("channelId");

	private WebSocketServerHandshaker handshaker = null;

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof FullHttpRequest) {
			handleWebSocketConnection(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketRequest(ctx, (WebSocketFrame) msg);
		} else {
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("channel is active,remoteAddress="
				+ ctx.channel().remoteAddress());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 移除缓存
		ChannelCache
				.removeFromCache(ctx.channel().attr(channelIdAttrKey).get());
		super.channelInactive(ctx);
	}

	/**
	 * action=connect channelId=appkey+userid
	 * 
	 * @param ctx
	 * @param request
	 */
	private void handleWebSocketConnection(ChannelHandlerContext ctx,
			FullHttpRequest request) {
		if (request.decoderResult().isFailure()
				|| (!"websocket".equals(request.headers().get("Upgrade")))) {
			ctx.channel().writeAndFlush(
					new DefaultHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.BAD_REQUEST));
			ctx.channel().close();
		}
		// TODO 权限验证
		// 只接受GET请求
		if (!request.method().equals(HttpMethod.GET)) {
			ctx.channel().writeAndFlush(
					new DefaultHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.METHOD_NOT_ALLOWED));
			ctx.channel().close();
		}
		Map<String, String> params = HttpUtils.getRequestParamsByUri(request
				.uri());
		if (MapUtils.isEmpty(params)) {
			ctx.channel().writeAndFlush(
					new DefaultHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.BAD_REQUEST));
			ctx.channel().close();
		}
		String action = params.get("action");
		final String channelId = params.get("channelId");
		if (StringUtils.isEmpty(action) || StringUtils.isEmpty(channelId)) {
			ctx.channel().writeAndFlush(
					new DefaultHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.BAD_REQUEST));
			ctx.channel().close();
		}
		if (action.equals("connect")) {
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
					getWebSocketLocation(ctx.pipeline(), request), null, true);
			handshaker = wsFactory.newHandshaker(request);
			if (handshaker == null) {
				WebSocketServerHandshakerFactory
						.sendUnsupportedVersionResponse(ctx.channel());
			} else {
				ChannelFuture future = handshaker.handshake(ctx.channel(),
						request);
				final Channel channel = ctx.channel();
				future.addListener(new GenericFutureListener<Future<? super Void>>() {

					public void operationComplete(Future<? super Void> future)
							throws Exception {
						if (future.isSuccess()) {
							// 将channelId和channel的对应关系放到本地缓存
							channel.attr(channelIdAttrKey).set(channelId);
							ChannelCache.addToCache(channelId, channel);
						} else {
							logger.warn("websocket connect error,channelId="
									+ channelId);
						}
					}
				});
			}
		}
	}

	private String getWebSocketLocation(ChannelPipeline cp,
			FullHttpRequest request) {
		String protocol = "ws";
		if (cp.get(SslHandler.class) != null) {
			protocol = "wss";
		}
		return protocol + "://" + request.headers().getAndConvert("Host")
				+ WEB_SOCKET_PATH;
	}

	private void handleWebSocketRequest(ChannelHandlerContext ctx,
			WebSocketFrame frame) {
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(),
					(CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(
					new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format(
					"%s frame type not supported!", frame.getClass().getName()));
		}
		String request = ((TextWebSocketFrame) frame).text();
		JSONObject reqObj = JSON.parseObject(request);
		String toChannelId = reqObj.getString(TO_CHANNEL_ID_KEY);
		Channel localChannel = ChannelCache.getByChannelId(toChannelId);
		if (localChannel != null && localChannel.isActive()) {
			
		}
	}

	private void pushMessage(Channel channel, String content) {
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		super.exceptionCaught(ctx, cause);
	}

}
