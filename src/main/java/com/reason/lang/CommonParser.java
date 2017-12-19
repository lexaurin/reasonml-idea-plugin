package com.reason.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import static com.reason.lang.ParserScopeEnum.file;
import static com.reason.lang.ParserScopeType.*;
import static com.reason.lang.RmlTypes.FILE_MODULE;

public abstract class CommonParser implements PsiParser, LightPsiParser {

    @Override
    @NotNull
    public ASTNode parse(@NotNull IElementType elementType, @NotNull PsiBuilder builder) {
        parseLight(elementType, builder);
        return builder.getTreeBuilt();
    }

    @Override
    public void parseLight(IElementType elementType, PsiBuilder builder) {
        //builder.setDebugMode(true);
        builder = adapt_builder_(elementType, builder, this, null);
        PsiBuilder.Marker m = enter_section_(builder, 0, _COLLAPSE_, null);

        Stack<ParserScope> scopes = new Stack<>();
        ParserScope fileScope = new ParserScope(file, FILE_MODULE, null);

        parseFile(builder, scopes, fileScope);

        // if we have a scope at last position in file, without SEMI, we need to handle it here
        if (!scopes.empty()) {
            ParserScope scope = scopes.pop();
            while (scope != null) {
                scope.end();
                scope = scopes.empty() ? null : scopes.pop();
            }
        }

        fileScope.end();

        exit_section_(builder, 0, m, elementType, true, true, TRUE_CONDITION);
    }

    protected abstract void parseFile(PsiBuilder builder, Stack<ParserScope> scopes, ParserScope fileScope);

    @Nullable
    ParserScope endUntilScopeExpression(Stack<ParserScope> scopes, IElementType scopeElementType) {
        ParserScope scope = null;

        if (!scopes.empty()) {
            scope = scopes.peek();
            while (scope != null && scope.scopeType != scopeExpression && (scopeElementType == null || scope.scopeElementType != scopeElementType)) {
                scopes.pop().end();
                scope = getLatestScope(scopes);
            }
        }

        return scope;
    }

    @Nullable
    protected ParserScope end(Stack<ParserScope> scopes) {
        ParserScope scope = null;

        if (!scopes.empty()) {
            scope = scopes.peek();
            while (scope != null && scope.scopeType == any) {
                scopes.pop().end();
                scope = getLatestScope(scopes);
            }
        }

        return scope;
    }

    @Nullable
    ParserScope endUntilStart(Stack<ParserScope> scopes) {
        ParserScope scope = null;

        if (!scopes.empty()) {
            scope = scopes.peek();
            while (scope != null && scope.scopeType != startExpression && scope.scopeType != scopeExpression) {
                scopes.pop().end();
                scope = getLatestScope(scopes);
            }
        }

        return scope;
    }

    @Nullable
    ParserScope endUntilStartForced(Stack<ParserScope> scopes) {
        ParserScope scope = null;

        if (!scopes.empty()) {
            scope = scopes.peek();
            while (scope != null && scope.scopeType != startExpression) {
                scopes.pop().end();
                scope = getLatestScope(scopes);
            }
        }

        return scope;
    }

    @Nullable
    ParserScope getLatestScope(Stack<ParserScope> scopes) {
        return scopes.empty() ? null : scopes.peek();
    }

    ParserScope mark(PsiBuilder builder, Stack<ParserScope> scopes, ParserScopeEnum resolution, IElementType tokenType) {
        ParserScope scope = new ParserScope(resolution, tokenType, builder.mark());
        scope.scopeType = any;
        scopes.push(scope);
        return scope;
    }

    ParserScope markComplete(PsiBuilder builder, Stack<ParserScope> scopes, ParserScopeEnum resolution, IElementType tokenType) {
        ParserScope scope = mark(builder, scopes, resolution, tokenType);
        scope.complete = true;
        return scope;
    }

    ParserScope markScope(PsiBuilder builder, Stack<ParserScope> scopes, ParserScopeEnum resolution, IElementType tokenType, ParserScopeType scopeType, IElementType scopeElementType) {
        ParserScope scope = mark(builder, scopes, resolution, tokenType);
        scope.scopeType = scopeType;
        scope.scopeElementType = scopeElementType;
        return scope;
    }

    ParserScope markCompleteScope(PsiBuilder builder, Stack<ParserScope> scopes, ParserScopeEnum resolution, IElementType tokenType, ParserScopeType scopeType, IElementType scopeElementType) {
        ParserScope scope = markScope(builder, scopes, resolution, tokenType, scopeType, scopeElementType);
        scope.complete = true;
        return scope;
    }

    boolean advance(PsiBuilder builder) {
        builder.advanceLexer();
        return true;
    }

    boolean advance(PsiBuilder builder, IElementType elementType) {
        PsiBuilder.Marker mark = builder.mark();
        advance(builder);
        mark.done(elementType);
        return true;
    }

}
