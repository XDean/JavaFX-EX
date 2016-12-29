package xdean.jfx.ex.fxml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class VelocityUtil {
  private static final VelocityEngine VELOCITY_ENGINE;
  static {
    VELOCITY_ENGINE = new VelocityEngine();
    Properties p = new Properties();
    p.setProperty( "resource.loader", "class" );
    p.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
    VELOCITY_ENGINE.init(p);
  }

  public static String toTextByVelocity(String vmFileName, String key, Object value) {
    Map<String, Object> map = new HashMap<>();
    map.put(key, value);
    return toTextByVelocity(vmFileName, map);
  }

  public static String toTextByVelocity(String vmFileName, Map<String, Object> context) {
    if (!vmFileName.endsWith(".vm")) {
      vmFileName += ".vm";
    }
    Template t = VELOCITY_ENGINE.getTemplate(vmFileName);
    VelocityContext vc = new VelocityContext(context);
    StringWriter sw = new StringWriter();
    t.merge(vc, sw);
    return sw.toString();
  }
}
