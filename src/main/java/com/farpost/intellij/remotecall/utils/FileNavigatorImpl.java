package com.farpost.intellij.remotecall.utils;

import com.farpost.intellij.remotecall.UserKeys;
import com.farpost.intellij.remotecall.model.RequestDto;
import com.google.common.base.Joiner;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.awt.*;
import java.io.File;
import java.util.*;

public class FileNavigatorImpl implements FileNavigator {

  private static final Logger log = Logger.getInstance(FileNavigatorImpl.class);
  private static final Joiner pathJoiner = Joiner.on("/");

  @Override
  public void findAndNavigate(final String fileName, final int line, final int column, RequestDto request) {
    ApplicationManager.getApplication().invokeLater(() -> {
      Map<Project, Collection<VirtualFile>> foundFilesInAllProjects = new HashMap<>();
      // collect all currently opened projects
      Project[] projects = ProjectManager.getInstance().getOpenProjects();

      // collect target file path in every available project
      for (Project project : projects) {
        foundFilesInAllProjects
          .put(project, FilenameIndex.getVirtualFilesByName(project, new File(fileName).getName(), GlobalSearchScope.allScope(project)));
      }

      Deque<String> pathElements = splitPath(fileName);
      String variableFileName = pathJoiner.join(pathElements);

      while (!pathElements.isEmpty()) {
        for (Project project : foundFilesInAllProjects.keySet()) {
          for (VirtualFile directFile : foundFilesInAllProjects.get(project)) {
            if (directFile.getPath().endsWith(variableFileName)) {
              log.info("Found file " + directFile.getName());
              navigate(project, directFile, line, column);
              IdeFocusManager.getInstance(project).doWhenFocusSettlesDown(() -> updateLocator(request));
              return;
            }
          }
        }
        pathElements.pop();
        variableFileName = pathJoiner.join(pathElements);
      }
    });
  }


  //TODO: pass somehow locator value to action event
  public static void updateLocator(RequestDto request) {
    ActionManager am = ActionManager.getInstance();
    DataManager dm = DataManager.getInstance();

    dm.getDataContextFromFocusAsync().onSuccess(context-> {
      dm.saveInDataContext(context, UserKeys.CUSTOM_DATA, request);
      AnActionEvent event = new AnActionEvent(null, context,
                                              ActionPlaces.UNKNOWN, new Presentation(),
                                              ActionManager.getInstance(), 0);
      am.getAction("updateBy").actionPerformed(event);
    });
  }

                                                                /**
   *
   * @param filePath
   * @return
   */
  private static Deque<String> splitPath(String filePath) {
    File file = new File(filePath);
    Deque<String> pathParts = new ArrayDeque<>();
    pathParts.push(file.getName());
    while ((file = file.getParentFile()) != null && !file.getName().isEmpty()) {
      pathParts.push(file.getName());
    }

    return pathParts;
  }

  /**
   * Open target project file and set focus to that window
   * @param project - target project
   * @param file - target file
   * @param line - line number
   * @param column - position
   */
  private static void navigate(Project project, VirtualFile file, int line, int column) {
    final OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, file, line, column);
    if (openFileDescriptor.canNavigate()) {
      log.info("Trying to navigate to " + file.getPath() + ":" + line);
      openFileDescriptor.navigate(true);
      Window parentWindow = WindowManager.getInstance().suggestParentWindow(project);
      if (parentWindow != null) {
        parentWindow.toFront();
      }
    }
    else {
      log.info("Cannot navigate");
    }
  }

}
