package io.github.kiryu1223.finalquery.service;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;
import io.github.kiryu1223.finalquery.annotation.Mapper;
import io.github.kiryu1223.finalquery.annotation.SqlTemplate;
import io.github.kiryu1223.finalquery.api.BaseMapper;

import javax.annotation.processing.Filer;
import javax.sql.DataSource;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FinalTaskListener implements TaskListener
{
    private final Context context;
    private final TreeMaker treeMaker;
    private final Names names;
    private final Filer filer;
    private long parmaFlags = 8589934592L;

    public FinalTaskListener(Context context)
    {
        this.context = context;
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
        JavacProcessingEnvironment instance = JavacProcessingEnvironment.instance(context);
        filer = instance.getFiler();
    }

    @Override
    public void started(TaskEvent e)
    {

    }

    @Override
    public void finished(TaskEvent e)
    {
        if (e.getKind() == TaskEvent.Kind.ENTER)
        {
            CompilationUnitTree compilationUnit = e.getCompilationUnit();
            JCTree.JCCompilationUnit unit = (JCTree.JCCompilationUnit) compilationUnit;
            List<JCTree> typeDecls = unit.getTypeDecls();
            for (JCTree tree : typeDecls)
            {
                if (tree instanceof JCTree.JCClassDecl)
                {
                    JCTree.JCClassDecl mapperClass = (JCTree.JCClassDecl) tree;
                    Mapper mapper = mapperClass.sym.getAnnotation(Mapper.class);
                    if (mapper != null)
                    {
                        startBuildImplClass(unit, mapperClass);
                    }
                }
            }
        }
    }

    private void startBuildImplClass(JCTree.JCCompilationUnit unit, JCTree.JCClassDecl mapperClass)
    {
        String packageName = unit.getPackageName().toString();
        String implName = mapperClass.getSimpleName() + "Impl";
        String fullName = packageName + "." + implName;

        if (createdImpl.contains(fullName)) return;
        createdImpl.add(fullName);

        List<JCTree> overrides = buildMethods(mapperClass);
        JCTree.JCClassDecl implClass = treeMaker.ClassDef(treeMaker.Modifiers(Flags.PUBLIC), getName(implName), List.nil(), null, List.of(getIdent(BaseMapper.class)), overrides);
        ListBuffer<JCTree> def = new ListBuffer<>();
        def.addAll(unit.getImports());
        def.addAll(baseImports());
        def.add(implClass);
        JCTree.JCCompilationUnit jcCompilationUnit = treeMaker.TopLevel(
                unit.getPackageAnnotations(),
                unit.getPackageName(),
                def.toList()
        );
        tryCreateSourceFile(fullName, jcCompilationUnit.toString());
    }

    private List<JCTree> buildMethods(JCTree.JCClassDecl mapperClass)
    {
        ListBuffer<JCTree> result = new ListBuffer<>();
        result.add(buildDataSourceField());
        result.add(overrideDataSourceSetter());
        for (JCTree member : mapperClass.getMembers())
        {
            if (!(member instanceof JCTree.JCMethodDecl)) continue;
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) member;
            SqlTemplate sqlTemplate = jcMethodDecl.sym.getAnnotation(SqlTemplate.class);
            if (sqlTemplate == null) continue;
            JCTree.JCBlock newBody = buildBody(sqlTemplate.value(), jcMethodDecl.restype);
            JCTree.JCMethodDecl def = treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), jcMethodDecl.getName(), jcMethodDecl.restype, jcMethodDecl.getTypeParameters(), jcMethodDecl.getParameters(), jcMethodDecl.getThrows(), newBody, jcMethodDecl.defaultValue);
            result.add(def);
        }
        return result.toList();
    }

    private JCTree.JCVariableDecl buildDataSourceField()
    {
        return treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE), getName("dataSource"), getIdent(DataSource.class), null);
    }

    private JCTree.JCMethodDecl overrideDataSourceSetter()
    {
        JCTree.JCVariableDecl jcVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(parmaFlags), getName("dataSource"), getIdent(DataSource.class), null);
        JCTree.JCAssign this_dataSource_dataSource = treeMaker.Assign(treeMaker.Select(treeMaker.Ident(names._this), getName("dataSource")), getIdent("dataSource"));
        JCTree.JCExpressionStatement exec = treeMaker.Exec(this_dataSource_dataSource);
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), getName("setDataSource"), treeMaker.TypeIdent(TypeTag.VOID), List.nil(), List.of(jcVariableDecl), List.nil(), treeMaker.Block(0, List.of(exec)), null);
    }

    private JCTree.JCBlock buildBody(String sql, JCTree.JCExpression returnType)
    {
        return treeMaker.Block(0, List.of(getVar(Flags.FINAL, String.class, "sql", treeMaker.Literal(sql)), tryGetConnection(returnType)));
    }

    private JCTree.JCTry tryGetConnection(JCTree.JCExpression returnType)
    {
        return treeMaker.Try(
                List.of(getVar(Connection.class, "connection", getMethodCall("dataSource", "getConnection"))),
                treeMaker.Block(0, List.of(tryGetPreparedStatement(returnType))),
                List.of(getCatch(SQLException.class, "e")),
                null
        );
    }

    private JCTree.JCTry tryGetPreparedStatement(JCTree.JCExpression returnType)
    {
        return treeMaker.Try(
                List.of(getVar(PreparedStatement.class, "preparedStatement", getMethodCall("connection", "prepareStatement", List.of(getIdent("sql"))))),
                treeMaker.Block(0, List.of(tryGetResultSet(returnType))),
                List.of(getCatch(SQLException.class, "e")),
                null
        );
    }

    private JCTree.JCTry tryGetResultSet(JCTree.JCExpression returnType)
    {
        return treeMaker.Try(
                List.of(getVar(ResultSet.class, "resultSet", getMethodCall("preparedStatement", "executeQuery"))),
                treeMaker.Block(0, buildObjects(returnType)),
                List.of(getCatch(SQLException.class, "e")),
                null
        );
    }

    private List<JCTree.JCStatement> buildObjects(JCTree.JCExpression returnType)
    {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        JCTree.JCIdent targetType = getTargetType(returnType);
        Map<String, String> allField = getAllField((Symbol.ClassSymbol) targetType.sym);
        statements.add(getResultList(targetType));
        statements.add(getEntrySet(allField));
        statements.add(getResultSetLoop(targetType, allField));
        statements.add(getReturnNull());
        return statements.toList();
    }

    private JCTree.JCIdent getTargetType(JCTree.JCExpression returnType)
    {
        if (returnType instanceof JCTree.JCTypeApply)
        {
            JCTree.JCTypeApply type = (JCTree.JCTypeApply) returnType;
            return (JCTree.JCIdent) type.getTypeArguments().get(0);
        }
        else
        {
            throw new RuntimeException(returnType.toString());
        }
    }

