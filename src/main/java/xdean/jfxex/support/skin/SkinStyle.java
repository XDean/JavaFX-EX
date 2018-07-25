package xdean.jfxex.support.skin;

public interface SkinStyle {
  String getURL();

  String getName();

  SkinStyle EMPTY = new SkinStyle() {
    @Override
    public String getURL() {
      return "/empty.css";
    }

    @Override
    public String getName() {
      return "empty";
    }
  };
}
