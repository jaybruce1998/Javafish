package Javafish;

import java.util.*;

public class Constants {

    // Piece values
    public static final Map<Character, Integer> piece = Map.of(
        'P', 100, 'N', 280, 'B', 320, 'R', 479, 'Q', 929, 'K', 60000
    );

    // Piece-square tables
    public static final Map<Character, int[]> pst = new HashMap<>();

    static {
        Map<Character, int[]> basePST = new HashMap<>();
        basePST.put('P', new int[]{
             0,  0,  0,  0,  0,  0,  0,  0,
            78, 83, 86, 73,102, 82, 85, 90,
             7, 29, 21, 44, 40, 31, 44,  7,
           -17, 16, -2, 15, 14,  0, 15,-13,
           -26,  3, 10,  9,  6,  1,  0,-23,
           -22,  9,  5,-11,-10, -2,  3,-19,
           -31,  8, -7,-37,-36,-14,  3,-31,
             0,  0,  0,  0,  0,  0,  0,  0
        });
        basePST.put('N', new int[]{
           -66,-53,-75,-75,-10,-55,-58,-70,
            -3, -6,100,-36,  4, 62, -4,-14,
            10, 67,  1, 74, 73, 27, 62, -2,
            24, 24, 45, 37, 33, 41, 25, 17,
            -1,  5, 31, 21, 22, 35,  2,  0,
           -18, 10, 13, 22, 18, 15, 11,-14,
           -23,-15,  2,  0,  2,  0,-23,-20,
           -74,-23,-26,-24,-19,-35,-22,-69
        });
            basePST.put('B', new int[]{
       -59, -78, -82, -76, -23,-107, -37, -50,
       -11,  20,  35, -42, -39,  31,   2, -22,
        -9,  39, -32,  41,  52, -10,  28, -14,
        25,  17,  20,  34,  26,  25,  15,  10,
        13,  10,  17,  23,  17,  16,   0,   7,
        14,  25,  24,  15,   8,  25,  20,  15,
        19,  20,  11,   6,   7,   6,  20,  16,
        -7,   2, -15, -12, -14, -15, -10, -10
    });
    basePST.put('R', new int[]{
        35,  29,  33,   4,  37,  33,  56,  50,
        55,  29,  56,  67,  55,  62,  34,  60,
        19,  35,  28,  33,  45,  27,  25,  15,
         0,   5,  16,  13,  18,  -4,  -9,  -6,
       -28, -35, -16, -21, -13, -29, -46, -30,
       -42, -28, -42, -25, -25, -35, -26, -46,
       -53, -38, -31, -26, -29, -43, -44, -53,
       -30, -24, -18,   5,  -2, -18, -31, -32
    });
    basePST.put('Q', new int[]{
         6,   1,  -8,-104,  69,  24,  88,  26,
        14,  32,  60, -10,  20,  76,  57,  24,
        -2,  43,  32,  60,  72,  63,  43,   2,
         1, -16,  22,  17,  25,  20, -13,  -6,
       -14, -15,  -2,  -5,  -1, -10, -20, -22,
       -30,  -6, -13, -11, -16, -11, -16, -27,
       -36, -18,   0, -19, -15, -15, -21, -38,
       -39, -30, -31, -13, -31, -36, -34, -42
    });
    basePST.put('K', new int[]{
         4,  54,  47, -99, -99,  60,  83, -62,
       -32,  10,  55,  56,  56,  55,  10,   3,
       -62,  12, -57,  44, -67,  28,  37, -31,
       -55,  50,  11,  -4, -19,  13,   0, -49,
       -55, -43, -52, -28, -51, -47,  -8, -50,
       -47, -42, -43, -79, -64, -32, -29, -32,
        -4,   3, -14, -50, -57, -18,  13,   4,
        17,  30,  -3, -14,   6,  -1,  40,  18
    });

        for (var entry : basePST.entrySet()) {
            char k = entry.getKey();
            int[] table = entry.getValue();
            int[] padded = new int[120];  // 20 + 8 rows of 10 + 20
            int[] row = new int[10];
            for (int i = 0; i < 8; i++) {
                row[0] = 0;
                for (int j = 0; j < 8; j++) {
                    row[j + 1] = table[i * 8 + j] + piece.get(k);
                }
                row[9] = 0;
                System.arraycopy(row, 0, padded, 20 + i * 10, 10);
            }
            pst.put(k, padded);
        }
    }

    // Board constants
    public static final int A1 = 91, H1 = 98, A8 = 21, H8 = 28;

    public static final String initial =
        "         \n" + // 0 - 9
        "         \n" + // 10 - 19
        " rnbqkbnr\n" + // 20 - 29
        " pppppppp\n" + // 30 - 39
        " ........\n" + // 40 - 49
        " ........\n" + // 50 - 59
        " ........\n" + // 60 - 69
        " ........\n" + // 70 - 79
        " PPPPPPPP\n" + // 80 - 89
        " RNBQKBNR\n" + // 90 - 99
        "         \n" + // 100 - 109
        "         \n";  // 110 - 119

    // Directions
    public static final int N = -10, E = 1, S = 10, W = -1;
    public static final Map<Character, int[]> directions = Map.of(
        'P', new int[]{N, N+N, N+W, N+E},
        'N', new int[]{N+N+E, E+N+E, E+S+E, S+S+E, S+S+W, W+S+W, W+N+W, N+N+W},
        'B', new int[]{N+E, S+E, S+W, N+W},
        'R', new int[]{N, E, S, W},
        'Q', new int[]{N, E, S, W, N+E, S+E, S+W, N+W},
        'K', new int[]{N, E, S, W, N+E, S+E, S+W, N+W}
    );
	
	public static final int MATE_LOWER = 60000-9290;
	public static final int MATE_UPPER = 60000+9290;
	public static final int QS = 40;
	public static final int QS_A = 140;
	public static final int EVAL_ROUGHNESS = 15;
}
