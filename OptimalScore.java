import java.util.HashMap;

public class OptimalScore {

    public static HashMap<String, Integer> memo;
    public static void main(String[] args) {

        int n = 12;
        int[] left = new int[n];
        int[] right = new int[n];
        for (int i = 1; i <= n; i++) {
            left[i - 1] = 2 * i;
            right[i - 1] = 2 * i - 1;
        }
        memo = new HashMap<>();
        System.out.println("Naive Result: " + (2 * naive(n, left, right, 0, 0, 0, 0) - n));
        memo = new HashMap<>();
        System.out.println("Pruned Result: " + (2 * pruned(n, left, right, 0, 0, 0, 0) - n));

    }

    public static int naive(int n, int[] left, int[] right, int turn, int leftUsed, int rightUsed, int leftInd) {
        if (turn == 2 * n) {
            return 0;
        }
        String key = leftUsed + " " + rightUsed + " " + leftInd;
        if (!memo.containsKey(key)) {
            int ans;
            if (turn % 2 == 0) {
                ans = -n;
                for (int i = 0; i < n; i++) {
                    if ((leftUsed & (1 << i)) == 0) {
                        int t = naive(n, left, right, turn + 1, leftUsed | (1 << i), rightUsed, i);
                        ans = Math.max(ans, t);
                    }
                }
            } else {
                ans = n;
                for (int i = 0; i < n; i++) {
                    if ((rightUsed & (1 << i)) == 0) {
                        int t = naive(n, left, right, turn + 1, leftUsed, rightUsed | (1 << i), -1);
                        if (left[leftInd] > right[i]) {
                            t++;
                        }
                        ans = Math.min(ans, t);
                    }
                }
            }
            memo.put(key, ans);
        }
        return memo.get(key);
    }

    public static int pruned(int n, int[] left, int[] right, int turn, int leftUsed, int rightUsed, int leftInd) {
        if (turn == 2 * n) {
            return 0;
        }
        String key = leftUsed + " " + rightUsed + " " + leftInd;
        if (!memo.containsKey(key)) {
            int ans;
            if (turn % 2 == 0) {
                ans = -n;
                for (int i = 0; i < n; i++) {
                    if ((leftUsed & (1 << i)) == 0) {
                        int t = pruned(n, left, right, turn + 1, leftUsed | (1 << i), rightUsed, i);
                        ans = Math.max(ans, t);
                    }
                }
            } else {
                ans = n;
                boolean lost = false;
                boolean won = false;
                for (int i = 0; i < n; i++) {
                    if ((rightUsed & (1 << i)) == 0) {
                        if (left[leftInd] < right[i] && !lost) {  
                            int t = pruned(n, left, right, turn + 1, leftUsed, rightUsed | (1 << i), -1);
                            if (left[leftInd] > right[i]) {
                                t++;
                            }
                            ans = Math.min(ans, t);
                            lost = true;
                        } else if (left[leftInd] > right[i] && !won) {
                            int t = pruned(n, left, right, turn + 1, leftUsed, rightUsed | (1 << i), -1);
                            if (left[leftInd] > right[i]) {
                                t++;
                            }
                            ans = Math.min(ans, t);
                            won = true;
                        }
                    }
                }
            }
            memo.put(key, ans);
        }
        return memo.get(key);
    }
    
}
