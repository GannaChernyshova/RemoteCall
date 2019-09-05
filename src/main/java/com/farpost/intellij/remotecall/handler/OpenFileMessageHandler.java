package com.farpost.intellij.remotecall.handler;

import com.farpost.intellij.remotecall.utils.FileNavigator;
import com.intellij.openapi.util.text.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class OpenFileMessageHandler implements MessageHandler {
  private static final Pattern COLUMN_PATTERN = compile("[:#](\\d+)[:#]?(\\d*)$");
  private static final Pattern LOCATOR_PATTERN = compile("[:#](\\d+)[:#]?(\\D*)");
  private final FileNavigator fileNavigator;


  public OpenFileMessageHandler(FileNavigator fileNavigator) {
    this.fileNavigator = fileNavigator;
  }

  public void handleMessage(String message) {
    Matcher matcher = COLUMN_PATTERN.matcher(message);
    //TODO: verify locator pattern (example: button#id-1 or html > body > div > div > div > div > div > div:nth-child(1) > div > div )
    //http://localhost:8091/?message=MainPageWithFindBy.java:33:button#id-1 in request
    Matcher locatorMatcher = LOCATOR_PATTERN.matcher(message);
    int line = 0;
    int column = 0;
    String newLocator = "";

    if (matcher.find()) {

      line = StringUtil.parseInt(StringUtil.notNullize(matcher.group(1)), 1) - 1;
      final String columnNumberString = matcher.group(2);
      if (StringUtil.isNotEmpty(columnNumberString)) {
        column = StringUtil.parseInt(columnNumberString, 1) - 1;
      }
    }

    if (locatorMatcher.find()) {
      newLocator = matcher.group(2);
    }

    fileNavigator.findAndNavigate(matcher.replaceAll(""), line, column, newLocator);
  }
}
