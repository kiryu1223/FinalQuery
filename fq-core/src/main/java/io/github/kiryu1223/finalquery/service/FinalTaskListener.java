package io.github.kiryu1223.finalquery.service;


import com.squareup.javapoet.*;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;
import io.github.kiryu1223.finalquery.annotation.Mapper;
import io.github.kiryu1223.finalquery.annotation.MapperImpl;
import io.github.kiryu1223.finalquery.annotation.MapperManager;
import io.github.kiryu1223.finalquery.annotation.SqlTemplate;
import io.github.kiryu1223.finalquery.service.util.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static io.github.kiryu1223.finalquery.service.util.TypeUtil.*;

public class FinalTaskListener implements TaskListener
{
    private final TreeMaker treeMaker;
    private final Names names;
    private final Filer filer;

    public FinalTaskListener(Context context)
    {
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
        JavacProcessingEnvironment instance = JavacProcessingEnvironment.instance(context);
        filer = instance.getFiler();
    }

    private final Set<String> importedImpl = new HashSet<>();

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
                    JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) tree;
                    Symbol.ClassSymbol classSymbol = classDecl.sym;
                    // mapper
                    if (classSymbol.isInterface() && classSymbol.getAnnotation(Mapper.class) != null)
                    {
                        startBuildImplClass(unit, classDecl);
                    }
                    // mapper控制中心
                    if (classSymbol.getAnnotation(MapperManager.class) != null)
                    {
                        String className = classSymbol.flatName().toString();
                        if (mapperNames.contains(className)) continue;
                        mapperNames.add(className);
                        mapperCtrl.add(unit);
                    }
                    // 是否为生成的源码
                    if (classSymbol.getAnnotation(MapperImpl.class) != null)
                    {
                        String fullName = classSymbol.flatName().toString();
                        if (importedImpl.contains(fullName)) continue;
                        importedImpl.add(fullName);

                        for (JCTree.JCCompilationUnit jcCompilationUnit : mapperCtrl)
                        {
                            ListBuffer<JCTree> defs = new ListBuffer<>();
                            JCTree.JCImport mapperImport = treeMaker.Import(treeMaker.Select(unit.getPackageName(), classSymbol.getInterfaces().get(0).asElement()), false);
                            JCTree.JCImport mapperImplImport = treeMaker.Import(treeMaker.Select(unit.getPackageName(), classDecl.getSimpleName()), false);
                            defs.add(mapperImport);
                            defs.add(mapperImplImport);
                            defs.addAll(jcCompilationUnit.defs);
                            jcCompilationUnit.defs = defs.toList();
                            for (JCTree typeDecl : jcCompilationUnit.getTypeDecls())
                            {
                                if (typeDecl.getKind() != Tree.Kind.CLASS) continue;
                                JCTree.JCClassDecl classDecl0 = (JCTree.JCClassDecl) typeDecl;
                                if (hasStaticBlock(classDecl0))
                                {
                                    for (JCTree member : classDecl0.getMembers())
                                    {
                                        if (member instanceof JCTree.JCBlock && ((JCTree.JCBlock) member).isStatic())
                                        {
                                            JCTree.JCBlock jcBlock = (JCTree.JCBlock) member;
                                            ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
                                            statements.addAll(jcBlock.getStatements());
                                            statements.add(setMapper(classDecl.sym.getInterfaces().get(0), classSymbol.asType()));
                                            jcBlock.stats = statements.toList();
                                        }
                                    }
                                }
                                else
                                {
                                    JCTree.JCBlock body = treeMaker.Block(Flags.STATIC, com.sun.tools.javac.util.List.of(setMapper(classDecl.sym.getInterfaces().get(0), classSymbol.asType())));
                                    ListBuffer<JCTree> statements = new ListBuffer<>();
                                    statements.addAll(classDecl0.getMembers());
                                    statements.add(body);
                                    classDecl0.defs = statements.toList();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private final Set<String> mapperNames = new HashSet<>();
    private final Set<JCTree.JCCompilationUnit> mapperCtrl = new HashSet<>();

    private void startBuildImplClass(JCTree.JCCompilationUnit unit, JCTree.JCClassDecl mapperClass)
    {
        String packageName = unit.getPackageName().toString();
        String implName = mapperClass.getSimpleName() + "Impl";
        String fullName = packageName + "." + implName;

        if (createdImpl.contains(fullName)) return;
        createdImpl.add(fullName);

        // 类名
        ClassName className = ClassName.get(packageName, implName);

        // datasource字段
        FieldSpec dataSource = FieldSpec.builder(DataSource.class, "dataSource", Modifier.PRIVATE).build();

        // datasource setter实现
        MethodSpec datasourceSetter = MethodSpec.methodBuilder("setDataSource")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(DataSource.class, "dataSource")
                .addStatement("this.dataSource = dataSource")
                .build();

        // 实现sql函数
        List<MethodSpec> sqlMethods = new ArrayList<>();
        for (JCTree member : mapperClass.getMembers())
        {
            if (!(member instanceof JCTree.JCMethodDecl)) continue;
            JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) member;
            Symbol.MethodSymbol methodSymbol = methodDecl.sym;
            SqlTemplate sqlTemplate = methodSymbol.getAnnotation(SqlTemplate.class);
            if (sqlTemplate == null) continue;
            Type returnType = methodSymbol.getReturnType();
            MethodSpec sqlMethod = MethodSpec.overriding(methodSymbol)
                    .addCode(genCode(dataSource, sqlTemplate.value(), returnType))
                    .build();
            sqlMethods.add(sqlMethod);
        }

        //拼装类
        TypeSpec implType = TypeSpec.classBuilder(className)
                .addAnnotation(MapperImpl.class)
                .addSuperinterface(mapperClass.sym.asType())
                .addModifiers(Modifier.PUBLIC)
                .addField(dataSource)
                .addMethod(datasourceSetter)
                .addMethods(sqlMethods)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, implType).build();
        try
        {
            javaFile.writeTo(filer);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private JCTree.JCStatement setMapper(Type mapperType, Type implType)
    {
        return treeMaker.Exec(
                treeMaker.Apply(
                        com.sun.tools.javac.util.List.nil(),
                        treeMaker.Ident(names.fromString("setMapper")),
                        com.sun.tools.javac.util.List.of(
                                treeMaker.Select(treeMaker.Ident(mapperType.asElement()), names._class),
                                treeMaker.NewClass(null, com.sun.tools.javac.util.List.nil(), treeMaker.Ident(implType.asElement()), com.sun.tools.javac.util.List.nil(), null)
                        )
                )
        );
    }

    private boolean hasStaticBlock(JCTree.JCClassDecl classDecl)
    {
        for (JCTree member : classDecl.getMembers())
        {
            if (member instanceof JCTree.JCBlock
                    && ((JCTree.JCBlock) member).isStatic())
            {
                return true;
            }
        }
        return false;
    }

    private CodeBlock genCode(FieldSpec dataSource, String sql, Type returnType)
    {
        boolean hasStar = false;
        PlainSelect plainSelect;
        try
        {
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            plainSelect = select.getPlainSelect();
            for (SelectItem<?> item : plainSelect.getSelectItems())
            {
                if (item.toString().equals("*") || item.toString().contains(".*"))
                {
                    hasStar = true;
                    break;
                }
            }
        }
        catch (JSQLParserException e)
        {
            throw new RuntimeException(e);
        }


//        Type TargetType = returnType.getTypeArguments().get(0);
        FieldSpec connection = FieldSpec.builder(Connection.class, "connection").build();
        FieldSpec preparedStatement = FieldSpec.builder(PreparedStatement.class, "preparedStatement").build();
        FieldSpec resultSet = FieldSpec.builder(ResultSet.class, "resultSet").build();

        FieldSpec entrySets = FieldSpec.builder(StringIntSet.class, "entrySets").build();
        return createCPR(dataSource, connection, preparedStatement, resultSet, sql, createBuild(hasStar, plainSelect, returnType, entrySets, resultSet));
//        return CodeBlock.builder()
//                .beginControlFlow("try ($T $N = $N.getConnection())", Connection.class, connection, dataSource)
//                .beginControlFlow("try ($T $N = $N.prepareStatement($S))", PreparedStatement.class, preparedStatement, connection, sql)
//                //.addStatement("$T $N = $N.prepareStatement($S)", PreparedStatement.class, preparedStatement, connection, sql)
//                .beginControlFlow("try ($T $N = $N.executeQuery())", ResultSet.class, resultSet, preparedStatement)
//                //.addStatement("$T $N = $N.executeQuery()", ResultSet.class, resultSet, preparedStatement)
//                .addStatement("$T $N = new $T()", TypeName.get(returnType), result, ParameterizedTypeName.get(ClassName.get(ArrayList.class), TypeName.get(TargetType)))
//                .addStatement(stringIntSet(TargetType, entrySets, resultSet, hasStar))
//                .beginControlFlow("while ($N.next())", resultSet)
//                .addStatement(newClass(TargetType, t))
//                .add(unKnowOrKnowIndex(sql, entrySets, t, resultSet, TargetType))
//                //for + switch
////                .beginControlFlow("for ($T $N : $N)", StringIntPair.class, entry, entrySets)
////                .add(sw(t, resultSet, entry, TargetType))
////                .endControlFlow()
//                .addStatement("$N.$L($N)", result, "add", t)
//                .endControlFlow()
//                .addStatement("return $N", result)
//                .endControlFlow()
//                .endControlFlow()
//                .endControlFlow()
//                .beginControlFlow("catch ($T e)", SQLException.class)
//                .addStatement("throw new $T(e)", RuntimeException.class)
//                .endControlFlow()
//                .build();
    }

    private CodeBlock createCPR(FieldSpec dataSource, FieldSpec connection, FieldSpec preparedStatement, FieldSpec resultSet, String sql, CodeBlock build)
    {
        FieldSpec e = FieldSpec.builder(SQLException.class, "e").build();
        return CodeBlock.builder()
                .beginControlFlow("try ($T $N = $N.$L())", Connection.class, connection, dataSource, "getConnection")
                .beginControlFlow("try ($T $N = $N.$L($S))", PreparedStatement.class, preparedStatement, connection, "prepareStatement", sql)
                .beginControlFlow("try ($T $N = $N.$L())", ResultSet.class, resultSet, preparedStatement, "executeQuery")
                .add(build)
                .endControlFlow()
                .endControlFlow()
                .nextControlFlow("catch ($T $N)", SQLException.class, e)
                .addStatement("throw new $T($N)", RuntimeException.class, e)
                .endControlFlow()
                .build();
    }

    private CodeBlock createBuild(boolean hasStar, PlainSelect plainSelect, Type returnType, FieldSpec entrySets, FieldSpec resultSet)
    {
        Type targetType = returnType.getTypeArguments().get(0);
        if (hasStar)
        {
            FieldSpec result = FieldSpec.builder(TypeName.get(returnType), "result").build();
            FieldSpec t = FieldSpec.builder(TypeName.get(targetType), "t").build();
            return CodeBlock.builder()
                    .addStatement(stringIntSet(targetType, entrySets, resultSet))
                    .addStatement("$T $N = new $T()", returnType, result, ParameterizedTypeName.get(ClassName.get(ArrayList.class), TypeName.get(targetType)))
                    .beginControlFlow("while ($N.next())", resultSet)
                    .addStatement(newClass(targetType, t))
                    .add(unKnowIndex(entrySets, t, resultSet, targetType))
                    .addStatement("$N.$L($N)", result, "add", t)
                    .endControlFlow()
                    .addStatement("return $N", result)
                    .build();
        }
        else
        {
            Map<Integer, FieldMetaData> names = getIntegerFieldMetaDataMap(plainSelect, targetType);
            FieldSpec result = FieldSpec.builder(TypeName.get(returnType), "result").build();
            FieldSpec t = FieldSpec.builder(TypeName.get(targetType), "t").build();
            return CodeBlock.builder()
                    .addStatement("$T $N = new $T()", returnType, result, ParameterizedTypeName.get(ClassName.get(ArrayList.class), TypeName.get(targetType)))
                    .beginControlFlow("while ($N.next())", resultSet)
                    .addStatement(newClass(targetType, t))
                    .add(knowIndex(entrySets, t, resultSet, targetType, names))
                    .addStatement("$N.$L($N)", result, "add", t)
                    .endControlFlow()
                    .addStatement("return $N", result)
                    .build();
        }
    }

    private static Map<Integer, FieldMetaData> getIntegerFieldMetaDataMap(PlainSelect plainSelect, Type targetType)
    {
        Map<Integer, FieldMetaData> names = new LinkedHashMap<>();
        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        TypeMetaData typeMetaData = TypeMetaData.get(targetType);
        List<FieldMetaData> fieldMetaData = typeMetaData.getFieldMetaData();
        for (int i = 0; i < selectItems.size(); i++)
        {
            SelectItem<?> item = selectItems.get(i);
            String itemName = item.toString();
            Alias alias = item.getAlias();
            String targetName = alias == null ? itemName : alias.getName();
            String cleared = SqlUtil.clearColumn(targetName);
            Optional<FieldMetaData> first = fieldMetaData.stream().filter(f -> f.getColumnName().equals(cleared)).findFirst();
            if (first.isPresent())
            {
                names.put(i + 1, first.get());
            }
        }
        return names;
    }

    private CodeBlock stringIntSet(Type TargetType, FieldSpec entrySets, FieldSpec resultSet)
    {
        StringBuilder sb = new StringBuilder();
        List<Object> args = new ArrayList<>();
        args.add(StringIntSet.class);
        args.add(entrySets);
        args.add(resultSet);
        sb.append("$T $N = getIndexEntrySet($N");
        TypeMetaData typeMetaData = TypeMetaData.get(TargetType);
        for (FieldMetaData fieldMetaData : typeMetaData.getFieldMetaData())
        {
            sb.append(",$S");
            args.add(fieldMetaData.getColumnName());
        }
        sb.append(")");
        return CodeBlock.builder()
                .add(sb.toString(), args.toArray())
                .build();
    }

    private CodeBlock newClass(Type TargetType, FieldSpec t)
    {
        return CodeBlock.builder()
                .add("$T $N = new $T()", TypeName.get(TargetType), t, TypeName.get(TargetType))
                .build();
    }

    private CodeBlock knowIndex(FieldSpec entrySets, FieldSpec t, FieldSpec resultSet, Type TargetType, Map<Integer, FieldMetaData> fieldMetaDataMap)
    {
        CodeBlock.Builder builder = CodeBlock.builder();
        int index = 1;
        for (Map.Entry<Integer, FieldMetaData> entry : fieldMetaDataMap.entrySet())
        {
            FieldMetaData metaData = entry.getValue();
            TypeName varType = TypeName.get(metaData.getType());
            FieldSpec value = FieldSpec.builder(varType, "value" + index).build();

            builder.add(getValue(metaData.getType(), value, resultSet, CodeBlock.of("$L", entry.getKey()), index))
                    .addStatement("$N.$L($N)", t, metaData.getSetterName(), value);
            index++;
        }
        return builder.build();
    }

    private CodeBlock unKnowIndex(FieldSpec entrySets, FieldSpec t, FieldSpec resultSet, Type TargetType)
    {
        FieldSpec entry = FieldSpec.builder(StringIntPair.class, "entry").build();
        return CodeBlock.builder()
                .beginControlFlow("for ($T $N : $N)", StringIntPair.class, entry, entrySets)
                .add(sw(t, resultSet, entry, TargetType))
                .endControlFlow()
                .build();
    }

    private CodeBlock sw(FieldSpec t, FieldSpec resultSet, FieldSpec entry, Type TargetType)
    {
        CodeBlock.Builder sw = CodeBlock.builder()
                .beginControlFlow("switch ($N.$L())", entry, "getName");
        int index = 1;
        TypeMetaData typeMetaData = TypeMetaData.get(TargetType);
        for (FieldMetaData metaData : typeMetaData.getFieldMetaData())
        {
            sw.add("case $S:\n", metaData.getColumnName());
            TypeName varType = TypeName.get(metaData.getType());
            FieldSpec value = FieldSpec.builder(varType, "value" + index).build();

            // getValue
            sw.add(getValue(metaData.getType(), value, resultSet, CodeBlock.of("$N.$L()", entry, "getIndex"), index));

            // setValue
            sw.addStatement("$N.$L($N)", t, metaData.getSetterName(), value);

            sw.addStatement("break");
            index++;
        }
        sw.endControlFlow();
        return sw.build();
    }

    private CodeBlock getValue(Type type, FieldSpec value, FieldSpec resultSet, CodeBlock index, int i)
    {
        if (isString(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getString", index)
                    .build();
        }
        else if (isChar(type))
        {
            FieldSpec temp = FieldSpec.builder(String.class, "temp" + i).build();
            return CodeBlock.builder()
                    // String temp = resultSet.getString(index)
                    .addStatement("$T $N = $N.$L($L)", String.class, temp, resultSet, "getString", index)
                    // char value = temp.charAt(0)
                    .addStatement("$T $N = $N.charAt(0)", char.class, value, temp)
                    .build();
        }
        else if (isByte(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getByte", index)
                    .build();
        }
        else if (isShort(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getShort", index)
                    .build();
        }
        else if (isInt(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getInt", index)
                    .build();
        }
        else if (isLong(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getLong", index)
                    .build();
        }
        else if (isBool(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getBoolean", index)
                    .build();
        }
        else if (isFloat(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getFloat", index)
                    .build();
        }
        else if (isDouble(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getDouble", index)
                    .build();
        }
        else if (isDate(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getDate", index)
                    .build();
        }
        else if (isTime(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getTime", index)
                    .build();
        }
        else if (isTimestamp(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getTimestamp", index)
                    .build();
        }
        else if (isLocalDate(type))
        {
            FieldSpec temp = FieldSpec.builder(Date.class, "temp" + i).build();
            return CodeBlock.builder()
                    // Date temp = resultSet.getDate(index)
                    .addStatement("$T $N = $N.$L($L)", Date.class, temp, resultSet, "getDate", index)
                    // LocalDate value = temp.toLocalDate()
                    .addStatement("$T $N = $N.toLocalDate()", LocalDate.class, value, temp)
                    .build();
        }
        else if (isLocalTime(type))
        {
            FieldSpec temp = FieldSpec.builder(Time.class, "temp" + i).build();
            return CodeBlock.builder()
                    // Time temp = resultSet.getTime(index)
                    .addStatement("$T $N = $N.$L($L)", Time.class, temp, resultSet, "getTime", index)
                    // LocalTime value = temp.toLocalTime()
                    .addStatement("$T $N = $N.toLocalTime()", LocalTime.class, value, temp)
                    .build();
        }
        else if (isLocalDateTime(type))
        {
            FieldSpec temp = FieldSpec.builder(Time.class, "temp" + i).build();
            return CodeBlock.builder()
                    // Timestamp temp = resultSet.getTimestamp(index)
                    .addStatement("$T $N = $N.$L($L)", Timestamp.class, temp, resultSet, "getTimestamp", index)
                    // LocalDateTime value = temp.toLocalDateTime()
                    .addStatement("$T $N = $N.toLocalDateTime()", LocalDateTime.class, value, temp)
                    .build();
        }
        else if (isBigDecimal(type))
        {
            return CodeBlock.builder()
                    .addStatement("$T $N = $N.$L($L)", TypeName.get(type), value, resultSet, "getBigDecimal", index)
                    .build();
        }
        else if (isBigInteger(type))
        {
            FieldSpec temp = FieldSpec.builder(Time.class, "temp" + i).build();
            return CodeBlock.builder()
                    // BigDecimal temp = resultSet.getBigDecimal(index)
                    .addStatement("$T $N = $N.$L($L)", BigDecimal.class, temp, resultSet, "getBigDecimal", index)
                    // BigInteger value = temp.toBigInteger()
                    .addStatement("$T $N = $N.toBigInteger()", BigInteger.class, value, temp)
                    .build();
        }
        else if (isEnum(type))
        {
            FieldSpec temp = FieldSpec.builder(Time.class, "temp" + i).build();
            return CodeBlock.builder()
                    // String temp = resultSet.getString(index)
                    .addStatement("$T $N = $N.$L($L)", String.class, temp, resultSet, "getString", index)
                    // Enum<T> value = Enum<T>.valueOf(temp)
                    .addStatement("$T $N = $T.valueOf($N)", TypeName.get(type), value, TypeName.get(type), temp)
                    .build();
        }
        else
        {
            throw new RuntimeException(type.toString());
        }
    }

    private final Set<String> createdImpl = new HashSet<>();
}
