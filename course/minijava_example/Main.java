import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        SymbolTable ST = new SymbolTable();
        ST_Class c1 = new ST_Class();
        ST_Class c2 = new ST_Class();
        ST.addClass("nik1", c1);
        ST.addClass("nik2", c2);
        System.out.println(ST.getClass("nik1"));
        System.out.println(ST.getClass("nik2"));
        System.out.println(ST.getClass("nik3"));
        // if (args.length != 1) {
        // System.err.println("Usage: java Main <inputFile>");
        // System.exit(1);
        // }

        // FileInputStream fis = null;
        // try {
        // fis = new FileInputStream(args[0]);
        // MiniJavaParser parser = new MiniJavaParser(fis);

        // Goal root = parser.Goal();

        // System.err.println("Program parsed successfully.");

        // MyVisitor eval = new MyVisitor();
        // root.accept(eval, null);
        // } catch (ParseException ex) {
        // System.out.println(ex.getMessage());
        // } catch (FileNotFoundException ex) {
        // System.err.println(ex.getMessage());
        // } finally {
        // try {
        // if (fis != null)
        // fis.close();
        // } catch (IOException ex) {
        // System.err.println(ex.getMessage());
        // }
        // }
    }
}

class SymbolTable {
    Map<String, ST_Class> classes = new HashMap<String, ST_Class>();

    ST_Class getClass(String className) {
        if (classes.containsKey(className))
            return classes.get(className);
        return null;
    }

    void addClass(String className, ST_Class classst) {
        classes.put(className, classst);
    }
}

class ST_Class {
    ST_Class(String name, String returnT, String e) {
        className = name;
        returnType = returnT;
        classesExtends = e;
    }

    String className;
    String returnType;
    String classesExtends;
    Map<String, String> variables = new HashMap<String, String>();
    Map<String, ST_Method> methods = new HashMap<String, ST_Method>();
}

class ST_Method {
    String returnType;
    Map<String, String> arguments = new HashMap<String, String>();
    Map<String, String> bodyVariables = new HashMap<String, String>();

    String findBodyVariable(String varName) {
        if (bodyVariables.containsKey(varName))
            return bodyVariables.get(varName);
        return "";
    }

    String findArgument(String argName) {
        if (arguments.containsKey(argName))
            return arguments.get(argName);
        return "";
    }

    String getReturnType() {
        return returnType;
    }
}

class MyVisitor extends GJDepthFirst<String, Void> {
    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "{" f3 -> "public" f4 -> "static" f5
     * -> "void" f6 -> "main" f7 -> "(" f8 -> "String" f9 -> "[" f10 -> "]" f11 ->
     * Identifier() f12 -> ")" f13 -> "{" f14 -> ( VarDeclaration() )* f15 -> (
     * Statement() )* f16 -> "}" f17 -> "}"
     */
    @Override
    public String visit(MainClass n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        super.visit(n, argu);

        System.out.println();

        return null;
    }

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "{" f3 -> ( VarDeclaration() )* f4 ->
     * ( MethodDeclaration() )* f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        super.visit(n, argu);

        System.out.println();

        return null;
    }

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "extends" f3 -> Identifier() f4 -> "{"
     * f5 -> ( VarDeclaration() )* f6 -> ( MethodDeclaration() )* f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        super.visit(n, argu);

        System.out.println();

        return null;
    }

    /**
     * f0 -> "public" f1 -> Type() f2 -> Identifier() f3 -> "(" f4 -> (
     * FormalParameterList() )? f5 -> ")" f6 -> "{" f7 -> ( VarDeclaration() )* f8
     * -> ( Statement() )* f9 -> "return" f10 -> Expression() f11 -> ";" f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, Void argu) throws Exception {
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);

        System.out.println(myType + " " + myName + " -- " + argumentList);
        return null;
    }

    /**
     * f0 -> FormalParameter() f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter() f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> "," f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        String ret = "";
        for (Node node : n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type() f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception {
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }

    @Override
    public String visit(ArrayType n, Void argu) {
        return "int[]";
    }

    public String visit(BooleanType n, Void argu) {
        return "boolean";
    }

    public String visit(IntegerType n, Void argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();
    }
}