//    private JCTree.JCVariableDecl getResult(JCTree.JCExpression returnType)
//    {
//        if (returnType instanceof JCTree.JCTypeApply)
//        {
//            JCTree.JCTypeApply type = (JCTree.JCTypeApply) returnType;
//            JCTree.JCIdent targetType = (JCTree.JCIdent) type.getTypeArguments().get(0);
//            return getResultList(targetType);
//        }
//        else
//        {
//            throw new RuntimeException(returnType.toString());
//        }
//    }

    private JCTree.JCVariableDecl getEntrySet(Map<String, String> allField)
    {
        ListBuffer<JCTree.JCExpression> args = new ListBuffer<>();
        args.add(getIdent("resultSet"));
        for (String s : allField.keySet())
        {
            args.add(treeMaker.Literal(s));
        }
        return treeMaker.VarDef(
                treeMaker.Modifiers(0),
                getName("entrySets"),
                treeMaker.TypeApply(
                        getIdent(Set.class),
                        List.of(treeMaker.TypeApply(getIdent("Map.Entry"), List.of(getIdent(String.class), getIdent(Integer.class))))
                ),
                getMethodCall("getIndexEntrySet", args.toList())
        );
    }

    private JCTree.JCWhileLoop getResultSetLoop(JCTree.JCIdent targetType, Map<String, String> allField)
    {
        JCTree.JCVariableDecl temp = treeMaker.VarDef(treeMaker.Modifiers(0), getName("t"), targetType, getNewClass(targetType));
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        JCTree.JCTypeApply map_Entry = treeMaker.TypeApply(getIdent("Map.Entry"), List.of(getIdent(String.class), getIdent(Integer.class)));
        treeMaker.ForeachLoop(getVar(map_Entry, "entry"), getIdent("entrySets"), treeMaker.Block(0, List.of(getSwitchFields())));
        statements.add(temp);
        return treeMaker.WhileLoop(getMethodCall("resultSet", "next"), treeMaker.Block(0, statements.toList()));
    }

    //    for (Map.Entry<String, Integer> entry : entrySets)
