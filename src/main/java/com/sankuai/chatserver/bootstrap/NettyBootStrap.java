package com.sankuai.chatserver.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import com.sankuai.chatserver.handler.WebSocketHandler;

public class NettyBootStrap {

	private final static int port = 8080;

	private static void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap sb = new ServerBootstrap();
			sb.group(bossGroup, workerGroup).channel(ServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new HttpServerCodec());
							// 聚合http片段
							pipeline.addLast(new HttpObjectAggregator(65536));
							// 支持HTML5 文本传输
							pipeline.addLast(new ChunkedWriteHandler());
							pipeline.addLast(new WebSocketHandler());
						}
					});
			ChannelFuture f = sb.bind(port).sync();
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			// TODO
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		run();
	}
}
