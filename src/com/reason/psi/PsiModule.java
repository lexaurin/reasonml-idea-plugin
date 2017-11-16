package com.reason.psi;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.reason.icons.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PsiModule extends PsiNamedElement {

    public PsiModule(ASTNode node) {
        super(node);
    }

    @Nullable
    public PsiScopedExpr getModuleBody() {
        return findChildByClass(PsiScopedExpr.class);
    }

    @NotNull
    PsiModuleName getModuleName() {
        return findNotNullChildByClass(PsiModuleName.class);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return getModuleName();
    }

    @Override
    public String getName() {
        return getModuleName().getText();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        // TODO: Module setName
        return null;
    }

    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return getModuleName().getText();
            }

            @Nullable
            @Override
            public String getLocationString() {
                PsiFile containingFile = getContainingFile();
                return containingFile == null ? null : containingFile.getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return Icons.MODULE;
            }
        };
    }

    @Override
    public String toString() {
        return "Module '" + getName() + "'";
    }

}