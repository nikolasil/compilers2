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
            // MyVisitor eval2 = new MyVisitor(true);
            root.accept(eval, null);
            // root.accept(eval2, null);

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
    Map<String, ST_Class> classes = new LinkedHashMap<String, ST_Class>();
    int state = 0;

    void makeState1() {
        this.state = 1;
    }

    void makeState0() {
        this.state = 0;
    }

    int getState() {
        return this.state;
    }

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
    ST_Class parent;
    ST_Class child;

    Map<String, String> atributes = new LinkedHashMap<String, String>();
    Map<String, ST_Method> methods = new LinkedHashMap<String, ST_Method>();

    ST_Class(String n, ST_Class p) {
        this.name = n;
        this.parent = p;
        this.child = null;
        if (parent != null)
            parent.setChild(this);
    }

    void setChild(ST_Class c) {
        this.child = c;
    }

    String getName() {
        return this.name;
    }

    ST_Class getChild() {
        return this.child;
    }

    ST_Class getParent() {
        return this.parent;
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
        if (parent != null)
            System.out.print("class " + name + " parent: " + parent.getName());
        else
            System.out.print("class " + name + " parent: none");
        if (child != null)
            System.out.println(" child: " + child.getName());
        else
            System.out.println(" child: none");

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

    Map<String, String> arguments = new LinkedHashMap<String, String>();
    Map<String, String> bodyVariables = new LinkedHashMap<String, String>();

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
     * f0 -> MainClass() f1 -> ( TypeDeclaration() )* f2 -> <EOF>
     */
    public String visit(Goal n, String argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        ST.makeState1();
        return null;
    }

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "{" f3 -> "public" f4 -> "static" f5
     * -> "void" f6 -> "main" f7 -> "(" f8 -> "String" f9 -> "[" f10 -> "]" f11 ->
     * Identifier() f12 -> ")" f13 -> "{" f14 -> ( VarDeclaration() )* f15 -> (
     * Statement() )* f16 -> "}" f17 -> "}"
     */
    @Override
    public String visit(MainClass n, String argu) throws Exception {
        n.f0.accept(this, null); // "class"
        String classname = n.f1.accept(this, null); // classname
        n.f2.accept(this, null); // "{"
        n.f3.accept(this, null); // "public"
        n.f4.accept(this, null); // "static"
        n.f5.accept(this, null); // "void"
        n.f6.accept(this, null); // "main"

        n.f7.accept(this, null); // "("
        n.f8.accept(this, null); // "String"
        n.f9.accept(this, null); // "["
        n.f10.accept(this, null); // "]"

        if (ST.enter(classname, null) != 0)
            System.exit(1);

        String argumentName = n.f11.accept(this, null); // argument name

        ST.insertMethod(classname, "main", "void"); // insert the main method
        ST.insertArgumentToMethod(classname, "main", argumentName, "String[]");
        // insert the argument to the main method

        n.f12.accept(this, null); // ")"
        n.f13.accept(this, null); // "{"
        n.f14.accept(this, classname + "->main");
        // visit VarDeclaration with className->method in order to know where this
        // variable will the be
        n.f15.accept(this, null); // Statements
        n.f16.accept(this, null); // "}"
        n.f17.accept(this, null); // "}"
        return null;
    }

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "{" f3 -> ( VarDeclaration() )* f4 ->
     * ( MethodDeclaration() )* f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        n.f0.accept(this, null); // "class"
        String classname = n.f1.accept(this, null);

        if (ST.enter(classname, null) != 0)
            System.exit(1);

        n.f2.accept(this, null); // "{"
        n.f3.accept(this, classname); // variables
        n.f4.accept(this, classname); // methods
        n.f5.accept(this, null); // "}"

        ST.print();
        return null;
    }

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "extends" f3 -> Identifier() f4 -> "{"
     * f5 -> ( VarDeclaration() )* f6 -> ( MethodDeclaration() )* f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        n.f0.accept(this, null); // "class"
        String classname = n.f1.accept(this, null);
        n.f2.accept(this, null); // "extends"
        String parent = n.f3.accept(this, null);

        if (ST.enter(classname, parent) != 2)
            System.exit(1);

        n.f4.accept(this, null); // "{"
        n.f5.accept(this, classname); // variables
        n.f6.accept(this, classname); // methods
        n.f7.accept(this, null); // "}"

        ST.print();
        return null;
    }

    /**
     * f0 -> Type() f1 -> Identifier() f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {
        String type = n.f0.accept(this, argu); // variable type
        String name = n.f1.accept(this, argu); // variable name

        String[] scope = argu.split("->");
        System.out.println(argu);
        for (String s : scope) {
            System.out.println(s);
        }

        if (scope.length == 1) {
            // the variables will be in a class
            System.out.println("In class with name [" + argu + "] there is a variable: " + type + " " + name);
            ST.insertAtribute(argu, name, type);
        } else if (scope.length == 2) {
            // the variables will be in a methods class
            String classname = "";
            String methname = "";
            int count = 1;
            for (int i = 0; i < scope.length; i++) {
                if (count == 1)
                    classname = scope[i];
                else
                    methname = scope[i];

                if (scope[i].length() != 0)
                    count = 2;
            }
            System.out.println("In class with name [" + classname + "] in method [" + methname
                    + "] there is a variable: " + type + " " + name);
            ST.insertBodyVariableToMethod(classname, methname, name, type);
        }

        n.f2.accept(this, argu); // ";"
        return null;
    }

    /**
     * f0 -> "public" f1 -> Type() f2 -> Identifier() f3 -> "(" f4 -> (
     * FormalParameterList() )? f5 -> ")" f6 -> "{" f7 -> ( VarDeclaration() )* f8
     * -> ( Statement() )* f9 -> "return" f10 -> Expression() f11 -> ";" f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, String argu) throws Exception {
        n.f0.accept(this, null); // "public"
        String myType = n.f1.accept(this, null); // method type
        String myName = n.f2.accept(this, null); // method name
        n.f3.accept(this, null); // "("

        // the argument list
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

        n.f5.accept(this, null); // ")"
        n.f6.accept(this, null); // "{"

        ST.insertMethod(argu, myName, myType);

        String[] arguments = argumentList.split(",");
        for (String arg : arguments) {
            String[] a = arg.split("\\s");
            String aType = "";
            String aName = "";
            int count = 1;
            for (int i = 0; i < a.length; i++) {
                if (count == 1)
                    aType = a[i];
                else
                    aName = a[i];

                if (a[i].length() != 0)
                    count = 2;
            }
            if (count == 2)
                ST.insertArgumentToMethod(argu, myName, aName, aType);
        }

        n.f7.accept(this, argu + "->" + myName); // variables
        n.f8.accept(this, argu + "->" + myName); // statements
        n.f9.accept(this, null); // "return"
        n.f10.accept(this, argu + "->" + myName); // expresion
        n.f11.accept(this, null); // ";"
        n.f12.accept(this, null); // "}"
        return null;
    }

    /**
     * f0 -> FormalParameter() f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null)
            ret += n.f1.accept(this, null);

        return ret;
    }

    /**
     * f0 -> FormalParameter() f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> "," f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for (Node node : n.f0.nodes)
            ret += ", " + node.accept(this, null);

        return ret;
    }

    /**
     * f0 -> Type() f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, String argu) throws Exception {
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }

    @Override
    public String visit(ArrayType n, String argu) {
        return "int[]";
    }

    public String visit(BooleanType n, String argu) {
        return "boolean";
    }

    public String visit(IntegerType n, String argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.toString();
    }
}
