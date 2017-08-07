import java.util.function.IntSupplier;

public class LambdaInlining {
  void testLambdaInline() {
    int x = ((IntSupplier) (() -> {
      if (Math.random() > 0.5) {
        return 4;
      }
      return 5;
    })).getAsInt();
    if (<warning descr="Condition 'x == 6' is always 'false'">x == 6</warning>) {
      System.out.println("oops");
    }
  }

  void testLambdaTryCatch() {
    int x = ((IntSupplier)(() -> {
      try {
        return Math.random() > 0.5 ? 2 : 3;
      }
      catch (Exception ex) {

      }
      return 1;
    })).getAsInt();
    if(<warning descr="Condition 'x == 0' is always 'false'">x == 0</warning>) {
      System.out.println("oops");
    }
  }
}