package com.farpost.intellij.remotecall.notifier;

import com.farpost.intellij.remotecall.handler.RequestHandler;
import com.farpost.intellij.remotecall.model.RequestData;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.HTTPMethod;
import org.apache.commons.net.io.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.net.URLDecoder.decode;

/**
 *
 */
public class SocketNotifier implements RequestNotifier {

  private static final Logger log = Logger.getInstance(SocketNotifier.class);
  private final Collection<RequestHandler> handlers = new HashSet<>();
  private final ServerSocket serverSocket;
  private static final String CRLF = "\r\n";
  private static final String NL = "\n";

  public SocketNotifier(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
  }

  public void addRequestHandler(RequestHandler handler) {
    handlers.add(handler);
  }

  public void run() {
    while (true) {
      Socket clientSocket;
      try {
        //noinspection SocketOpenedButNotSafelyClosed
        clientSocket = serverSocket.accept();
      }
      catch (IOException e) {
        if (serverSocket.isClosed()) {
          break;
        }
        else {
          log.error("Error while accepting", e);
          continue;
        }
      }

      InputStream inputStream = null;
      try {
        inputStream = clientSocket.getInputStream();
      }
      catch (IOException e) {
        log.error(e);
      }
      if (inputStream != null) {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        try {
          String inputLine;
          StringBuilder requestString = new StringBuilder();

          while ((inputLine = in.readLine()) != null && !inputLine.equals(CRLF) && !inputLine.equals(NL) && !inputLine.isEmpty()) {
            requestString.append(inputLine);
          }
          clientSocket.getOutputStream().write(("HTTP/1.1 204 No Content" + CRLF + "Access-Control-Allow-Origin: *" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8.name()));
          clientSocket.close();

          StringTokenizer tokenizer = new StringTokenizer(requestString.toString());
          String method = tokenizer.hasMoreElements() ? tokenizer.nextToken() : "";
          if (!method.equals(HTTPMethod.POST.name())) {
            log.warn("Only POST requests allowed");
            continue;
          }

          log.info("Received request " + requestString);
          Map<String, String> parameters = getParametersFromUrl(tokenizer.nextToken());

          RequestData data = new RequestData();

          String message = parameters.get("target") != null ? decode(parameters.get("target").trim(), StandardCharsets.UTF_8.name()) : "";
          String oldLocator = parameters.get("oldLocator") != null ? decode(parameters.get("oldLocator").trim(), StandardCharsets.UTF_8.name()) : "";
          String newLocator = parameters.get("newLocator") != null ? decode(parameters.get("newLocator").trim(), StandardCharsets.UTF_8.name()) : "";
          data.setTarget(message);
          data.setOldLocator(oldLocator);
          data.setNewLocator(newLocator);

          log.info("Received data " + data);
          handle(data);
        }
        catch (IOException e) {
          log.error("Error", e);
        }
        finally {
          Util.closeQuietly(in);
        }
      }
    }
  }

  /**
   * Parse input string to parameters map
   * @param url
   * @return
   */
  private static Map<String, String> getParametersFromUrl(String url) {
    String parametersString = url.substring(url.indexOf('?') + 1);
    Map<String, String> parameters = new HashMap<>();
    StringTokenizer tokenizer = new StringTokenizer(parametersString, "&");
    while (tokenizer.hasMoreElements()) {
      String[] parametersPair = tokenizer.nextToken().split("=", 2);
      if (parametersPair.length > 1) {
        parameters.put(parametersPair[0], parametersPair[1]);
      }
    }

    return parameters;
  }


  /**
   * Processing incoming request with handler
   * @param request
   */
  private void handle(RequestData request) {
    for (RequestHandler handler : handlers) {
      handler.handle(request);
    }
  }
}
