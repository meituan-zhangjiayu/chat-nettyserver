package com.sankuai.chatserver.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslHandler;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.sankuai.chatserver.utils.HttpUtils;

public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

	private final static String WEB_SOCKET_PATH = "websocket";

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg)
			throws Exception {

	}

	/**
	 * action=connect channelId=appkey+userid
	 * 
	 * @param ctx
	 * @param request
	 */
	private void handleWebSocketConnection(ChannelHandlerContext ctx,
			FullHttpRequest request) {
		// TODO 权限验证
		// 只接受GET请求
		if (!request.method().equals(HttpMethod.GET)) {
			ctx.channel().writeAndFlush(
					new DefaultHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.METHOD_NOT_ALLOWED));
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
		String channelId = params.get("channelId");
		if (StringUtils.isEmpty(action) || StringUtils.isEmpty(channelId)) {
			ctx.channel().writeAndFlush(
					new DefaultHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.BAD_REQUEST));
			ctx.channel().close();
		}
		if (action.equals("connect")) {
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
					getWebSocketLocation(ctx.pipeline(), request), null, true);

		}
	}

	private String getWebSocketLocation(ChannelPipeline cp,
			FullHttpRequest request) {
		String protocol = "ws";
		if (cp.get(SslHandler.class) != null) {
			protocol = "wss";
		}
		return protocol + "://" + request.headers() + WEB_SOCKET_PATH; // TODO
	}

	private void handleWebSocketRequest(ChannelHandlerContext ctx,
			WebSocketFrame request) {

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		super.exceptionCaught(ctx, cause);
	}

}
