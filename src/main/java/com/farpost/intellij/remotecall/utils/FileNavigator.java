package com.farpost.intellij.remotecall.utils;

import com.farpost.intellij.remotecall.model.RequestDto;

public interface FileNavigator {
  void findAndNavigate(String fileName, int line, int column, RequestDto request);
}
