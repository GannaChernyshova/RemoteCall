package com.farpost.intellij.remotecall.handler;

import com.farpost.intellij.remotecall.model.RequestDto;
import com.farpost.intellij.remotecall.utils.FileNavigator;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpenFileMessageHandlerTest {

  private static OpenFileMessageHandler handler;
  private static StubFileNavigator fileNavigator;
  private static RequestDto request;

  @BeforeClass
  public static void setUp() {
    fileNavigator = new StubFileNavigator();
    handler = new OpenFileMessageHandler(fileNavigator);
    request = new RequestDto();
  }

  @Test
  public void handlerShouldExtractFilenameAndLineFromMessage() {
    request.setTarget("FileName.java:80");
    handler.handleMessage(request);
    assertEquals("FileName.java", fileNavigator.getFileName());
    assertEquals(79, fileNavigator.getLine());
    assertEquals(0, fileNavigator.getColumn());

    request.setTarget("FileName.java");
    handler.handleMessage(request);
    assertEquals("FileName.java", fileNavigator.getFileName());
    assertEquals(0, fileNavigator.getLine());
    assertEquals(0, fileNavigator.getColumn());

    request.setTarget("FileName.java:error");
    handler.handleMessage(request);
    assertEquals("FileName.java:error", fileNavigator.getFileName());
    assertEquals(0, fileNavigator.getLine());
    assertEquals(0, fileNavigator.getColumn());
  }

  @Test
  public void handlerShouldExtractFileNameFromFullWindowsPath() {
    request.setTarget("c:\\FileName.java");
    handler.handleMessage(request);
    assertEquals("c:\\FileName.java", fileNavigator.getFileName());
    assertEquals(0, fileNavigator.getLine());
    assertEquals(0, fileNavigator.getColumn());

    request.setTarget("c:\\FileName.java:80");
    handler.handleMessage(request);
    assertEquals("c:\\FileName.java", fileNavigator.getFileName());
    assertEquals(79, fileNavigator.getLine());
    assertEquals(0, fileNavigator.getColumn());

    request.setTarget("c:\\FileName.java:80:20");
    handler.handleMessage(request);
    assertEquals("c:\\FileName.java", fileNavigator.getFileName());
    assertEquals(79, fileNavigator.getLine());
    assertEquals(19, fileNavigator.getColumn());
  }

  @Test
  public void handlerShouldExtractLineNumberAfterHashCharacter() {
    request.setTarget("FileName.java#80");
    handler.handleMessage(request);
    assertEquals("FileName.java", fileNavigator.getFileName());
    assertEquals(79, fileNavigator.getLine());
    assertEquals(0, fileNavigator.getColumn());
  }

  @Test
  public void handlerShouldExtractLineAndColumnNumberAfterColon() {
    request.setTarget("FileName.java#80#20");
    handler.handleMessage(request);
    assertEquals("FileName.java", fileNavigator.getFileName());
    assertEquals(79, fileNavigator.getLine());
    assertEquals(19, fileNavigator.getColumn());
  }

  @Test
  public void handlerShouldExtractLineAndColumnNumberAfterHashCharacter() {
    request.setTarget("FileName.java:80:20");
    handler.handleMessage(request);
    assertEquals("FileName.java", fileNavigator.getFileName());
    assertEquals(79, fileNavigator.getLine());
    assertEquals(19, fileNavigator.getColumn());
  }
}

class StubFileNavigator implements FileNavigator {

  private String fileName;
  private int line;
  private int column;
  private String newLocator;

  @Override
  public void findAndNavigate(String fileName, int line, int column, RequestDto request) {
    this.fileName = fileName;
    this.line = line;
    this.column = column;
  }

  public String getFileName() {
    return fileName;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }
}
