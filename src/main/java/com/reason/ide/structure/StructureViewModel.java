package com.reason.ide.structure;

import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.reason.OclFile;
import com.reason.RmlFile;
import com.reason.lang.core.psi.PsiModuleFile;
import org.jetbrains.annotations.NotNull;

public class StructureViewModel extends StructureViewModelBase implements com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider {
    StructureViewModel(PsiFile psiFile) {
        super(psiFile, new StructureViewElement(PsiTreeUtil.getChildOfType(psiFile, PsiModuleFile.class)));
    }

    @NotNull
    public Sorter[] getSorters() {
        return new Sorter[]{Sorter.ALPHA_SORTER};
    }


    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element instanceof RmlFile || element instanceof OclFile;
    }
}