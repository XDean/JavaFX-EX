package xdean.jfx.ex.util.bean;

import static xdean.jfx.ex.util.bean.CollectionUtil.fixPosition;
import io.reactivex.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Test;

public class TestCollectionUtil {

  /******* Fix position ******/
  @Test
  public void testNormal() {
    ObservableList<Integer> list = FXCollections.observableArrayList();
    Observable<Integer> ob = Observable.fromIterable(list);
    fixPosition(list, 1, 0);
    ob.test().assertValues(1);
    fixPosition(list, 2, 1);
    ob.test().assertValues(1, 2);
    fixPosition(list, -1, -1);
    ob.test().assertValues(1, 2, -1);
    list.add(2);
    ob.test().assertValues(1, 2, -1);
    list.add(3);
    ob.test().assertValues(1, 2, 3, -1);
    list.remove(1);
    ob.test().assertValues(1, 2, 3, -1);
    list.add(0, 5);
    ob.test().assertValues(1, 2, 5, 3, -1);
    fixPosition(list, -2, -2);
    list.remove(1);
    ob.test().assertValues(1, 2, 5, 3, -2, -1);
    list.remove(3);
    ob.test().assertValues(1, 2, 5, -2, -1);
    fixPosition(list, 10, 0);
    ob.test().assertValues(10, 2, 5, -2, -1);
  }
}