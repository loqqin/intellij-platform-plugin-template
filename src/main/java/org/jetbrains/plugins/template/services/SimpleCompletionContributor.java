package org.jetbrains.plugins.template.services;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SimpleCompletionContributor extends CompletionContributor {

  private static final Logger log = LoggerFactory.getLogger(SimpleCompletionContributor.class);

  // todo должно работать онли на 1 позиции курсора потому что в ifpresent мешает
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
          if (call == null) {
            return;
          }
          log.error("call " + call + " " + call.getText() + "  " + call.getArgumentList());
          log.error("refname " + call.getMethodExpression().getReferenceName());
          {

          }
          ArrayList<PsiField> fields = new ArrayList<>();
          if (call != null) {
            PsiMethod method = call.resolveMethod();
            if (method != null) {
              PsiClass containingClass = method.getContainingClass();
              if (containingClass != null) {
                String className = containingClass.getQualifiedName();
                for (PsiField field : containingClass.getFields()) {
                  log.error("field name " + field.getName() + " " + field.getType().getCanonicalText());
                }
                String methodName = method.getName();
                if ((methodName.equals("get") || methodName.equals("set") || methodName.equals("ifPresent")) /*&& className.equals("skyblock.utils.DataHolder")*/) {
                  if (InheritanceUtil.isInheritor(containingClass, "skyblock.utils.DataHolder")) {
                    System.out.println("inheritor");
                    PsiExpression qualifier = call.getMethodExpression().getQualifierExpression();
                    if (qualifier != null) {
                      PsiType qualifierType = qualifier.getType();
                      if (qualifierType != null) {
                        PsiClass qualifierClass = PsiUtil.resolveClassInType(qualifierType);
                        if (qualifierClass != null) {
                          System.out.println("qualifierClass.getName() = " + (qualifierClass.getQualifiedName()));
                          for (PsiField field : fields(qualifierClass)) {
                            if (field.hasModifierProperty(PsiModifier.STATIC)) {
                              String canonicalText = field.getType().getCanonicalText();
                              if (canonicalText.startsWith("skyblock.utils.DataHolder.Key<")) {
                                LookupElementBuilder lookupElement = LookupElementBuilder.create(qualifierClass.getName() + "." + field.getName())
                                  .withTypeText(field.getType().getPresentableText(), true)
                                  .withLookupStrings(List.of(field.getName(), field.getType().getCanonicalText()));
                                result.addElement(PrioritizedLookupElement.withPriority(lookupElement, 1000));
                                System.out.println("add field " + canonicalText);
//                                fields.add(field);
                              }
                              log.error("Static field: " + field.getName() + " "
                                + canonicalText);
                            }
                          }
                        }
                      }
                    }
                  } else {
                    System.out.println("not inheritor");
                  }

                }
                log.error("Method: " + methodName + " in class: " + className);
              }
            }
          }
//          result.addElement(LookupElementBuilder.create("Hello 123"));
          System.out.println("fields = " + (fields));
//          for (PsiField field : fields) {
//            result.addElement(LookupElementBuilder.create(field));
//          }
        }
      });
  }

  private static ArrayList<PsiField> fields(PsiClass psiClass) {
    PsiClass current = psiClass;
    ArrayList<PsiField> psiFields = new ArrayList<>();
    while (current != null) {
      for (PsiField field : current.getFields()) {
        if (field.hasModifierProperty(PsiModifier.STATIC)) {
          psiFields.add(field);
        }
      }
      current = current.getSuperClass(); // идём вверх по наследованию }
    }
    return psiFields;
  }
}