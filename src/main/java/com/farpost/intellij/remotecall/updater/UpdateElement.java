package com.farpost.intellij.remotecall.updater;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateElement extends AnAction {

  final static String PAGA_AWARE_BY_ANNOTATION = "com.epam.sha.selenium.PageAwareFindBy";


  @Override
  public void actionPerformed(AnActionEvent e) {
    //TODO: window should show old locator value, new locator value, question: replace old locator?, YES NO buttons.
    // If Yes - perform replacement. If No - close window and no nothing.
    //Messages.showMessageDialog(e.getProject(), "some message info", "PSI Info", null);


    final PsiElement element = e.getData(PlatformDataKeys.PSI_FILE);

    //TODO: get class name from navigated file without hardcode
    PsiClass psiClass = (PsiClass)(element).getChildren()[4];

    List<PsiField> elements =
      Arrays.stream((psiClass).getFields()).filter(m -> m.hasAnnotation(PAGA_AWARE_BY_ANNOTATION)).collect(Collectors.toList());

    //TODO: works for only one PageAwareFindBy element in class now. Add filter through elements to define correct
    // one element where value of findBy equals to old locator value.
    PsiField field = elements.get(0);
    String value = field.getAnnotation(PAGA_AWARE_BY_ANNOTATION).findAttributeValue("findBy").getText();

    final String pageName = field.getAnnotation(PAGA_AWARE_BY_ANNOTATION).findAttributeValue("page").getText();
    //TODO: pass variable with new locator value from request instead of ".newClass
    final String tmsLinkText = String.format("@PageAwareFindBy(page = %s, findBy = @FindBy(css = \"%s\"))", pageName, ".newClass");
    final PsiAnnotation tmsLink = createAnnotation(tmsLinkText, field);
    final Project project = field.getProject();
    CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication().runWriteAction(() -> {
      field.getAnnotation(PAGA_AWARE_BY_ANNOTATION).delete();
      field.addBefore(tmsLink, field);

    }), "Migrate Allure TestCaseId", null);

  }

  public PsiAnnotation createAnnotation(final String annotation, final PsiElement context) {
    final PsiElementFactory factory = PsiElementFactory.SERVICE.getInstance(context.getProject());
    return factory.createAnnotationFromText(annotation, context);
  }

  public static void addImport(final PsiFile file, final String qualifiedName) {
    if (file instanceof PsiJavaFile) {
      addImport((PsiJavaFile)file, qualifiedName);
    }
  }

  public static void optimizeImports(final PsiFile file) {
    if (file instanceof PsiJavaFile) {
      optimizeImports((PsiJavaFile)file);
    }
  }

}
