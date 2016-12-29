# JavaFX Extension #
---
### Bean ###

#### BeanConvertUtil ####

JavaFX为基本类型和`String`包装了对应的`Property`。但当我们遇到诸如`Property<Integer>`时，其与
`IntegerProperty(Property<Number>)`无法相互转换。`BeanConvertUtil`包装了`BidirectionalBinding`并拓展了相应的`Binding`转换，方便将`Property`转换为对应的包装`Property`，将`ObservableValue`转换为对应的包装`Binding`。如：

    IntegerBinding toIntegerBinding(ObservableValue<? extends Number>)
	IntegerProperty toInteger(Property<Integer>)

#### BeanUtil ####

**MapProperty**

一个常见的情况是，当属性变换时需要对其进行取值和计算，计算结果再通知其他属性。遗憾的是JavaFX的`Bindings`工具类仅提供了常用的基本包装，`BeanUtil`下的`map`相关方法提供了通用的解决方案：

*`MapableValue<T>`是对`map`方法封装后的`ObservableValue`对象，方便`map`的链式调用*

	<F, T> MapableValue<T> map(ObservableValue<F>, Function<F, T>)

同时提供了`ObservableList`的map实现（`Set`和`Map`暂未实现）：

	<F, T> ObservableList<T> map(ObservableList<F>, Function<F, T>, Function<T, F>)

通过`map`方法，我们可以将两个不同类型的`List`绑定到一起：

	<F, T> void bind(ObservableList<F>, ObservableList<T>, Function<F, T>, Function<T, F>)

**NestProperty**

另一个常见的情况是，“属性”的属性仍然是“属性”，当我们关心一个`Property`的值的一个`Property`时，嵌套的依赖关系显得十分棘手，`BeanUtil`的`nestProp`和`nestValue`解决了这个问题：

	<F, T> Property<T> nestProp(ObservableValue<F>, Function<F, Property<T>>)
	<F, T> ObservableValue<T> nestValue(ObservableValue<F>, Function<F, ObservableValue<T>>)

另外，`nestWrap`方法正在研究中，希望通过动态代理来实现返回一个`F`本身，而对F的所有getXXX, setXXX, XXXProperty方法都可以动态的调回`nestProp`以及`nestValue`方法。当前仅支持标准的`Property`返回，任何返回`Property`的未知子类的xxxProperty方法将会导致`ClassCastException`。

	<T> T nestWrap(ObservableValue<T>, Class<T>)

### Layout ###
提供一些链式调用方法，实质上可以由[XDean/Java-EX](https://github.com/XDean/Java-EX "JavaEX")提供的`TaskUtil`替代。

### Support ###

#### DragSupport & ResizeSupport ####

快捷支持拖拽组件以及改变大小，直接调用`bind`和`unbind`方法即可。

#### Skin ####

统一管理应用风格，通过`bind`和`unbind`绑定到`SkinManager`，`changeSkin`更换风格，`getSkinList`获取支持的风格列表。

#### UndoRedo ####

通用的撤销重做支持。通过向`UndoRedoSupport`中添加`Undoable`来记录操作。任何实现`Undoable`的对象皆可以作为一个操作单元，`Undoable`提供了数个工厂方法以方便创建。同时`UndoRedoSupport`也提供了诸多`bind`方法以快捷的将JavaFX组件绑定到其上。推荐使用`UndoUtil`中的`weak`方法以防止内存泄漏。

### FXML2Controller ###
在定义完FXML文件后，第一步就是创建对应的`Controller`类。`FXML2Controller`自动从FXML文件提取controller、id、action信息创建对应java文件，以防止漏写、错写，提高速度。目前由于代码未加工，需要手动修改代码来指定FXML文件。