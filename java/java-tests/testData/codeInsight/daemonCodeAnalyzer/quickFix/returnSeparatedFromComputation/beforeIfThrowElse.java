// "Move 'return' closer to computation of the value of 'n'" "true"
class T {
    int f(boolean b) {
        int n = -1;
        if (b) {
            throw new RuntimeException();
        }
        else {
            n = 2;
        }
        r<caret>eturn n;
    }
}