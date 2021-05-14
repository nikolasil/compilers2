import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(args[0]);
            MiniJavaParser parser = new MiniJavaParser(fis);

            Goal root = parser.Goal();

            System.err.println("Program parsed successfully.");

            MyVisitor eval = new MyVisitor();
            root.accept(eval, null);

        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}

class SymbolTable {
    Map<String, ST_Class> classes = new HashMap<String, ST_Class>();

    int enter(String className, String classExtend) {
        if (this.getClass(className) == null) {
            if (classExtend == null) {
                this.classes.put(className, new ST_Class(className, null));
                return 0;
            }
            if (this.getClass(classExtend) == null) {
                System.out.println(
                        "Error in class: " + className + " -- extend class: " + classExtend + " not declared --");
                return 1;
            }
            this.classes.put(className, new ST_Class(className, this.getClass(classExtend)));
            return 2;
        }
        System.out.println("Error in class: " + className + " -- double declaration --");
        return 3;
    }

    int insertAtribute(String className, String atrName, String atrType) {
        if (this.getClass(className) != null) {
            this.getClass(className).addAtribute(atrName, atrType);
            return 0;
        }
        System.out.println("Error in class: " + className + " -- not declared --");
        return 1;
    }

    int insertMethod(String className, String methName, String methType) {
        if (this.getClass(className) != null) {
            this.getClass(className).addMethod(methName, methType);
            return 0;
        }
        System.out.println("Error in class: " + className + " -- not declared --");
        return 1;
    }

    int insertArgumentToMethod(String className, String methName, String argName, String argType) {
        if (this.getClass(className) != null) {
            if (this.getClass(className).getMethod(methName) != null) {
                this.getClass(className).getMethod(methName).addArgument(argName, argType);
                return 0;
            }
            System.out.println("Error in class method: " + className + "." + methName + " -- not declared --");
            return 1;
        }
        System.out.println("Error in class: " + className + " -- not declared --");
        return 2;
    }

    int insertBodyVariableToMethod(String className, String methName, String varName, String varType) {
        if (this.getClass(className) != null) {
            if (this.getClass(className).getMethod(methName) != null) {
                this.getClass(className).getMethod(methName).addBodyVariable(varName, varType);
                return 0;
            }
            System.out.println("Error in class method: " + className + "." + methName + " -- not declared --");
            return 1;
        }
        System.out.println("Error in class: " + className + " -- not declared --");
        return 2;
    }

    String lookupAtribute(String className, String atrName) {
        if (this.getClass(className) != null) {
            return this.getClass(className).getAtribute(atrName);
        }
        System.out.println("Error in class: " + className + " -- not declared --");
        return "error";
    }

    ST_Method lookupMethod(String className, String methName) {
        if (this.getClass(className) != null) {
            return this.getClass(className).getMethod(methName);
        }
        System.out.println("Error in class: " + className + " -- not declared --");
        return null;
    }

    String lookupArgumentOfMethod(String className, String methName, String argName) {
        if (this.getClass(className) != null) {
            if (this.getClass(className).getMethod(methName) != null)
                return this.getClass(className).getMethod(methName).getArgument(argName);
            System.out.println("Error in class method: " + className + "." + methName + " -- not declared --");
            return "error0";
        }
        System.out.println("Error in class: " + className + " -- not declared --");
        return "error1";
    }

    String lookupBodyVariableOfMethod(String className, String methName, String varName) {
        if (this.getClass(className) != null) {
            if (this.getClass(className).getMethod(methName) != null)
                return this.getClass(className).getMethod(methName).getBodyVariable(varName);
            System.out.println("Error in class method: " + className + "." + methName + " -- not declared --");
            return "error0";
        }
        System.out.println("Error in class: " + className + " -- not declared --");
        return "error1";
    }

    ST_Class getClass(String className) {
        if (this.classes.containsKey(className))
            return this.classes.get(className);
        return null;
    }

    void print() {
        System.out.println("-- [START PRINTING] --");
        for (String name : this.classes.keySet()) {
            this.classes.get(name).print();
            System.out.println();
        }
        System.out.println("-- [END PRINTING] --");
    }

}

class ST_Class {
    String name;
    ST_Class extend;

