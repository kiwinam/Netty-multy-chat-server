package server;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;


public class ChatInitailizer extends ChannelInitializer<SocketChannel>{
	private static final StringDecoder STRING_DECODER = new StringDecoder(CharsetUtil.UTF_8);
	private static final StringEncoder STRING_ENCODER = new StringEncoder(CharsetUtil.UTF_8);
	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
		// TODO Auto-generated method stub
		ChannelPipeline pipeline = socketChannel.pipeline();
		
		//pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast(new ByteToMessageDecoder() {
			
			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				// TODO Auto-generated method stub
				out.add(in.readBytes(in.readableBytes()));
			}
		});
		pipeline.addLast(STRING_DECODER);
		pipeline.addLast(STRING_ENCODER);
		pipeline.addLast(new ChatHandler());
	}
	
}
