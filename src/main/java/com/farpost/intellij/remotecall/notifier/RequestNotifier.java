package com.farpost.intellij.remotecall.notifier;

import com.farpost.intellij.remotecall.handler.RequestHandler;

public interface RequestNotifier extends Runnable {

  void addRequestHandler(RequestHandler handler);

}
