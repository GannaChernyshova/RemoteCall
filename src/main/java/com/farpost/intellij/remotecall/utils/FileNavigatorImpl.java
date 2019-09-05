package com.farpost.intellij.remotecall.utils;

import com.google.common.base.Joiner;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.awt.*;
import java.io.File;
import java.util.*;

public class FileNavigatorImpl implements FileNavigator {

  private static final Logger log = Logger.getInstance(FileNavigatorImpl.class);
  private static final Joiner pathJoiner = Joiner.on("/");

  @Override
  public void findAndNavigate(final String fileName, final int line, final int column, String newLocator) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        Map<Project, Collection<VirtualFile>> foundFilesInAllProjects = new HashMap<Project, Collection<VirtualFile>>();
        Project[] projects = ProjectManager.getInstance().getOpenProjects();

        for (Project project : projects) {
          foundFilesInAllProjects
            .put(project, FilenameIndex.getVirtualFilesByName(project, new File(fileName).getName(), GlobalSearchScope.allScope(project)));
        }

        Deque<String> pathElements = splitPath(fileName);
        String variableFileName = pathJoiner.join(pathElements);

        while (pathElements.size() > 0) {
          for (Project project : foundFilesInAllProjects.keySet()) {
            for (VirtualFile directFile : foundFilesInAllProjects.get(project)) {
              if (directFile.getPath().endsWith(variableFileName)) {
                log.info("Found file " + directFile.getName());
                navigate(project, directFile, line, column);
                updateLocator(newLocator);
                return;
              }
            }
          }
          pathElements.pop();
          variableFileName = pathJoiner.join(pathElements);
        }
      }
    });
  }


  //TODO: pass somehow locator value to action event
  public static void updateLocator(String locator) {
    ActionManager am = ActionManager.getInstance();
    am.getAction("updateBy").actionPerformed(new AnActionEvent(null, DataManager.getInstance().getDataContext(),
                                                                   ActionPlaces.UNKNOWN, new Presentation(),
                                                                   ActionManager.getInstance(), 0));
  }

  public static PsiAnnotation createAnnotation(final String annotation, final PsiElement context) {
    final PsiElementFactory factory = PsiElementFactory.SERVICE.getInstance(context.getProject());
    return factory.createAnnotationFromText(annotation, context);
  }

  private static Deque<String> splitPath(String filePath) {
    File file = new File(filePath);
    Deque<String> pathParts = new ArrayDeque<String>();
    pathParts.push(file.getName());
    while ((file = file.getParentFile()) != null && !file.getName().isEmpty()) {
      pathParts.push(file.getName());
    }

    return pathParts;
  }

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
