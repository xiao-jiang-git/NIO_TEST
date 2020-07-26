package NIOTest;


import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * 使用NIO完成网络通信的三个核心
 *
 * 1 通道：负责连接
 *      java.nio.Channel
 *          --SelectableChannel
 *              --SocketChannel
 *              --ServerSocketChannel
 *              --DatagramChannel
 *              --（FileChannel 没有非阻塞模式不在SelectableChannel中）
 *
 *              -Pipe.SinkChannel
 *              -Pipe.SourceChannel
 *
 * 2 缓冲区：负责存取数据
 * 3 选择器：是SelectableChannel的多路复用器。用于监控SelectableChannel的IO情况
 */
public class TestNIO {

    @Test
    public void client() throws IOException {
        //获取通道
        SocketChannel sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));
        //要读取本地文件，首先要有FileChannel
        FileChannel fileChannel = FileChannel.open(Paths.get("1.pdf"), StandardOpenOption.READ);
        //创建缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);
        //发送数据


        while(fileChannel.read(buf)!=-1){
            buf.flip();
            sc.write(buf);
            buf.clear();
        }

        //告诉服务端传输结束
        sc.shutdownOutput();

        //接收服务端的反馈
        int len = 0;
        while((len = sc.read(buf))!=-1){
            buf.flip();
            System.out.println(new String(buf.array(), 0, len));
            buf.clear();
        }

        fileChannel.close();
        sc.close();
    }

    @Test
    public void server() throws IOException {
        //开启服务端channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        FileChannel fileChannel = FileChannel.open(Paths.get("33.pdf"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        //绑定端口号
        ssc.bind(new InetSocketAddress(8000));
        //获取客户端连接通道
        SocketChannel socketChannel = ssc.accept();
        //读取客户端数据并保存到的本地
        ByteBuffer sbuf = ByteBuffer.allocate(1024);

        while(socketChannel.read(sbuf)!=-1){
            sbuf.flip();
            fileChannel.write(sbuf);
            sbuf.clear();

        }

        //发送反馈
        sbuf.put("SUCCESS".getBytes());
        sbuf.flip();
        socketChannel.write(sbuf);

        ssc.close();
        socketChannel.close();
        fileChannel.close();

    }


}
