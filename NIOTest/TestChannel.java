package NIOTest;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

/**
 * Channel 用于连接原节点与目标节点，负责缓冲区中的数据传输（NIO）
 *
 *通道的主要实现类
 *
 *
 * java.nio.channels.Channel
 *      FileChannel // documents
 *      SocketChannel //TCP
 *      ServerSocketChannel //TCP
 *      DatagramChannel //UDP
 *
 *
 * 获取通道
 *
 * 1\ getChannel()
 *      本地IO：
 *      FileInputStream/FileOutPutStream
 *      RandomAccessFile
 *
 *      网络IO:
 *      Socket
 *      ServerSocket
 *      DatagramSocket
 *
 * 2\ NIO2 针对各个通道提供了一个静态方法 open()
 * 3\ NIO2 工具类中的 newByteChannel()
 *
 *
 *4、
 * 通道之间的数据传输
 * transferFrom()
 * transferTo()
 *
 * 5、分散（Scatter）与聚集（Gather）
 * 分散读取（将通道中的数据分散到多个缓冲区中）依次将缓冲区填满
 * 聚集写入（将多个缓冲区的数据聚集到通道中）
 *
 *
 *
 * 6、Charset 字符集
 * 编码：字符串-》字节数组
 * 解码：字节数组-》字符串
 */

public class TestChannel {

    //1 利用通道
    @Test
    public void test1(){

        long start = System.currentTimeMillis(); //1000
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            fis = new FileInputStream("1.pdf");
            fos = new FileOutputStream("2.pdf");

            //获取通道
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();

            //分派缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            //将通道中的数据存入缓冲区
            while(inChannel.read(buf) != -1){

                buf.flip();

                //将缓冲区的数据写入通道中
                outChannel.write(buf);

                buf.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(outChannel!=null){
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outChannel!=null){
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if(outChannel!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if(outChannel!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }


        long end = System.currentTimeMillis();

        System.out.println("use time:" + (end-start));
    }



    //直接缓冲区完成文件的复制

    @Test
    public void test2() throws IOException {
        long start = System.currentTimeMillis();

        //java 1.7
        FileChannel inChannel = FileChannel.open(Paths.get("1.pdf"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("3.pdf"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

        //内存映射文件 通过FileChannel的map()方法来将文件区域直接映射到内存中创建， 该方法返回MappedByteBuffer （只有ByteBuffer支持）
        MappedByteBuffer inMappedBuff = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size()); //从0开始读，读channel大小的数据
        MappedByteBuffer outMappedBuff = outChannel.map(FileChannel.MapMode.READ_WRITE, 0 , inChannel.size());

        //这样做不需要通道，直接操作缓冲区
        byte[] dst = new byte[inMappedBuff.limit()];
        inMappedBuff.get(dst);
        outMappedBuff.put(dst);


        inChannel.close();
        outChannel.close();


        long end = System.currentTimeMillis();
        System.out.println("use time:" + (end-start));
    }



    // 通道之间的数据传输(直接缓冲区)

    @Test
    public void test4() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("1.pdf"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("2.pdf"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

        inChannel.transferTo(0, inChannel.size(),outChannel);
        outChannel.close();
        inChannel.close();
    }

    @Test
    public void test5() throws IOException {

        //分散读取
        RandomAccessFile raf1 = new RandomAccessFile("1.pdf", "rw");
        FileChannel channel1 = raf1.getChannel();

        ByteBuffer bf1 = ByteBuffer.allocate(100);
        ByteBuffer bf2 = ByteBuffer.allocate(1000);

        ByteBuffer[] bufs = {bf1, bf2};
        channel1.read(bufs);

        for(ByteBuffer byteBuffer : bufs){
            byteBuffer.flip();
        }

        System.out.println(new String(bufs[0].array(), 0 , bufs[0].limit()));
        System.out.println("--------------");
        System.out.println(new String(bufs[1].array(), 0 , bufs[1].limit()));



        //聚集写入(按顺序将ByteBuffer数组中的数据写入到文件中去)
        RandomAccessFile raf2 = new RandomAccessFile("2.txt","rw");
        FileChannel channel2 = raf2.getChannel();
        channel2.write(bufs);

    }

    @Test
    public void test6() throws CharacterCodingException {

        /*展示所有编码类型
        Map<String, Charset> map = Charset.availableCharsets();
        Set<Map.Entry<String , Charset>> set = map.entrySet();

        for(Map.Entry<String,Charset> entry : set){
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

         */


        Charset cs1 = Charset.forName("GBK");

        //获取编码器
        CharsetEncoder ce = cs1.newEncoder();

        //获取解码器
        CharsetDecoder cd = cs1.newDecoder();

        CharBuffer cBuf = CharBuffer.allocate(1024);
        cBuf.put("江潇");
        cBuf.flip();

        ByteBuffer bBuf = ce.encode(cBuf);
        for(int i = 0; i<4; i++){
            System.out.println(bBuf.get());
        }

        bBuf.flip();
        CharBuffer cbuf2 = cd.decode(bBuf);
        System.out.println(cbuf2.toString());

        System.out.println("--------------");

        bBuf.flip();
        Charset cs2 = Charset.forName("UTF-8");
        CharBuffer bBuf2 = cs2.decode(bBuf);
        System.out.println(bBuf2.toString());


    }

}
