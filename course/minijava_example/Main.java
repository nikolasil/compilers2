import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
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
                this.getClass(className).getMethod(methName).addArgument(varName, varType);
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
        if (this.getAtribute(atrName) == null) {
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
        if (this.getArgument(argName) == null) {
            this.arguments.put(argName, argType);
            return 0;
        }
        System.out.println("Error in method's arguments: " + this.name + "." + argName + " -- double declaration --");
        return 1;
    }

    int addBodyVariable(String varName, String varType) {
        if (this.getBodyVariable(varName) == null) {
            this.bodyVariables.put(varName, varType);
            return 0;
        }
        System.out.println(
                "Error in method's bodyVariables : " + this.name + "." + varName + " -- double declaration --");
        return 1;
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