    Map<String, String> atributes = new HashMap<String, String>();
    Map<String, ST_Method> methods = new HashMap<String, ST_Method>();

    ST_Class(String n, ST_Class e) {
        this.name = n;
        this.extend = e;
    }

    String getName() {
        return this.name;
    }

    ST_Class getExtend() {
        return this.extend;
    }

    String getAtribute(String name) {
        if (this.atributes.containsKey(name))
            return this.atributes.get(name);
        return "";
    }

    ST_Method getMethod(String name) {
        if (this.methods.containsKey(name))
            return this.methods.get(name);
        return null;
    }

    int addAtribute(String atrName, String atrType) {
        if (this.getAtribute(atrName) == "") {
            this.atributes.put(atrName, atrType);
            return 0;
        }
        System.out.println("Error in class atribute: " + this.name + "." + atrName + " -- double declaration --");
        return 1;
    }

    int addMethod(String methName, String methType) {
        if (this.getMethod(methName) == null) {
            this.methods.put(methName, new ST_Method(methName, methType));
            return 0;
        }
        System.out.println("Error in class method: " + this.name + "." + methName + " -- double declaration --");
        return 1;
    }

    void print() {
        if (extend != null)
            System.out.println("class " + name + " extends: " + extend.getName());
        else
            System.out.println("class " + name + " extends: none");

        System.out.println("\t -- [Attributes] --");
        if (this.atributes.size() == 0)
            System.out.println("\tnone");
        else {
            for (String name : this.atributes.keySet()) {
                System.out.println("\t" + this.atributes.get(name) + " " + name);
            }
        }
        System.out.println("\n\t -- [Methods] --");
        if (this.methods.size() == 0)
            System.out.println("\tnone");
        else {
            for (String name : this.methods.keySet()) {
                System.out.print("\t");
                this.methods.get(name).print();
            }
        }
    }
}

class ST_Method {
    String name;
    String type;

    Map<String, String> arguments = new HashMap<String, String>();
    Map<String, String> bodyVariables = new HashMap<String, String>();

    ST_Method(String n, String t) {
        this.name = n;
        this.type = t;
    }

    String getName() {
        return this.name;
    }

    String getType() {
        return this.type;
    }

    String getArgument(String argName) {
        if (this.arguments.containsKey(argName))
            return this.arguments.get(argName);
        return "";
    }

    String getBodyVariable(String varName) {
        if (this.bodyVariables.containsKey(varName))
            return this.bodyVariables.get(varName);
        return "";
    }

    int addArgument(String argName, String argType) {
        if (this.getArgument(argName) == "") {
            this.arguments.put(argName, argType);
            return 0;
        }
        System.out.println("Error in method's arguments: " + this.name + "." + argName + " -- double declaration --");
        return 1;
    }

    int addBodyVariable(String varName, String varType) {
        if (this.getBodyVariable(varName) == "") {
            this.bodyVariables.put(varName, varType);
            return 0;
        }
        System.out.println(
                "Error in method's bodyVariables : " + this.name + "." + varName + " -- double declaration --");
        return 1;
    }

    void print() {
        System.out.print(type + " " + name + "(");

        for (String name : this.arguments.keySet()) {
            System.out.print(this.arguments.get(name) + " " + name + ",");
        }
        System.out.println(")");
        System.out.print("\t\t\tbodyVariables: \n\t\t\t  ");
        for (String name : this.bodyVariables.keySet()) {
            System.out.print(this.bodyVariables.get(name) + " " + name + ",");
        }
        System.out.println("\n");
    }
}

class MyVisitor extends GJDepthFirst<String, String> {
    static SymbolTable ST = new SymbolTable();

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "{" f3 -> "public" f4 -> "static" f5
     * -> "void" f6 -> "main" f7 -> "(" f8 -> "String" f9 -> "[" f10 -> "]" f11 ->
     * Identifier() f12 -> ")" f13 -> "{" f14 -> ( VarDeclaration() )* f15 -> (
     * Statement() )* f16 -> "}" f17 -> "}"
     */
    @Override
    public String visit(MainClass n, String argu) throws Exception {
        System.out.println("[MainClass]");
        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        ST.enter(classname, null);

        String arg = n.f11.accept(this, null);
        super.visit(n, classname);

        ST.insertMethod(classname, "main", "void");
        ST.insertArgumentToMethod(classname, "main", arg, "String[]");

        n.f14.accept(this, classname + "->" + "main");

        System.out.println();

        return null;
    }

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "{" f3 -> ( VarDeclaration() )* f4 ->
     * ( MethodDeclaration() )* f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        System.out.println("[ClassDeclaration]");
        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        if (ST.enter(classname, null) != 0)
            System.exit(1);

