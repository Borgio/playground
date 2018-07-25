package playground.java.nio.tcp;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@swim.ai>
 * @since 7/24/18
 */
public class ServerMain {

    static final int PORT = 9002;

    static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    public static void main(String[] args) throws Exception {

        Selector selector = Selector.open();

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        InetSocketAddress address = new InetSocketAddress(PORT);
        ServerSocket ss = ssc.socket();
        ss.bind(address);

        SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println(TIMESTAMP_FORMAT.format(new Date()) + ": TCP server bound to port " + PORT);

        while(true) {

            int selectedKeyCount = selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            for(Iterator<SelectionKey> i = selectedKeys.iterator(); i.hasNext(); ) {

                //
                // figure out what kind of I/O event was selected
                //

                SelectionKey k = i.next();

                if ((k.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {

                    //
                    // new connection
                    //

                    System.out.println(TIMESTAMP_FORMAT.format(new Date()) + ": new connection");

                    //
                    // remove the key from the set
                    //

                    i.remove();

                    ServerSocketChannel c = (ServerSocketChannel)k.channel();
                    SocketChannel sc = c.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);

                    //
                    // TODO this is where we pass the channel to a console subsystem to send data back on it
                    //

                }
                else if ((k.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {

                    //
                    // remove the key from the set
                    //

                    i.remove();

                    //
                    // read the data
                    //

                    SocketChannel sc = (SocketChannel)k.channel();

                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    int bytesRead = sc.read(buffer);

                    if (bytesRead == -1) {

                        //
                        // TCP connection is closed
                        //

                        System.out.println(TIMESTAMP_FORMAT.format(new Date()) + ": TCP connection closed");

                        //
                        // unregister the channel, by canceling the key. If we don't we'll always get a selection event
                        // on a closed channel
                        //

                        k.cancel();

                    }
                    else {

                        buffer.flip();

                        byte[] content = new byte[bytesRead];

                        buffer.get(content, 0, bytesRead);

                        System.out.println(TIMESTAMP_FORMAT.format(new Date()) + ": " + new String(content));
                    }
                }
            }
        }
    }
}
