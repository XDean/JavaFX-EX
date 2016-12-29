package xdean.jfx.ex.fxml;

import java.util.List;
import java.util.Map;

public class FxmlEntity {
  String packageName;
  String className;
  List<String> imports;
  Map<String, String> fields;// (name, class)
  List<String> methods;

  public Map<String, String> getFields() {
    return fields;
  }

  public void setFields(Map<String, String> fields) {
    this.fields = fields;
  }

  public List<String> getMethods() {
    return methods;
  }

  public void setMethods(List<String> methods) {
    this.methods = methods;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  @Override
  public String toString() {
    return "FxmlEntity [packageName=" + packageName + ", className=" + className + ", fields=" + fields + ", methods="
        + methods + "]";
  }

  public List<String> getImports() {
    return imports;
  }

  public void setImports(List<String> imports) {
    this.imports = imports;
  }

}
