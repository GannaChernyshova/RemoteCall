package com.farpost.intellij.remotecall.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class PsiClassUtil {

  private PsiClassUtil() {
  }

  public static List<PsiClass> getClass(PsiJavaFile psiJavaFile) {
    List<PsiClass> classes = Arrays.asList(psiJavaFile.getClasses());
    return classes.stream()
      .filter(it->{
        PsiModifierList modifierList = it.getModifierList();
        return modifierList != null && modifierList.hasExplicitModifier(PsiModifier.PUBLIC) && !PsiUtil.isInnerClass(it);
      })
      .collect(Collectors.toList());
  }

}
