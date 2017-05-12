/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.psi.impl.beanProperties;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.JvmCommonIntentionActionsFactory;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UastContextKt;

import java.util.Arrays;

import static com.intellij.codeInspection.IntentionWrapper.wrapToQuickFix;
import static com.intellij.psi.CommonClassNames.JAVA_LANG_STRING;

@ApiStatus.Experimental
public class CreateBeanPropertyFixes {

  public static LocalQuickFix[] createFixes(String propertyName,
                                            @NotNull PsiClass psiClass,
                                            @Nullable PsiType type,
                                            final boolean createSetter) {
    return Arrays.stream(createActions(propertyName, psiClass, type, createSetter))
      .map(ia -> wrapToQuickFix(ia, psiClass.getContainingFile()))
      .toArray(LocalQuickFix[]::new);
  }

  public static IntentionAction[] createActions(String propertyName,
                                                @NotNull PsiClass psiClass,
                                                @Nullable PsiType type,
                                                final boolean createSetter) {
    if (type == null) {
      final Project project = psiClass.getProject();
      final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
      final PsiClass aClass = facade.findClass(JAVA_LANG_STRING, GlobalSearchScope.allScope(project));
      if (aClass == null) return IntentionAction.EMPTY_ARRAY;
      type = facade.getElementFactory().createType(aClass);
    }
    JvmCommonIntentionActionsFactory factory = JvmCommonIntentionActionsFactory.forLanguage(psiClass.getLanguage());
    if (factory == null) return IntentionAction.EMPTY_ARRAY;
    UClass uClass = UastContextKt.toUElement(psiClass, UClass.class);
    if (uClass == null) return IntentionAction.EMPTY_ARRAY;
    return factory.createAddBeanPropertyActions(uClass, propertyName, PsiModifier.PUBLIC, type, createSetter, !createSetter);
  }
}
