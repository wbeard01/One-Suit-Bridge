import java.util.*;

public class OptimalScore {

    private static class State {

        int currentN, origN, leaderCardsUsed, followerCardsUsed, leaderCardIndex;
        int[] leaderCards, followerCards;

        public State(int origN, int currentN, int[] leaderCards, int[] followerCards, int leaderCardsUsed, int followerCardsUsed, int leaderCardIndex) {
            this.origN = origN;
            this.currentN = currentN;
            this.leaderCardsUsed = leaderCardsUsed;
            this.followerCardsUsed = followerCardsUsed;
            this.leaderCards = leaderCards;
            this.followerCards = followerCards;
            this.leaderCardIndex = leaderCardIndex;
        }

        public State compress() {

            ArrayList<Integer> remainingCards = new ArrayList<>();
            for (int i = 0; i < currentN; i++) {
                if ((leaderCardsUsed & (1 << i)) == 0) remainingCards.add(leaderCards[i]);
                if ((followerCardsUsed & (1 << i)) == 0) remainingCards.add(followerCards[i]);
            }
            Collections.sort(remainingCards);
            HashMap<Integer, Integer> cardMap = new HashMap<>();
            for (int i = 0; i < remainingCards.size(); i++) {
                cardMap.put(remainingCards.get(i), i + 1);
            }
            int[] renumberedLeaderCards = new int[remainingCards.size() / 2];
            int[] renumberedfollowerCards = new int[remainingCards.size() / 2];
            int newLeaderCardIndex = -1;
            int leaderIndex = 0, followerIndex = 0;
            for (int i = 0; i < currentN; i++) {
                if ((leaderCardsUsed & (1 << i)) == 0) {
                    if (i == leaderIndex) {
                        newLeaderCardIndex = leaderIndex;
                    }
                    renumberedLeaderCards[leaderIndex++] = cardMap.get(leaderCards[i]);
                }
                if ((followerCardsUsed & (1 << i)) == 0) {
                    renumberedfollowerCards[followerIndex++] = cardMap.get(followerCards[i]);
                }
            }
            return new State(origN, currentN - 1, renumberedLeaderCards, renumberedfollowerCards, 0, 0, newLeaderCardIndex);
    
        }

        public State useLeaderCard(int i) {
            return new State(origN, currentN, leaderCards, followerCards, leaderCardsUsed | (1 << i), followerCardsUsed, i);
        }

        public State useFollowerCard(int i, boolean swap) {
            if (swap) {
                return new State(origN, currentN, followerCards, leaderCards, followerCardsUsed | (1 << i), leaderCardsUsed, -1).compress();
            }
            return new State(origN, currentN, leaderCards, followerCards, leaderCardsUsed, followerCardsUsed | (1 << i), -1).compress();
        }

        public String hash() {
            return leaderCardsUsed + " " + followerCardsUsed + " | " + Arrays.toString(leaderCards) + " | " + Arrays.toString(followerCards) + " | " + leaderCardIndex;
        }

    }

    public static HashMap<String, Integer> memo;

    public static void main(String[] args) {

        for (int n = 1; n <= 12; n++) {
            int[] left = new int[n];
            int[] right = new int[n];
            for (int i = 0; i < n; i++) {
                left[i] = 2 * i + 1;
                right[i] = 2 * i + 2;
            }
            System.out.println(" ---- N = " + n + " ---- ");
            State state = new State(n, n, left, right, 0, 0, 0);
            memo = new HashMap<>();
            System.out.println("Naive Result: " + naive(n, left, right, 0, 0, 0, 0, 1));
            System.out.println(memo.size());
            memo = new HashMap<>();
            System.out.println("Pruned Result: " + pruned(n, left, right, 0, 0, 0, 0, 1));
            System.out.println(memo.size());
            memo = new HashMap<>();
            System.out.println("Pruned + Compressed Result: " + prunedAndCompressed(state, 0, 1));
            System.out.println(memo.size());
        }
        /* int[] left = { 2, 3, 5, 8, 9 };
        int[] right = { 1, 4, 6, 7, 10 };
        int n = left.length; */

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

    public static int prunedAndCompressed(State state, int turn, int currentLeaderMultiplier) {
        if (turn == 2 * state.origN) {
            return 0;
        }
        String key = state.hash() + " " + currentLeaderMultiplier;
        if (!memo.containsKey(key)) {
            int bestScore;
            if (turn % 2 == 0) {
                bestScore = -state.origN * currentLeaderMultiplier;
                for (int i = 0; i < state.currentN; i++) {
                    if ((state.leaderCardsUsed & (1 << i)) == 0) {
                        int t = prunedAndCompressed(state.useLeaderCard(i), turn + 1, currentLeaderMultiplier);
                        if (t > bestScore && currentLeaderMultiplier == 1 || t < bestScore && currentLeaderMultiplier == -1) {
                            bestScore = t;
                        }
                    }
                }
            } else {
                bestScore = state.origN * currentLeaderMultiplier;
                boolean lost = false;
                boolean won = false;
                for (int i = 0; i < state.currentN; i++) {
                    if ((state.followerCardsUsed & (1 << i)) == 0) {
                        if (state.leaderCards[state.leaderCardIndex] < state.followerCards[i] && !lost) {
                            int t = prunedAndCompressed(state.useFollowerCard(i, true), turn + 1, currentLeaderMultiplier * -1) - currentLeaderMultiplier;
                            if (t < bestScore && currentLeaderMultiplier == 1 || t > bestScore && currentLeaderMultiplier == -1) {
                                bestScore = t;
                            }
                            lost = true;
                        } else if (state.leaderCards[state.leaderCardIndex] > state.followerCards[i] && !won) {
                            int t = prunedAndCompressed(state.useFollowerCard(i, false), turn + 1, currentLeaderMultiplier) + currentLeaderMultiplier;
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
