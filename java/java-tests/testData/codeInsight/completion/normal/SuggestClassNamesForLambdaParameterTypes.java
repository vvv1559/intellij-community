package abcdef;

import java.util.List;

public class Foo {
    void foo() {
        List<String> strings = null;
        strings.stream().anyMatch((ChSeq<caret>x) -> x.charAt(0) == 'a');
    }
}
