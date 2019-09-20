package com.farpost.intellij.remotecall.notifier;

import com.farpost.intellij.remotecall.handler.RequestHandler;
import com.farpost.intellij.remotecall.model.RequestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SocketMessageNotifierTest {

  private static final int PORT = 62775;
  private Thread notifierThread;
  private final StubMessageHandler messageHandler = new StubMessageHandler();
  private ServerSocket socket;

  @Before
  public void setUp() throws IOException {
    SocketNotifier notifier = createNotifier();
    notifier.addRequestHandler(messageHandler);

    notifierThread = new Thread(notifier);
    notifierThread.start();
  }

  @After
  public void tearDown() throws IOException {
    messageHandler.clear();
    notifierThread.interrupt();
    disposeNotifier();
  }

  @Test
  public void notifierShouldCallHandlerOnMessageReceived() throws IOException {
    sendMessage("GET /?message=HelloFile.java");
    assertEquals("HelloFile.java", messageHandler.getLastMessage());
  }

  @Test
  public void notifierShouldSkipEmptyMessages() throws IOException {
    sendMessage("");
    assertNull("Received " + messageHandler.getLastMessage() + ". Null expected", messageHandler.getLastMessage());
    messageHandler.clear();
  }

  @Test
  public void notifierShouldReceiveOnlyGetRequests() throws IOException {
    sendMessage("GET /?message=foo");
    assertEquals("foo", messageHandler.getLastMessage());
    messageHandler.clear();

    sendMessage("POST /\r\n\r\nmessage=bar");
    assertNull("Received " + messageHandler.getLastMessage() + ". Null expected", messageHandler.getLastMessage());
    messageHandler.clear();

    sendMessage("DELETE /?message=bar");
    assertNull("Received " + messageHandler.getLastMessage() + ". Null expected", messageHandler.getLastMessage());
    messageHandler.clear();
  }

  private SocketNotifier createNotifier() throws IOException {
    socket = new ServerSocket();
    socket.bind(new InetSocketAddress("localhost", PORT));
    return new SocketNotifier(socket);
  }

  private static void sendMessage(String message) throws IOException {
    Socket client = new Socket("localhost", PORT);
    try {
      client.getOutputStream().write(message.getBytes());
    }
    finally {
      client.close();
    }
  }

  private void disposeNotifier() throws IOException {
    socket.close();
  }
}

class StubMessageHandler implements RequestHandler {

  private final BlockingQueue<RequestData> messages = new LinkedBlockingQueue<>();

  @Override
  public void handle(RequestData message) {
    messages.add(message);
  }

  public RequestData getLastMessage() {
    try {
      return messages.poll(1, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void clear() {
    messages.clear();
  }

}

