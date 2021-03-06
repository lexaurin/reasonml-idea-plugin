package com.reason.icons;

import com.intellij.psi.PsiElement;
import com.reason.lang.core.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class IconProvider extends com.intellij.ide.IconProvider {
    @Nullable
    @Override
    public Icon getIcon(@NotNull PsiElement element, int flags) {
        if (element instanceof PsiModule) {
            return Icons.MODULE;
        } else if (element instanceof PsiType) {
            return Icons.TYPE;
        } else if (element instanceof PsiLet) {
            PsiLet let = (PsiLet) element;
            return let.isObject() ? Icons.OBJECT : Icons.LET;
        } else if (element instanceof PsiExternal) {
            return Icons.EXTERNAL;
        } else if (element instanceof PsiVal) {
            return Icons.VAL;
        }
        return null;
    }
}
