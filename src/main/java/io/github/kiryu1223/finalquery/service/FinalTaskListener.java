package io.github.kiryu1223.finalquery.service;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

public class FinalTaskListener implements TaskListener
{
    private final Context context;

    public FinalTaskListener(Context context)
    {
        this.context = context;
    }

    @Override
    public void started(TaskEvent e)
    {

    }

    @Override
    public void finished(TaskEvent e)
    {
        if(e.getKind()== TaskEvent.Kind.ENTER)
        {
            CompilationUnitTree compilationUnit = e.getCompilationUnit();
            JCTree.JCCompilationUnit unit = (JCTree.JCCompilationUnit) compilationUnit;
            for (JCTree tree : unit.getTypeDecls())
            {
                if (tree instanceof JCTree.JCClassDecl)
                {
                    JCTree.JCClassDecl jcClassDecl = (JCTree.JCClassDecl) tree;
                    System.out.println(jcClassDecl.getSimpleName());
                    System.out.println(jcClassDecl.getImplementsClause());
                }
            }
        }
    }

    public Context getContext()
    {
        return context;
    }
}
