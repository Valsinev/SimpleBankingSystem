import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int A = scanner.nextInt();
        int B = scanner.nextInt();
        int N = scanner.nextInt();
        int K = scanner.nextInt();
        int seedMin = Integer.MAX_VALUE;
        int seed = A;
        for (int i = A; i <= B ; i++) {
            Random rn = new Random(i);
            int max = 0;
            for (int j = 0; j < N; j++) {
                int currentNumber = rn.nextInt(K);
                if (currentNumber > max) {
                    max = currentNumber;
                }
            }
            if (seedMin > max) {
                seedMin = max;
                seed = i;
            }
        }
        System.out.println(seed);
        System.out.println(seedMin);
    }
}