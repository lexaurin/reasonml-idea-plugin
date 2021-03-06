package com.reason.ide.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.reason.lang.MlTypes;
import com.reason.lang.RmlLanguage;
import com.reason.lang.core.psi.*;
import com.reason.lang.ocaml.OclTypes;
import com.reason.lang.reason.RmlTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FoldingBuilder extends FoldingBuilderEx {
    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        PsiTreeUtil.processElements(root, element -> {
            IElementType elementType = element.getNode().getElementType();
            MlTypes types = elementType.getLanguage() == RmlLanguage.INSTANCE ? RmlTypes.INSTANCE : OclTypes.INSTANCE;
            if (types.COMMENT == elementType) {
                FoldingDescriptor fold = fold(element);
                if (fold != null) {
                    descriptors.add(fold);
                }
            } else if (types.TYPE_EXPRESSION == elementType) {
                foldType(descriptors, (PsiType) element);
            } else if (types.LET_EXPRESSION == elementType) {
                foldLet(descriptors, (PsiLet) element);
            } else if (types.MODULE_EXPRESSION == elementType) {
                foldModule(descriptors, (PsiModule) element);
            }
            return true;
        });

        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private void foldType(List<FoldingDescriptor> descriptors, PsiType typeExpression) {
        FoldingDescriptor fold = fold(typeExpression.getScopedExpression());
        if (fold != null) {
            descriptors.add(fold);
        }
    }

    private void foldLet(List<FoldingDescriptor> descriptors, PsiLet letExpression) {
        PsiFunBody functionBody = letExpression.getFunctionBody();
        if (functionBody != null) {
            FoldingDescriptor fold = fold(functionBody);
            if (fold != null) {
                descriptors.add(fold);
            }
        } else {
            PsiLetBinding letBinding = letExpression.getLetBinding();
            FoldingDescriptor fold = fold(letBinding);
            if (fold != null) {
                descriptors.add(fold);
            }
        }
    }

    private void foldModule(List<FoldingDescriptor> descriptors, PsiModule module) {
        FoldingDescriptor foldSignature = fold(module.getSignature());
        if (foldSignature != null) {
            descriptors.add(foldSignature);
        }

        FoldingDescriptor foldBody = fold(module.getBody());
        if (foldBody != null) {
            descriptors.add(foldBody);
        }
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        IElementType elementType = node.getElementType();
        if (elementType == RmlTypes.INSTANCE.COMMENT) {
            return "/*...*/";
        } else if (elementType == OclTypes.INSTANCE.COMMENT) {
            return "(*...*)";
        }

        return "{...}";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    @Nullable
    private FoldingDescriptor fold(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        TextRange textRange = element.getTextRange();
        return textRange.getLength() > 5 ? new FoldingDescriptor(element, textRange) : null;
    }
}
