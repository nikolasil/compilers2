# NIKOLAOS ILIOPOULOS
# 1115201800332

## SymbolTable Structure

```javascript
class SymbolTable {
    Map<String, ST_Class> classes; // holds all the symbol tables
}

class ST_Class {
    String name;
    ST_Class parent; // points to the parent else null
    ST_Class child; // points to the child else null

    Map<String, String> atributes;
    Map<String, ST_Method> methods;
}

class ST_Method {
    String name;
    String type;

    Map<String, String> arguments;
    Map<String, String> bodyVariables;
}
```
