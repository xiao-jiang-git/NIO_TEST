package NIOTest;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

/**
 *
 * 添加Selector
 *
 */

public class TestUnblockingNIO {


    @Test
    public void Client() throws IOException {

        SocketChannel sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8080));
        //首先切换成非阻塞模式
        sc.configureBlocking(false);
        //分配缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //发送数据
        buf.put((new Date().toString()).getBytes());
        buf.flip();
        sc.write(buf);
        buf.clear();

        sc.close();

    }


    @Test
    public void Server() throws IOException {

        ServerSocketChannel ssc = ServerSocketChannel.open();

        ssc.configureBlocking(false);

        ssc.bind(new InetSocketAddress(8080));

        //获取选择器
        Selector selector = Selector.open();

        //将通道注册到选择器(selector, ops(检测当前状态：读1， 写4， 连接8， 接收16))
        //也可以监听多个状态 int ops = SelectionKey.OP_ACCEPT | SelectionKey.OP_CONNECT(监听的是接收事件和连接事件)；
        //只有监听的事件发生了才继续往下进行

        ssc.register(selector, SelectionKey.OP_ACCEPT);

        //轮巡式的获取选择器上的事件
        while(selector.select()>0){

            //获取当前选择器中所有注册的选择键（已就绪的监听事件）
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while(it.hasNext()){

                SelectionKey sk = it.next();
                //判断是什么时间准备就绪：如果接收就绪
                if(sk.isAcceptable()){
                    System.out.println("1");

                    //获取客户端连接
                    SocketChannel socketChannel = ssc.accept();

                    //把客户端连接也切换为非阻塞
                    socketChannel.configureBlocking(false);
                    //将该通道注册到选择器上
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if(sk.isReadable()){

                    //获取当前选择器上读就绪的通道
                    SocketChannel socketChannel = (SocketChannel)sk.channel();

                    //读数据
                    ByteBuffer buf = ByteBuffer.allocate(1024);

                    int len;
                    while((len = socketChannel.read(buf)) > 0){
                        buf.flip();
                        System.out.println(new String(buf.array(), 0, len));
                        buf.clear();
                    }

                }

                //取消选择键
                it.remove();
            }
        }

    }

}
