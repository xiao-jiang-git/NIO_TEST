package NIOTest;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * 缓冲区：负责NIO中数据的存取，缓冲区就是数组
 *
 * ByteBuffer
 * CharBuffer
 * ShortBuffer
 * IntBuffer
 * LongBuffer
 * FloatBuffer
 * DoubleBuffer
 *
 * get buffer through  allocate()//初始化缓冲区
 *
 *
 * 存取数据的核心方法：
 * put();
 * get();
 *
 *
 * 缓冲区中的核心属性
 * 1、int cap    缓冲区的容量
 * 2、int lim    缓冲区界限
 * 3、int pos    当前正在操作的位置
 * 4、int mark   标记，记住position的位置，并且可以通过reset()重新回到position的位置
 * 0 <= mark <= position <= limit <= capacity
 *
 * flip() 切换到读数据模式
 * 令limit = position
 * position归零
 *
 * rewind() 重新切换到读数据模式
 * 重复读数据
 *
 * clear() 清空缓冲区
 * 但是！！缓冲区中的数据依然存在
 * 只是数据处于被遗忘状态，下次直接覆盖
 *
 *
 * remaining() 看看position之后还剩下几个可操作的数据
 *
 *
 * 直接缓冲区 && 非直接缓冲区
 *
 * 非直接缓冲区：
 *  通过 allocate() 方法分配缓冲区，将缓冲区建立在JVM的内存中
 * 直接缓冲区：
 *  通过 allocateDirect() 方法将缓冲区直接建立在物理内存中，提高运行效率
 *
 */


public class TestBuffer {

    private String str = "abcde";

    @Test
    public void test1(){
        //分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //查看缓冲区的核心属性的初始化定义
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());

        buf.put(str.getBytes());
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());

        //切换为读数据模式
        buf.flip();
        System.out.println("-----------filp()----------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());

        byte[] dst = new byte[buf.limit()];
        buf.get(dst);
        System.out.println("------------get()----------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());

    }

    @Test
    public void Test2(){

        // 用的是 HeapByteBuffer 在堆中开辟空间（数组）
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //
        ByteBuffer buffers = ByteBuffer.allocateDirect(1024);
    }
}
