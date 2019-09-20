package com.farpost.intellij.remotecall.handler;

import com.farpost.intellij.remotecall.model.RequestData;

/**
 * Responsible for processing incoming requests
 */
public interface RequestHandler {

  /**
   * Handle incoming request
   * @param request - request payload
   */
  void handle(RequestData request);

}
