package org.jetbrains.plugins.template.services;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleCompletionContributor extends CompletionContributor {

  private static final Logger log = LoggerFactory.getLogger(SimpleCompletionContributor.class);

  public SimpleCompletionContributor() {
    extend(CompletionType.BASIC,
      PsiJavaPatterns.psiElement()
        .withParent(PsiExpression.class).inside(PsiExpressionList.class),
      new CompletionProvider<>() {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
          @NotNull ProcessingContext context,
          @NotNull CompletionResultSet result) {
          log.error("con " + context);
          log.error("res " + result);
          log.error("par " + parameters);
          // Получи позицию курсора
          PsiElement position = parameters.getPosition();
          log.error("Position: " + position.getText());
          Language language = position.getLanguage();
          log.error("Language: " + language);
          PsiElement parent = position.getParent();
          log.error("parent " + parent);
          log.error("Parent: " + parent.getClass().getName());
          PsiMethodCallExpression call = PsiTreeUtil.getParentOfType(parameters.getPosition(), PsiMethodCallExpression.class);
          log.error("call " + call + " " + call.getText() + "  " + call.getArgumentList());
          log.error("refname " + call.getMethodExpression().getReferenceName());
          if (call != null) {
            PsiMethod method = call.resolveMethod();
            if (method != null) {
              PsiClass containingClass = method.getContainingClass();
              if (containingClass != null) {
                String className = containingClass.getQualifiedName();
                String methodName = method.getName();
                log.error("Method: " + methodName + " in class: " + className);
              }
            }
          }
          result.addElement(LookupElementBuilder.create("Hello 123"));
        }
      });
  }

}
