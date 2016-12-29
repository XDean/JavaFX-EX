package xdean.jfx.ex.fxml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import xdean.jex.util.string.StringUtil;

public class Fxml2Controller {

  public static void main(String[] args) throws IOException, URISyntaxException {
    Path path = Paths.get(Fxml2Controller.class.getResource("/Options.fxml").toURI());
    FxmlEntity fxml = convert(path);
    System.out.println(fxml);
    String text = VelocityUtil.toTextByVelocity("xdean/jfx/ex/fxml/Controller.vm", "fxml", fxml);
    Path javaFile = path.getParent().resolve(fxml.getClassName() + ".java");
    System.out.println(javaFile);
    Files.write(javaFile, text.getBytes());
  }

  // private static Pattern importPattern = Pattern.compile("<?import (.*?)?>");
  // private static Pattern classPattern =
  // Pattern.compile("fx:controller\\s*=\\s*\"(.*?)\"");
  // private static Pattern idPattern =
  // Pattern.compile("fx:id\\s*=\\s*\"(.*?)\"");
  // private static Pattern actionPattern =
  // Pattern.compile("(on.*?)\\s*=\\s*\"#(.*?)\"");

  public static FxmlEntity convert(Path path) throws IOException {
    FxmlEntity fxml = new FxmlEntity();
    fxml.setFields(new HashMap<>());
    fxml.setImports(new ArrayList<>());
    fxml.setMethods(new ArrayList<>());

    List<String> lines = Files.readAllLines(path);
    String text = String.join("\n", lines.toArray(new String[lines.size()]));
    while (true) {
      int[] pair = StringUtil.balancePair(text, "<", ">");
      if (pair[0] == -1 || pair[1] == -1) {
        break;
      }
      String now = text.substring(pair[0] + 1, pair[1]);
      text = text.substring(pair[1] + 1);

      if (now.startsWith("?import")) {
        fxml.imports.add(now.substring(7, now.length() - 1));
      } else {
        String[] split = now.split(" ");
        String name = split[0];
        Map<String, String> map = new HashMap<>();
        Stream.of(split)
            .skip(1)
            .map(s -> s.split("=", 2))
            .filter(ss -> ss.length >= 2)
            .forEach(ss -> map.put(ss[0].trim(), StringUtil.unWrap(ss[1], "\"", "\"")));
        map.forEach((k, v) -> {
          if ("fx:controller".equals(k)) {
            int index = v.lastIndexOf('.');
            fxml.setPackageName(v.substring(0, index));
            fxml.setClassName(v.substring(index + 1));
          } else if ("fx:id".equals(k)) {
            fxml.fields.put(v, name);
          } else if (k.startsWith("on") && v.startsWith("#")) {
            fxml.methods.add(v.substring(1));
          }
        });
      }
    }

    return fxml;
  }
}
