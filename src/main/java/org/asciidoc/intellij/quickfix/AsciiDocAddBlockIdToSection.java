package org.asciidoc.intellij.quickfix;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.asciidoc.intellij.psi.AsciiDocFileReference;
import org.asciidoc.intellij.psi.AsciiDocLink;
import org.asciidoc.intellij.psi.AsciiDocSection;
import org.asciidoc.intellij.psi.AsciiDocUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Schwartz 2020
 */
public class AsciiDocAddBlockIdToSection extends LocalQuickFixBase {
  public static final String NAME = "Add Block ID to Section";

  public AsciiDocAddBlockIdToSection() {
    super(NAME);
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    if (element instanceof AsciiDocLink) {
      AsciiDocSection section = ((AsciiDocLink) element).resolveAnchorForSection();
      if (section != null) {
        if (section.getBlockId() == null) {
          PsiElement firstChild = section.getFirstChild();
          String id = section.getAutogeneratedId();
          for (PsiReference reference : element.getReferences()) {
            if (reference instanceof AsciiDocFileReference && ((AsciiDocFileReference) reference).isAnchor()) {
              id = reference.getRangeInElement().substring(element.getText());
              break;
            }
          }
          for (PsiElement child : createBlockId(project,
            "[#" + id + "]").getChildren()) {
            section.addBefore(child,
              firstChild);
          }
        }
      }
    }
  }

  @NotNull
  private static PsiElement createBlockId(@NotNull Project project, @NotNull String text) {
    return AsciiDocUtil.createFileFromText(project, text);
  }
}
