package NIOTest;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class TestPipe {

    @Test
    public void test1() throws IOException {
        Pipe pipe = Pipe.open();

        ByteBuffer buf = ByteBuffer.allocate(1024);

        // 将缓冲区中的数据写入管道
        Pipe.SinkChannel sinkChannel = pipe.sink();

        buf.put("通过单向管传输数据".getBytes());
        buf.flip();
        sinkChannel.write(buf);

        //读数据
        Pipe.SourceChannel sourceChannel = pipe.source();
        buf.flip();
        int len = sourceChannel.read(buf);

        System.out.println(new String(buf.array(), 0, len));

        sourceChannel.close();
        sinkChannel.close();

    }
}
