package com.farpost.intellij.remotecall.updater;

import com.farpost.intellij.remotecall.UserKeys;
import com.farpost.intellij.remotecall.model.RequestDto;
import com.farpost.intellij.remotecall.utils.PsiClassUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.util.Arrays;

/**
 *
 */
public class UpdateElement extends AnAction {

  private static final Logger log = Logger.getInstance(UpdateElement.class);
  private static final String PAGE_AWARE_BY_ANNOTATION = "com.epam.sha.selenium.PageAwareFindBy";

  @Override
  public void actionPerformed(AnActionEvent e) {
    //TODO: window should show old locator value, new locator value, question: replace old locator?, YES NO buttons.
    // If Yes - perform replacement. If No - close window and no nothing.
    //Messages.showMessageDialog(e.getProject(), "some message info", "PSI Info", null);

    final PsiJavaFile element = (PsiJavaFile)e.getData(CommonDataKeys.PSI_FILE);
    final RequestDto data = new DataContextWrapper(e.getDataContext()).getUserData(UserKeys.CUSTOM_DATA);

    if(element == null){
      log.error("Failed to get target file data");
      return;
    }

    PsiClassUtil.getClass(element).stream()
      .flatMap(it-> Arrays.stream(it.getFields()))
      .filter(m -> m.hasAnnotation(PAGE_AWARE_BY_ANNOTATION))
      .filter(field -> {
        String value = field.getAnnotation(PAGE_AWARE_BY_ANNOTATION).findAttributeValue("findBy").getText();
        String pageName = field.getAnnotation(PAGE_AWARE_BY_ANNOTATION).findAttributeValue("page").getText();
        return value.contains(data.getOldLocator()) && pageName.contains(data.getTarget());
      })
      .forEach(it->{
        final String tmsLinkText = String.format("@PageAwareFindBy(page = \"%s\", findBy = @FindBy(css = \"%s\"))", data.getTarget(), data.getNewLocator());
        final PsiAnnotation tmsLink = createAnnotation(tmsLinkText, it);
        final Project project = it.getProject();
        CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication().runWriteAction(() -> {
          it.getAnnotation(PAGE_AWARE_BY_ANNOTATION).delete();
          PsiModifierList modifierList = it.getModifierList();
          modifierList.add(tmsLink);
        }), "Migrate Allure TestCaseId", null);
      });
  }

  /**
   *
   * @param annotation
   * @param context
   * @return
   */
  private PsiAnnotation createAnnotation(final String annotation, final PsiElement context) {
    final PsiElementFactory factory = PsiElementFactory.SERVICE.getInstance(context.getProject());
    return factory.createAnnotationFromText(annotation, context);
  }

}