        super.visit(n, classname);
        ST.print();
        System.out.println();

        return null;
    }

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "extends" f3 -> Identifier() f4 -> "{"
     * f5 -> ( VarDeclaration() )* f6 -> ( MethodDeclaration() )* f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        System.out.println("[ClassExtendsDeclaration]");
        String classname = n.f1.accept(this, null);
        String extend = n.f3.accept(this, null);
        System.out.println("Class: " + classname);

        if (ST.enter(classname, extend) != 2)
            System.exit(1);

        super.visit(n, classname);

        System.out.println();

        return null;
    }

    /**
     * f0 -> Type() f1 -> Identifier() f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {
        System.out.println("[VarDeclaration]");
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);
        String[] a = argu.split("->");
        for (String o : a) {
            System.out.println(o);
        }
        System.out.println("l " + a.length);
        if (a.length == 1) {
            n.f2.accept(this, argu);
            System.out.println(type + " " + name);
            ST.insertAtribute(argu, name, type);
        } else if (a.length == 2) {
            String classname = "";
            String methname = "";
            int k = 1;
            for (int i = 0; i < a.length; i++) {
                // System.out.println(a[i]);
                if (k == 1) {
                    classname = a[i];
                } else {
                    methname = a[i];
                }
                if (a[i].length() != 0)
                    k = 2;
            }
            System.out.println("here " + classname + " " + methname);
            ST.insertBodyVariableToMethod(classname, methname, name, type);
        } else {
            System.out.println("WTF???????????????????");
        }

        return null;
    }

    /**
     * f0 -> "public" f1 -> Type() f2 -> Identifier() f3 -> "(" f4 -> (
     * FormalParameterList() )? f5 -> ")" f6 -> "{" f7 -> ( VarDeclaration() )* f8
     * -> ( Statement() )* f9 -> "return" f10 -> Expression() f11 -> ";" f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, String argu) throws Exception {
        System.out.println("[MethodDeclaration]");
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);
        ST.insertMethod(argu, myName, myType);
        String[] arguments = argumentList.split(",");
        for (String arg : arguments) {
            // System.out.println("1");
            String[] a = arg.split("\\s");
            String a1 = "";
            String a2 = "";
            int k = 1;
            for (int i = 0; i < a.length; i++) {
                // System.out.println(a[i]);
                if (k == 1) {
                    a1 = a[i];
                } else {
                    a2 = a[i];
                }
                if (a[i].length() != 0)
                    k = 2;
            }
            // System.out.println(a1 + " " + a2);
            if (k == 2)
                ST.insertArgumentToMethod(argu, myName, a2, a1);
        }
        System.out.println(myType + " " + myName + " -- " + argumentList);
        System.out.println("beforenik " + myName);

        super.visit(n, argu + "->" + myName);
        System.out.println("after");
        return null;
    }

    /**
     * f0 -> FormalParameter() f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        System.out.println("[FormalParameterList]");
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter() f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        System.out.println("[FormalParameterTerm]");
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> "," f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
        System.out.println("[FormalParameterTail]");
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
    public String visit(FormalParameter n, String argu) throws Exception {
        System.out.println("[FormalParameter]");
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }

    @Override
    public String visit(ArrayType n, String argu) {
        System.out.println("[ArrayType]");
        return "int[]";
    }

    public String visit(BooleanType n, String argu) {
        System.out.println("[BooleanType]");
        return "boolean";
    }

    public String visit(IntegerType n, String argu) {
        System.out.println("[IntegerType]");
        return "int";
    }

    @Override
    public String visit(Identifier n, String argu) {
        System.out.println("[Identifier]");
        return n.f0.toString();
    }
}
