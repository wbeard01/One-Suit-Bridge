import java.util.*;

public class OptimalScore {

    public static HashMap<String, Integer> memo;

    public static void main(String[] args) {

        // Note: the pruned approach requires left / right to be in sorted order
        int[] left = { 2,4,6,8,10,12 };
        int[] right = { 1,3,5,7,9,11 };
        int n = left.length;
        memo = new HashMap<>();
        System.out.println("Naive Result: " + naive(n, left, right, 0, 0, 0, 0, 1));
        memo = new HashMap<>();
        System.out.println("Pruned Result: " + pruned(n, left, right, 0, 0, 0, 0, 1));

    }

    public static int naive(int n, int[] leaderCards, int[] followerCards, int turn, int leaderCardsUsed,
            int followerCardsUsed, int leaderCardIndex, int currentLeaderMultiplier) {
        if (turn == 2 * n) {
            return 0;
        }
        String key = leaderCardsUsed + " " + followerCardsUsed + " " + leaderCardIndex + " " + currentLeaderMultiplier;
        if (!memo.containsKey(key)) {
            int bestScore;
            if (turn % 2 == 0) {
                bestScore = -n * currentLeaderMultiplier;
                for (int i = 0; i < n; i++) {
                    if ((leaderCardsUsed & (1 << i)) == 0) {
                        int t = naive(n, leaderCards, followerCards, turn + 1, leaderCardsUsed | (1 << i),
                                followerCardsUsed, i, currentLeaderMultiplier);
                        if (t > bestScore && currentLeaderMultiplier == 1 || t < bestScore && currentLeaderMultiplier == -1) {
                            bestScore = t;
                        }
                    }
                }
            } else {
                bestScore = n * currentLeaderMultiplier;
                for (int i = 0; i < n; i++) {
                    if ((followerCardsUsed & (1 << i)) == 0) {
                        int t;
                        if (leaderCards[leaderCardIndex] > followerCards[i]) {
                            t = naive(n, leaderCards, followerCards, turn + 1, leaderCardsUsed,
                                    followerCardsUsed | (1 << i), -1, currentLeaderMultiplier)
                                    + currentLeaderMultiplier;
                        } else {
                            t = naive(n, followerCards, leaderCards, turn + 1, followerCardsUsed | (1 << i),
                                    leaderCardsUsed, -1, currentLeaderMultiplier * -1) - currentLeaderMultiplier;

                        }
                        if (t < bestScore && currentLeaderMultiplier == 1 || t > bestScore && currentLeaderMultiplier == -1) {
                            bestScore = t;
                        }
                    }
                }
            }
            memo.put(key, bestScore);
        }
        return memo.get(key);
    }

    public static int pruned(int n, int[] leaderCards, int[] followerCards, int turn, int leaderCardsUsed,
            int followerCardsUsed, int leaderCardIndex, int currentLeaderMultiplier) {
        if (turn == 2 * n) {
            return 0;
        }
        String key = leaderCardsUsed + " " + followerCardsUsed + " " + leaderCardIndex + " " + currentLeaderMultiplier;
        if (!memo.containsKey(key)) {
            int bestScore;
            if (turn % 2 == 0) {
                bestScore = -n * currentLeaderMultiplier;
                for (int i = 0; i < n; i++) {
                    if ((leaderCardsUsed & (1 << i)) == 0) {
                        int t = pruned(n, leaderCards, followerCards, turn + 1, leaderCardsUsed | (1 << i),
                                followerCardsUsed, i, currentLeaderMultiplier);
                        if (t > bestScore && currentLeaderMultiplier == 1 || t < bestScore && currentLeaderMultiplier == -1) {
                            bestScore = t;
                        }
                    }
                }
            } else {
                bestScore = n * currentLeaderMultiplier;
                boolean lost = false;
                boolean won = false;
                for (int i = 0; i < n; i++) {
                    if ((followerCardsUsed & (1 << i)) == 0) {
                        if (leaderCards[leaderCardIndex] < followerCards[i] && !lost) {
                            int t = pruned(n, followerCards, leaderCards, turn + 1, followerCardsUsed | (1 << i),
                                    leaderCardsUsed, -1, currentLeaderMultiplier * -1) - currentLeaderMultiplier;
                            if (t < bestScore && currentLeaderMultiplier == 1 || t > bestScore && currentLeaderMultiplier == -1) {
                                bestScore = t;
                            }
                            bestScore = Math.min(bestScore, t);
                            lost = true;
                        } else if (leaderCards[leaderCardIndex] > followerCards[i] && !won) {
                            int t = pruned(n, leaderCards, followerCards, turn + 1, leaderCardsUsed,
                                    followerCardsUsed | (1 << i), -1, currentLeaderMultiplier)
                                    + currentLeaderMultiplier;
                            if (t < bestScore && currentLeaderMultiplier == 1 || t > bestScore && currentLeaderMultiplier == -1) {
                                bestScore = t;
                            }
                            won = true;
                        }
                    }
                }
            }
            memo.put(key, bestScore);
        }
        return memo.get(key);
    }

}
