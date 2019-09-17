package com.farpost.intellij.remotecall.handler;

import com.farpost.intellij.remotecall.model.RequestDto;

/**
 *
 */
public interface MessageHandler {

  void handleMessage(RequestDto request);

}