//    {
//        switch (entry.getKey())
//        {
//            case "dept_no":
//                department.setNumber(resultSet.getString(entry.getValue()));
//                break;
//            case "dept_name":
//                department.setName(resultSet.getString(entry.getValue()));
//                break;
//        }
//    }
    private JCTree.JCSwitch getSwitchFields()
    {
        return treeMaker.Switch(getMethodCall("entry", "getKey"), );
    }

    private Map<String, String> getAllField(Symbol.ClassSymbol classSymbol)
    {
        Map<String, String> stringStringMap = new HashMap<>();
        for (Symbol enclosedElement : classSymbol.getEnclosedElements())
        {
            if (enclosedElement instanceof Symbol.VarSymbol)
            {
                Symbol.VarSymbol varSymbol = (Symbol.VarSymbol) enclosedElement;
                stringStringMap.put(varSymbol.getSimpleName().toString(), varSymbol.getSimpleName().toString());
            }
        }
        return stringStringMap;
    }

    private JCTree.JCVariableDecl getResultList(JCTree.JCExpression targetType)
    {
        List<JCTree.JCExpression> gen = List.of(targetType);
        return treeMaker.VarDef(
                treeMaker.Modifiers(0),
                getName("result"),
                treeMaker.TypeApply(getIdent(java.util.List.class), gen),
                getNewClass(gen, ArrayList.class)
        );
    }


    private JCTree.JCNewClass getNewClass(Class<?> type)
    {
        return getNewClass(type.getSimpleName());
    }

    private JCTree.JCNewClass getNewClass(List<JCTree.JCExpression> typeargs, Class<?> type)
    {
        return getNewClass(typeargs, type.getSimpleName());
    }

    private JCTree.JCNewClass getNewClass(String type)
    {
        return getNewClass(type, List.nil());
    }

    private JCTree.JCNewClass getNewClass(String type, List<JCTree.JCExpression> args)
    {
        return getNewClass(List.nil(), type, args);
    }

    private JCTree.JCNewClass getNewClass(List<JCTree.JCExpression> typeargs, String type)
    {
        return getNewClass(typeargs, type, List.nil());
    }

    private JCTree.JCNewClass getNewClass(JCTree.JCIdent ident)
    {
        return treeMaker.NewClass(null, List.nil(), ident, List.nil(), null);
    }

    private JCTree.JCNewClass getNewClass(List<JCTree.JCExpression> typeargs, String type, List<JCTree.JCExpression> args)
    {
        return treeMaker.NewClass(null, typeargs, getIdent(type), args, null);
    }

    private JCTree.JCMethodInvocation getMethodCall(String methodName, List<JCTree.JCExpression> arg)
    {
        return treeMaker.Apply(List.nil(), getIdent(methodName), arg);
    }

    private JCTree.JCMethodInvocation getMethodCall(String caller, String methodName)
    {
        return getMethodCall(caller, methodName, List.nil());
    }

    private JCTree.JCMethodInvocation getMethodCall(String caller, String methodName, List<JCTree.JCExpression> arg)
    {
        return treeMaker.Apply(List.nil(), treeMaker.Select(getIdent(caller), getName(methodName)), arg);
    }

    private JCTree.JCReturn getReturnNull()
    {
        return treeMaker.Return(getNull());
    }

    private JCTree.JCCatch getCatch(Class<? extends Throwable> throwable, String name)
    {
        return treeMaker.Catch(getVar(throwable, name), treeMaker.Block(0, List.of(getThrow(RuntimeException.class, List.of(getIdent(name))))));
    }

    private JCTree.JCThrow getThrow(Class<? extends Throwable> throwable, List<JCTree.JCExpression> args)
    {
        return treeMaker.Throw(treeMaker.NewClass(null, List.nil(), getIdent(throwable), args, null));
    }

    private JCTree.JCLiteral getNull()
    {
        return treeMaker.Literal(TypeTag.BOT, null);
    }

    private JCTree.JCIdent getIdent(Class<?> type)
    {
        return getIdent(type.getSimpleName());
    }

    private JCTree.JCIdent getIdent(String name)
    {
        return treeMaker.Ident(getName(name));
    }

    private Name getName(String name)
    {
        return names.fromString(name);
    }

    private JCTree.JCVariableDecl getVar(JCTree.JCExpression type, String name)
    {
        return treeMaker.VarDef(treeMaker.Modifiers(0), getName(name), type, null);
    }

    private JCTree.JCVariableDecl getVar(Class<?> type, String name)
    {
        return getVar(0, type, name, null);
    }

    private JCTree.JCVariableDecl getVar(Class<?> type, String name, JCTree.JCExpression init)
    {
        return getVar(0, type, name, init);
    }

    private JCTree.JCVariableDecl getVar(long flags, Class<?> type, String name)
    {
        return getVar(flags, type, name, null);
    }

    private JCTree.JCVariableDecl getVar(long flags, Class<?> type, String name, JCTree.JCExpression init)
    {
        return treeMaker.VarDef(treeMaker.Modifiers(flags), getName(name), getIdent(type), init);
    }

    //import javax.sql.DataSource;
    //import java.sql.Connection;
    //import java.sql.PreparedStatement;
    //import java.sql.ResultSet;
    //import java.sql.SQLException;
    //import java.util.ArrayList;
    //import java.util.Arrays;
    //import java.util.List;
    //import java.util.Map;
    private ListBuffer<JCTree.JCImport> baseImports()
    {
        ListBuffer<JCTree.JCImport> imports = new ListBuffer<>();

        imports.add(makeImport(javax.sql.DataSource.class));
        imports.add(makeImport(java.sql.Connection.class));
        imports.add(makeImport(java.sql.PreparedStatement.class));
        imports.add(makeImport(java.sql.ResultSet.class));
        imports.add(makeImport(java.sql.SQLException.class));

        imports.add(makeImport(java.util.List.class));
        imports.add(makeImport(java.util.Set.class));
        imports.add(makeImport(java.util.Map.class));

        imports.add(makeImport(java.util.ArrayList.class));
        imports.add(makeImport(java.util.HashSet.class));
        imports.add(makeImport(java.util.HashMap.class));

        imports.add(makeImport(java.util.Arrays.class));

        return imports;
    }

    private JCTree.JCImport makeImport(Class<?> type)
    {
        String canonicalName = type.getCanonicalName();
        String[] split = canonicalName.split("\\.", 2);
        return treeMaker.Import(treeMaker.Select(getIdent(split[0]), getName(split[1])), false);
    }

    private final Set<String> createdImpl = new HashSet<>();

    private void tryCreateSourceFile(String fullName, String code)
    {
        try
        {
            JavaFileObject sourceFile = filer.createSourceFile(fullName);
            try (Writer writer = sourceFile.openWriter())
            {
                writer.write(code);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
