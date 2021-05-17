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

            MyVisitor declarationST = new MyVisitor();
            MyVisitor typeChecking = new MyVisitor();
            root.accept(declarationST, null);
            root.accept(typeChecking, null);

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
    int state = 0; // 0 = fill table , 1 = type check

    void setState(int s) {
        this.state = s;
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

    String lookup(String className, String methName, String varName) {
        String r = "";
        ST_Class C = this.getClass(className);
        if (C != null) {
            ST_Method M = C.getMethod(methName);
            if (M != null) {
                r = M.getBodyVariable(varName);
                if (r != "")
                    return r;

                r = M.getArgument(varName);
                if (r != "")
                    return r;

                ST_Class temp = C;
                while (temp != null) {
                    r = temp.getAtribute(varName);
                    if (r != "")
                        return r;

                    temp = temp.getParent();
                }
            }
        }
        return "";
    }

    ST_Method lookupMethod(String className, String methName) {
        String r = "";
        ST_Class C = this.getClass(className);
        if (C != null) {
            ST_Method M = C.getMethod(methName);
            if (M != null)
                return M;
        }
        return null;
    }

    // String lookupAtribute(String className, String atrName) {
    // if (this.getClass(className) != null) {
    // return this.getClass(className).getAtribute(atrName);
    // }
    // System.out.println("Error in class: " + className + " -- not declared --");
    // return "error";
    // }

    // String lookupArgumentOfMethod(String className, String methName, String
    // argName) {
    // if (this.getClass(className) != null) {
    // if (this.getClass(className).getMethod(methName) != null)
    // return this.getClass(className).getMethod(methName).getArgument(argName);
    // System.out.println("Error in class method: " + className + "." + methName + "
    // -- not declared --");
    // return "error0";
    // }
    // System.out.println("Error in class: " + className + " -- not declared --");
    // return "error1";
    // }

    // String lookupBodyVariableOfMethod(String className, String methName, String
    // varName) {
    // if (this.getClass(className) != null) {
    // if (this.getClass(className).getMethod(methName) != null)
    // return this.getClass(className).getMethod(methName).getBodyVariable(varName);
    // System.out.println("Error in class method: " + className + "." + methName + "
    // -- not declared --");
    // return "error0";
    // }
    // System.out.println("Error in class: " + className + " -- not declared --");
    // return "error1";
    // }

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
        this.atributes.put("this", this.name);
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
        this.arguments.clear();
        this.bodyVariables.clear();
        this.name = n;
        this.type = t;
    }

    boolean compareArgs(LinkedList<String> args) {
        if (args.size() != this.arguments.size())
            return false;
        System.out.println("same size");
        int count = 0;
        for (String name1 : this.arguments.keySet()) {
            int i = 0;
            for (int num = 0; num < args.size(); num++) {
                if (i == count) {
                    System.out.println(
                            "name1=" + name1 + " value1=" + this.arguments.get(name1) + " name2=" + args.get(num));
                    if (!this.arguments.get(name1).equals(args.get(num))) {
                        return false;
                    } else {
                        break;
                    }
                }
                i++;
            }
            count++;
        }
        return true;
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
        ST.setState(1);
        System.out.println("Starting typechecking");
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
        if (ST.getState() == 0) {
            n.f0.accept(this, null); // "class"
            String classname = n.f1.accept(this, null);
            n.f2.accept(this, classname); // "{"
            n.f3.accept(this, classname); // "public"
            n.f4.accept(this, classname); // "static"
            n.f5.accept(this, classname); // "void"
            n.f6.accept(this, classname); // "main"

            n.f7.accept(this, classname); // "("
            n.f8.accept(this, classname); // "String"
            n.f9.accept(this, classname); // "["
            n.f10.accept(this, classname); // "]"

            if (ST.enter(classname, null) != 0)
                System.exit(1);

            String argumentName = n.f11.accept(this, classname); // argument name

            ST.insertMethod(classname, "main", "void"); // insert the main method
            ST.insertArgumentToMethod(classname, "main", argumentName, "String[]");
            // insert the argument to the main method

            n.f12.accept(this, classname); // ")"
            n.f13.accept(this, classname); // "{"
            n.f14.accept(this, classname + "->main");
            // visit VarDeclaration with className->method in order to know where this
            // variable will the be
            n.f15.accept(this, classname + "->main"); // Statements
            n.f16.accept(this, classname + "->main"); // "}"
            n.f17.accept(this, classname + "->main"); // "}"
        } else {
            n.f0.accept(this, null); // "class"
            String classname = n.f1.accept(this, null);
            n.f2.accept(this, classname); // "{"
            n.f3.accept(this, classname); // "public"
            n.f4.accept(this, classname); // "static"
            n.f5.accept(this, classname); // "void"
            n.f6.accept(this, classname); // "main"

            n.f7.accept(this, classname); // "("
            n.f8.accept(this, classname); // "String"
            n.f9.accept(this, classname); // "["
            n.f10.accept(this, classname); // "]"

            String argumentName = n.f11.accept(this, classname); // argument name

            // insert the argument to the main method

            n.f12.accept(this, classname); // ")"
            n.f13.accept(this, classname); // "{"
            n.f14.accept(this, classname + "->main");
            // visit VarDeclaration with className->method in order to know where this
            // variable will the be
            n.f15.accept(this, classname + "->main"); // Statements
            n.f16.accept(this, classname + "->main"); // "}"
            n.f17.accept(this, classname + "->main"); // "}
        }
        return null;
    }

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "{" f3 -> ( VarDeclaration() )* f4 ->
     * ( MethodDeclaration() )* f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        if (ST.getState() == 0) {
            n.f0.accept(this, null); // "class"
            String classname = n.f1.accept(this, null);
            if (ST.enter(classname, null) != 0)
                System.exit(1);

            n.f2.accept(this, classname); // "{"
            n.f3.accept(this, classname); // variables
            n.f4.accept(this, classname); // methods
            n.f5.accept(this, classname); // "}"

            ST.print();
        } else {
            n.f0.accept(this, null); // "class"
            String classname = n.f1.accept(this, null);
            n.f2.accept(this, classname); // "{"
            n.f3.accept(this, classname); // variables
            n.f4.accept(this, classname); // methods
            n.f5.accept(this, classname); // "}"
        }
        return null;
    }

    /**
     * f0 -> "class" f1 -> Identifier() f2 -> "extends" f3 -> Identifier() f4 -> "{"
     * f5 -> ( VarDeclaration() )* f6 -> ( MethodDeclaration() )* f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        if (ST.getState() == 0) {
            System.out.println("ClassExtendsDeclaration");
            n.f0.accept(this, null); // "class"
            String classname = n.f1.accept(this, null);
            n.f2.accept(this, classname); // "extends"
            String parent = n.f3.accept(this, classname);

            if (ST.enter(classname, parent) != 2)
                System.exit(1);

            n.f4.accept(this, classname); // "{"
            n.f5.accept(this, classname); // variables
            n.f6.accept(this, classname); // methods
            n.f7.accept(this, classname); // "}"

            ST.print();
        } else {
            n.f0.accept(this, null); // "class"
            String classname = n.f1.accept(this, null);
            n.f2.accept(this, classname); // "extends"
            String parent = n.f3.accept(this, classname);
            n.f4.accept(this, classname); // "{"
            n.f5.accept(this, classname); // variables
            n.f6.accept(this, classname); // methods
            n.f7.accept(this, classname); // "}"
        }
        return null;
    }

    /**
     * f0 -> Type() f1 -> Identifier() f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {
        if (ST.getState() == 0) {
            String type = n.f0.accept(this, argu); // argument name
            String name = n.f1.accept(this, argu); // argument name

            String[] scope = argu.split("->");

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
        } else {
            String type = n.f0.accept(this, argu); // variable type
            String name = n.f1.accept(this, argu); // argument name
            n.f2.accept(this, argu); // ";"
            System.out.println(type + " " + name);
            if (!type.equals("int") && !type.equals("boolean") && !type.equals("int[]")) {
                if (ST.getClass(type) == null) {
                    System.out.println(type + " does not name a type");
                    System.exit(1);
                }
            }
            System.out.println("type:" + type + " exists");
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
        if (ST.getState() == 0) {
            n.f0.accept(this, argu); // "public"
            String myType = n.f1.accept(this, argu); // method type
            String myName = n.f2.accept(this, argu); // method name

            n.f3.accept(this, argu); // "("

            // the argument list
            String argumentList = n.f4.present() ? n.f4.accept(this, argu) : "";

            n.f5.accept(this, argu); // ")"
            n.f6.accept(this, argu); // "{"

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
                if (!aName.equals("") && !aType.equals(""))
                    ST.insertArgumentToMethod(argu, myName, aName, aType);
            }

            n.f7.accept(this, argu + "->" + myName); // variables
            n.f8.accept(this, argu + "->" + myName); // statements
            n.f9.accept(this, argu + "->" + myName); // "return"
            n.f10.accept(this, argu + "->" + myName); // expresion
            n.f11.accept(this, argu + "->" + myName); // ";"
            n.f12.accept(this, argu + "->" + myName); // "}"
        } else {
            n.f0.accept(this, argu); // "public"
            String myType = n.f1.accept(this, argu); // method type

            String myName = n.f2.accept(this, argu); // method name

            if (!myType.equals("int") && !myType.equals("String") && !myType.equals("boolean")) {
                if (ST.getClass(myType) == null) {
                    System.out.println(myType + " does not name a type");
                    System.exit(1);
                }
            }
            n.f3.accept(this, argu); // "("
            String argumentList = n.f4.present() ? n.f4.accept(this, argu) : "";

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
                    if (!aType.equals("int") && !aType.equals("String") && !aType.equals("boolean")) {
                        if (ST.getClass(aType) == null) {
                            System.out.println(aType + " does not name a type");
                            System.exit(1);
                        }
                    }
            }

            n.f5.accept(this, argu + "->" + myName); // ")"
            n.f6.accept(this, argu + "->" + myName); // "{"
            n.f7.accept(this, argu + "->" + myName); // variables
            n.f8.accept(this, argu + "->" + myName); // statements
            n.f9.accept(this, argu + "->" + myName); // "return"
            n.f10.accept(this, argu + "->" + myName); // expresion
            n.f11.accept(this, argu + "->" + myName); // ";"
            n.f12.accept(this, argu + "->" + myName); // "}"
        }
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
        String name = n.f1.accept(this, null); // method name

        System.out.println(type + " " + name);
        if (!type.equals("int") && !type.equals("boolean") && !type.equals("int[]")) {
            if (ST.getClass(type) == null) {
                System.out.println(type + " does not name a type");
                System.exit(1);
            }
        }
        System.out.println("type:" + type + " exists");
        return type + " " + name;
    }

    /**
     * f0 -> ArrayType() | BooleanType() | IntegerType() | Identifier()
     */
    public String visit(Type n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> IntegerLiteral() | TrueLiteral() | FalseLiteral() | Identifier() |
     * ThisExpression() | ArrayAllocationExpression() | AllocationExpression() |
     * NotExpression() | BracketExpression()
     */
    public String visit(PrimaryExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "(" f1 -> Expression() f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        if (ST.getState() == 1) {
            n.f0.accept(this, argu);
            String a = n.f1.accept(this, argu);
            System.out.println("(" + a + ")");
            n.f2.accept(this, argu);
            return a;
        } else {
            return null;
        }
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) throws Exception {
        n.f0.accept(this, argu);
        return "int";
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception {
        n.f0.accept(this, argu);
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception {
        n.f0.accept(this, argu);
        return "boolean";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        String a = n.f0.toString();
        return a;
        // return n.f0.accept(this, argu);
    }

    // /**
    // * f0 -> "new" f1 -> "int" f2 -> "[" f3 -> Expression() f4 -> "]"
    // */
    // public R visit(ArrayAllocationExpression n, A argu) throws Exception {
    // R _ret = null;
    // n.f0.accept(this, argu);
    // n.f1.accept(this, argu);
    // n.f2.accept(this, argu);
    // n.f3.accept(this, argu);
    // n.f4.accept(this, argu);
    // return _ret;
    // }

    // /**
    // * f0 -> "new" f1 -> Identifier() f2 -> "(" f3 -> ")"
    // */
    // public R visit(AllocationExpression n, A argu) throws Exception {
    // R _ret = null;
    // n.f0.accept(this, argu);
    // n.f1.accept(this, argu);
    // n.f2.accept(this, argu);
    // n.f3.accept(this, argu);
    // return _ret;
    // }

    /**
     * f0 -> Identifier() f1 -> "=" f2 -> Expression() f3 -> ";"
     */
    public String visit(AssignmentStatement n, String argu) throws Exception {
        System.out.println("AssignmentStatement");
        if (ST.getState() == 1) {
            String varName1 = n.f0.accept(this, argu);
            String[] scope = argu.split("->");
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
            String r1 = ST.lookup(classname, methname, varName1);
            if (r1 == "") {
                System.out.println(varName1 + " has not been declared");
                System.exit(1);
            }
            System.out.println("var: " + varName1 + " has been declared with type " + r1);

            n.f1.accept(this, argu);
            String var2 = n.f2.accept(this, argu);
            String[] checkForAllocation = var2.split("\\s");

            if (checkForAllocation[0].equals("return")) {
                if (!r1.equals(checkForAllocation[1])) {
                    System.out.println("Assignment in variable " + varName1 + " bad type allocation");
                    System.exit(1);
                }
                System.out.println("Assignment NEW OK");
            } else {
                System.out.println("var2 = " + var2);
                String r2 = ST.lookup(classname, methname, var2);
                System.out.println("varName1: " + varName1 + " r1: " + r1 + " var2: " + var2 + " r2: " + r2);
                if (r1.equals("int") && !var2.equals("int") && !r2.equals("int")) {
                    System.out.println(varName1 + " is type of int , cannot be assinged with " + r2 + " " + var2);
                    System.exit(1);
                } else if (r1.equals("boolean") && !var2.equals("boolean") && !r2.equals("boolean")) {
                    System.out.println(varName1 + " is type of boolean , cannot be assinged with " + r2 + " " + var2);
                    System.exit(1);
                } else if (r2 == "" && !var2.equals("int") && !var2.equals("boolean")) {
                    System.out.println(var2 + " has not been declared");
                    System.exit(1);
                } else if (!r2.equals(r1) && !r2.equals("")) {
                    System.out.println(varName1 + " is type of " + r1 + " , cannot be assinged with an " + r2);
                    System.exit(1);
                }
                System.out.println("Assignment OK");
                n.f3.accept(this, argu);
            }

        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            n.f3.accept(this, argu);
        }
        return null;
    }

    /**
     * f0 -> Identifier() f1 -> "[" f2 -> Expression() f3 -> "]" f4 -> "=" f5 ->
     * Expression() f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        System.out.println("ArrayAssignmentStatement");
        if (ST.getState() == 1) {
            String array = n.f0.accept(this, argu);
            String[] scope = argu.split("->");
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
            String arrayType = ST.lookup(classname, methname, array);
            if (!arrayType.equals("int[]")) {
                System.out.println("Assignment in " + array + " which is not of type int[]");
                System.exit(1);
            }
            n.f1.accept(this, argu);
            String pos = n.f2.accept(this, argu);
            String posType = ST.lookup(classname, methname, pos);
            System.out.println(pos);
            if (!pos.equals("int") && !posType.equals("int")) {
                System.out.println("In array " + array + " assignment with" + pos + posType + " position");
                System.exit(1);
            }

            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            String assignment = n.f5.accept(this, argu);
            String assignmentType = ST.lookup(classname, methname, assignment);
            if (!assignmentType.equals("int") && !assignment.equals("int") && !assignmentType.equals("return int")
                    && !assignment.equals("return int")) {
                System.out.println("In array " + array + " assignment with must be assigned only int values");
                System.exit(1);
            }

            n.f6.accept(this, argu);
            return null;
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            n.f5.accept(this, argu);
            n.f6.accept(this, argu);
            return null;
        }
    }

    /**
     * f0 -> PrimaryExpression() f1 -> "[" f2 -> PrimaryExpression() f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        System.out.println("ArrayLookup");
        if (ST.getState() == 1) {
            String array = n.f0.accept(this, argu);
            String[] scope = argu.split("->");
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
            String arrayType = ST.lookup(classname, methname, array);
            if (!arrayType.equals("int[]")) {
                System.out.println("Assignment in " + array + " which is not of type int[]");
                System.exit(1);
            }
            n.f1.accept(this, argu);
            String pos = n.f2.accept(this, argu);
            String posType = ST.lookup(classname, methname, pos);
            System.out.println(pos + " " + posType);
            if (!pos.equals("int") && !posType.equals("int")) {
                System.out.println("In array " + array + " assignment with" + pos + posType + " position");
                System.exit(1);
            }
            n.f3.accept(this, argu);
            return "return int";
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            n.f3.accept(this, argu);
            return null;
        }
    }

    /**
     * f0 -> "if" f1 -> "(" f2 -> Expression() f3 -> ")" f4 -> Statement() f5 ->
     * "else" f6 -> Statement()
     */
    public String visit(IfStatement n, String argu) throws Exception {
        System.out.println("IF");
        if (ST.getState() == 1) {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            String[] scope = argu.split("->");
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
            String ifExpr = n.f2.accept(this, argu);
            String t = ST.lookup(classname, methname, ifExpr);
            System.out.println(t + " " + ifExpr);
            if (!t.equals("boolean") && !ifExpr.equals("return boolean")) {
                if (!ifExpr.equals("CompareExpression")) {
                    System.out.println(t + " " + ifExpr);
                    System.out.println("If statement must have only CompareExpression as condition");
                    System.exit(1);
                }
            }
            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            n.f5.accept(this, argu);
            n.f6.accept(this, argu);
            System.out.println("IF OK");
            return null;
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            n.f5.accept(this, argu);
            n.f6.accept(this, argu);
            return null;

        }
    }

    /**
     * f0 -> "while" f1 -> "(" f2 -> Expression() f3 -> ")" f4 -> Statement()
     */
    public String visit(WhileStatement n, String argu) throws Exception {
        if (ST.getState() == 1) {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            String[] scope = argu.split("->");
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
            String whileExpr = n.f2.accept(this, argu);
            String t = ST.lookup(classname, methname, whileExpr);
            if (!t.equals("boolean") && !whileExpr.equals("return boolean")) {
                if (!whileExpr.equals("CompareExpression")) {
                    System.out.println("While statement must have only CompareExpression as condition");
                    System.exit(1);
                }
            }
            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            System.out.println("WHILE OK");
            return null;
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            return null;

        }
    }

    /**
     * f0 -> PrimaryExpression() f1 -> "<" f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {
        System.out.println("compare");
        if (ST.getState() == 1) {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            return "CompareExpression";
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            return null;
        }
    }

    /**
     * f0 -> PrimaryExpression() f1 -> "." f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression() f1 -> "." f2 -> Identifier() f3 -> "(" f4 -> (
     * ExpressionList() )? f5 -> ")"
     */
    public String visit(MessageSend n, String argu) throws Exception {
        if (ST.getState() == 1) {
            String object = n.f0.accept(this, argu);
            String[] scope = argu.split("->");
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

            String objectType = ST.lookup(classname, methname, object);
            System.out.println(object);
            if (ST.getClass(objectType) == null) {
                String[] ob = object.split("\\s");
                if (ob[0].equals("return")) {
                    System.out.println("yeahh");
                    objectType = ob[1];
                } else {
                    System.out.println("type " + objectType + " is not a class");
                    System.exit(1);
                }
            }
            System.out.println("object " + object + "  is type of " + objectType);
            n.f1.accept(this, argu);

            String func = n.f2.accept(this, argu);
            ST_Method funcObj = ST.lookupMethod(objectType, func);
            String funcType = funcObj.getType();
            if (funcType == "") {
                System.out.println("Method " + func + " is not a member of the class " + objectType);
                System.exit(1);
            }
            System.out.println(func);
            n.f3.accept(this, argu);
            String l = n.f4.accept(this, argu);
            LinkedList<String> args = new LinkedList<String>();
            if (l != null) {
                System.out.println("list " + l);
                String[] list = l.split(" , ");
                for (String li : list) {
                    li = li.trim();
                    if (!li.equals("int") && !li.equals("int[]") && !li.equals("boolean") && !li.equals("return int")
                            && !li.equals("return int[]") && !li.equals("return boolean")) {
                        String[] liNoReturn = li.split("\\s");
                        if (liNoReturn[0].equals("return")) {
                            args.add(liNoReturn[1]);
                        } else {
                            String t = ST.lookup(classname, methname, li.trim());
                            if (t.equals("")) {
                                System.out.println("Argument " + li.trim() + " has not been declared");
                                System.exit(1);
                            }
                            args.add(t);
                        }
                    } else {
                        String[] liNoReturn = li.split("\\s");
                        if (liNoReturn[0].equals("return")) {
                            args.add(liNoReturn[1]);
                        } else {
                            args.add(li);
                        }
                    }
                }
                if (!funcObj.compareArgs(args)) {
                    System.out.println("Arguments are not the same");
                    System.exit(1);
                }
                System.out.println("true");
                n.f5.accept(this, argu);
                return "return " + funcType;
            } else {
                System.out.println("list empty");
                if (!funcObj.compareArgs(args)) {
                    System.out.println("Arguments are not the same");
                    System.exit(1);
                }
                System.out.println("true");
                n.f5.accept(this, argu);
                return "return " + funcType;
            }
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            n.f5.accept(this, argu);
            return null;

        }
    }

    /**
     * f0 -> Expression() f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String ret = n.f0.accept(this, argu);

        if (n.f1 != null)
            ret += " " + n.f1.accept(this, argu);

        return ret;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, String argu) throws Exception {
        String ret = "";
        NodeListOptional variables = n.f0;
        for (int i = 0; i < variables.size(); i++) {
            ExpressionTerm variable = (ExpressionTerm) variables.elementAt(i);
            ret += ret + ", " + variable.f1.accept(this, argu);
        }
        return ret;
    }

    /**
     * f0 -> "," f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String argu) throws Exception {
        String t = n.f0.accept(this, argu);
        return "this";
    }

    /**
     * f0 -> "!" f1 -> PrimaryExpression()
     */
    public String visit(NotExpression n, String argu) throws Exception {
        System.out.println("Not");
        if (ST.getState() == 1) {
            n.f0.accept(this, argu);
            String a = n.f1.accept(this, argu);
            if (!a.equals("CompareExpression") && !a.equals("return boolean") && !a.equals("boolean")) {
                // System.out.println("ELAA RE POYSTIIII");
                System.exit(1);
            }
            System.out.println("not " + a);
            return "return boolean";
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            return null;
        }
    }

    /**
     * f0 -> PrimaryExpression() f1 -> "+" f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        if (ST.getState() == 1) {
            System.out.println("PlusExpression");
            String l = n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            String r = n.f2.accept(this, argu);

            l = l.trim();
            r = r.trim();
            String[] scope = argu.split("->");
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
            System.out.println(l + " " + r);
            String[] le = l.split("\\s");
            String[] ri = r.split("\\s");
            if (le[0].equals("return"))
                l = le[1];
            else
                l = le[0];

            if (ri[0].equals("return"))
                r = ri[1];
            else
                r = ri[0];
            String left = l;
            String right = r;

            if (!l.equals("int") && !l.equals("int[]")) {
                left = ST.lookup(classname, methname, l);
            }
            if (!r.equals("int") && !r.equals("int[]")) {
                right = ST.lookup(classname, methname, r);
            }

            if (!left.equals("int")) {
                System.out.println("PLUS must have int");
                System.exit(1);
            }
            if (!right.equals("int")) {
                System.out.println("PLUS must have int");
                System.exit(1);
            }
            if (!left.equals(right)) {
                System.out.println("PLUS must have the same type in each side");
                System.exit(1);
            }
            return "return int";

        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            return null;
        }
        // if (ST.getState() == 1) {
        // System.out.println("PLUS");
        // String l = n.f0.accept(this, argu);
        // n.f1.accept(this, argu);
        // String r = n.f2.accept(this, argu);

        // l = l.trim();
        // r = r.trim();
        // String[] scope = argu.split("->");
        // String classname = "";
        // String methname = "";
        // int count = 1;
        // for (int i = 0; i < scope.length; i++) {
        // if (count == 1)
        // classname = scope[i];
        // else
        // methname = scope[i];

        // if (scope[i].length() != 0)
        // count = 2;
        // }
        // System.out.println(l + " " + r);
        // String left = l;
        // String right = r;
        // if (!l.equals("int") && !l.equals("int[]"))
        // left = ST.lookup(classname, methname, l);
        // if (!r.equals("int") && !r.equals("int[]"))
        // right = ST.lookup(classname, methname, r);
        // if (!left.equals("int")) {
        // System.out.println("PLUS must have int");
        // System.exit(1);
        // }
        // if (!right.equals("int")) {
        // System.out.println("PLUS must have int");
        // System.exit(1);
        // }
        // if (!left.equals(right)) {
        // System.out.println("PLUS must have the same type in each side");
        // System.exit(1);
        // }
        // return "return int";
        // } else {
        // n.f0.accept(this, argu);
        // n.f1.accept(this, argu);
        // n.f2.accept(this, argu);
        // return null;
        // }
    }

    /**
     * f0 -> PrimaryExpression() f1 -> "-" f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        if (ST.getState() == 1) {
            System.out.println("MinusExpression");
            String l = n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            String r = n.f2.accept(this, argu);

            l = l.trim();
            r = r.trim();
            String[] scope = argu.split("->");
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
            System.out.println(l + " " + r);
            String[] le = l.split("\\s");
            String[] ri = r.split("\\s");
            if (le[0].equals("return"))
                l = le[1];
            else
                l = le[0];

            if (ri[0].equals("return"))
                r = ri[1];
            else
                r = ri[0];
            String left = l;
            String right = r;

            if (!l.equals("int") && !l.equals("int[]")) {
                left = ST.lookup(classname, methname, l);
            }
            if (!r.equals("int") && !r.equals("int[]")) {
                right = ST.lookup(classname, methname, r);
            }

            if (!left.equals("int")) {
                System.out.println("PLUS must have int1");
                System.exit(1);
            }
            if (!right.equals("int")) {
                System.out.println("PLUS must have int2");
                System.exit(1);
            }
            if (!left.equals(right)) {
                System.out.println("PLUS must have the same type in each side");
                System.exit(1);
            }
            return "return int";

        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            return null;
        }
    }

    /**
     * f0 -> PrimaryExpression() f1 -> "*" f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        if (ST.getState() == 1) {
            System.out.println("TimesExpression");
            String l = n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            String r = n.f2.accept(this, argu);

            l = l.trim();
            r = r.trim();
            String[] scope = argu.split("->");
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
            System.out.println(l + " " + r);
            String[] le = l.split("\\s");
            String[] ri = r.split("\\s");
            if (le[0].equals("return"))
                l = le[1];
            else
                l = le[0];

            if (ri[0].equals("return"))
                r = ri[1];
            else
                r = ri[0];

            String left = l;
            String right = r;
            System.out.println(left + " " + right);
            if (!l.equals("int") && !l.equals("int[]")) {
                left = ST.lookup(classname, methname, l);
            }
            if (!r.equals("int") && !r.equals("int[]")) {
                right = ST.lookup(classname, methname, r);
            }

            if (!left.equals("int")) {
                System.out.println("PLUS must have int11");
                System.exit(1);
            }
            if (!right.equals("int")) {
                System.out.println("PLUS must have int22");
                System.exit(1);
            }
            if (!left.equals(right)) {
                System.out.println("PLUS must have the same type in each side");
                System.exit(1);
            }
            return "return int";

        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            return null;
        }
    }

    /**
     * f0 -> PrimaryExpression() f1 -> "&&" f2 -> PrimaryExpression()
     */
    public String visit(AndExpression n, String argu) throws Exception {
        if (ST.getState() == 1) {
            System.out.println("AND &&");
            String l = n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            String r = n.f2.accept(this, argu);

            l = l.trim();
            r = r.trim();
            String[] scope = argu.split("->");
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
            System.out.println(l + " " + r);
            String left = l;
            String right = r;
            if (!l.equals("boolean") && !l.equals("return boolean") && !l.equals("CompareExpression"))
                left = ST.lookup(classname, methname, l);
            if (!r.equals("boolean") && !r.equals("return boolean") && !r.equals("CompareExpression"))
                right = ST.lookup(classname, methname, r);
            if (!left.equals(right)) {
                System.out.println("AND must have the same type in each side");
                System.exit(1);
            }

            // System.out.println(left);
            return left;
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            return null;

        }
    }

    /**
     * f0 -> "new" f1 -> "int" f2 -> "[" f3 -> Expression() f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        if (ST.getState() == 1) {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            String num = n.f3.accept(this, argu);
            System.out.println(num);
            n.f4.accept(this, argu);
            return "return int[]";
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            return null;

        }
    }

    /**
     * f0 -> "new" f1 -> Identifier() f2 -> "(" f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception {
        if (ST.getState() == 1) {
            n.f0.accept(this, argu);
            String id = n.f1.accept(this, argu);

            String[] scope = argu.split("->");
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
            if (ST.getClass(id) == null)
                System.out.println("Allocation type " + id + " didnt found");

            n.f2.accept(this, argu);
            n.f3.accept(this, argu);
            return "return " + id;
        } else {
            n.f0.accept(this, argu);
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            n.f3.accept(this, argu);
            return null;
        }
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
}
