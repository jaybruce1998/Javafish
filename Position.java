package Javafish;

import static Javafish.Constants.*;
import java.util.*;

/**
 * @param bc [west, east] castling rights
 */
public record Position(String board, int score, boolean[] wc, boolean[] bc, int ep, int kp) {

    public List<Move> genMoves() {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < board.length(); i++) {
            char p = board.charAt(i);
            if (!Character.isUpperCase(p)) continue;
            for (int d : directions.get(p)) {
                for (int j = i + d; j >= 0 && j < 120; j += d) {
                    char q = board.charAt(j);
                    if (Character.isWhitespace(q) || Character.isUpperCase(q)) break;

                    // Pawn special rules
                    if (p == 'P') {
                        if ((d == N || d == N + N) && q != '.') break;
                        if (d == N + N && (i < A1 + N || board.charAt(i + N) != '.')) break;
                        if ((d == N + W || d == N + E) && q == '.' &&
                                j != ep && j != kp && j != kp - 1 && j != kp + 1) break;

                        if (j >= A8 && j <= H8) {
                            for (char prom : new char[]{'N', 'B', 'R', 'Q'}) {
                                moves.add(new Move(i, j, prom));
                            }
                            break;
                        }
                    }

                    moves.add(new Move(i, j, '\0'));

                    if ("PNK".indexOf(p) != -1 || Character.isLowerCase(q)) break;

                    if (i == A1 && board.charAt(j + E) == 'K' && wc[0]) {
                        moves.add(new Move(j + E, j + W, '\0'));
                    }
                    if (i == H1 && board.charAt(j + W) == 'K' && wc[1]) {
                        moves.add(new Move(j + W, j + E, '\0'));
                    }
                }
            }
        }
        return moves;
    }

    public Position rotate(boolean nullmove) {
        StringBuilder newBoard = new StringBuilder();
        for (int i = board.length() - 1; i >= 0; i--) {
            char c = board.charAt(i);
            newBoard.append(Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c));
        }
        return new Position(
                newBoard.toString(),
                -score,
                bc, wc,
                (ep != 0 && !nullmove) ? 119 - ep : 0,
                (kp != 0 && !nullmove) ? 119 - kp : 0
        );
    }

    public Position move(Move move) {
        int i = move.from(), j = move.to();
        char prom = move.promotion();
        char p = board.charAt(i);

        boolean[] newWc = Arrays.copyOf(wc, 2);
        boolean[] newBc = Arrays.copyOf(bc, 2);
        int newEp = 0, newKp = 0;
        int newScore = score + value(move);

        StringBuilder newBoard = new StringBuilder(board);
        newBoard.setCharAt(j, board.charAt(i));
        newBoard.setCharAt(i, '.');

        if (i == A1) newWc[0] = false;
        if (i == H1) newWc[1] = false;
        if (j == A8) newBc[0] = false;
        if (j == H8) newBc[1] = false;

        if (p == 'K') {
            newWc[0] = newWc[1] = false;
            if (Math.abs(j - i) == 2) {
                newKp = (i + j) / 2;
                newBoard.setCharAt(j < i ? A1 : H1, '.');
                newBoard.setCharAt(newKp, 'R');
            }
        }

        if (p == 'P') {
            if (j >= A8 && j <= H8) {
                newBoard.setCharAt(j, prom);
            }
            if (j - i == 2 * N) {
                newEp = i + N;
            }
            if (j == ep) {
                newBoard.setCharAt(j + S, '.');
            }
        }

        return new Position(newBoard.toString(), newScore, newWc, newBc, newEp, newKp).rotate(false);
    }

    public int value(Move move) {
        int i = move.from(), j = move.to();
        char p = board.charAt(i), q = board.charAt(j);
        char prom = move.promotion();

        int val = pst.get(p)[j] - pst.get(p)[i];
        if (Character.isLowerCase(q)) {
            val += pst.get(Character.toUpperCase(q))[119 - j];
        }
        if (Math.abs(j - kp) < 2) {
            val += pst.get('K')[119 - j];
        }
        if (p == 'K' && Math.abs(i - j) == 2) {
            val += pst.get('R')[(i + j) / 2] - pst.get('R')[j < i ? A1 : H1];
        }
        if (p == 'P') {
            if (j >= A8 && j <= H8) {
                val += pst.get(prom)[j] - pst.get('P')[j];
            }
            if (j == ep) {
                val += pst.get('P')[119 - (j + S)];
            }
        }
        return val;
    }
}
