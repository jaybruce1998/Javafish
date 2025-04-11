package Javafish;

import java.util.*;
import static Javafish.Constants.A1;

public class UCI {

    static int parse(String c) {
        int file = c.charAt(0) - 'a';
        int rank = c.charAt(1) - '1';
        return A1 + file - 10 * rank;
    }

    static String render(int i) {
		return ""+(char)('a'+i%10-1)+(10-i/10);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Position> hist = new ArrayList<>();
		Position pos=new Position(Constants.initial, 0, new boolean[]{true, true}, new boolean[]{true, true}, 0, 0);
        hist.add(pos);
        Searcher searcher = new Searcher();

        while (true) {
            String line = in.nextLine();
            String[] tokens = line.split("\\s+");

            if (tokens[0].equals("uci")) {
                System.out.println("id name JavaFish");
                System.out.println("uciok");

            } else if (tokens[0].equals("isready")) {
                System.out.println("readyok");

            } else if (tokens[0].equals("quit")) {
                break;

            } else if (tokens[0].equals("position") && tokens[1].equals("startpos")) {
                hist.subList(1, hist.size()).clear();
                for (int ply = 0; ply < tokens.length - 3; ply++) {
                    String moveStr = tokens[3 + ply];
                    int i = parse(moveStr.substring(0, 2));
                    int j = parse(moveStr.substring(2, 4));
                    char prom = moveStr.length() > 4 ? Character.toUpperCase(moveStr.charAt(4)) : 0;
                    if (ply % 2 == 1) {
                        i = 119 - i;
                        j = 119 - j;
                    }
                    Position last = hist.getLast();
                    hist.add(last.move(new Move(i, j, prom)));
                }
            } else if (tokens[0].equals("go")) {
                double thinkTime = getThinkTime(tokens, hist);
                long startTime = System.currentTimeMillis();
                String moveStr = null;
				char c;
				DepthScore result=null;
                for (Iterator<DepthScore> it=searcher.search(hist); it.hasNext();) {
					result=it.next();
                    if (result.score >= result.gamma && result.bestMove != null) {
                        int i = result.bestMove.from();
                        int j = result.bestMove.to();
                        if (hist.size() % 2 == 0) {
                            i = 119 - i;
                            j = 119 - j;
                        }

                        moveStr = render(i) + render(j);
						c=result.bestMove.promotion();
						if(c>0)
							moveStr+=Character.toLowerCase(c);
                        System.out.printf("info depth %d score cp %d pv %s%n", result.depth, result.score, moveStr);
                    }

                    if (moveStr != null && System.currentTimeMillis() - startTime > thinkTime * 0.8) {
                        break;
                    }
                }
                System.out.println("bestmove " + (moveStr != null ? moveStr : "(none)"));
            }
        }
    }

    private static double getThinkTime(String[] tokens, List<Position> hist) {
        int wtime = 300000, btime = 300000, winc = 0, binc = 0;
        for (int i = 1; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "wtime" -> wtime = Integer.parseInt(tokens[++i]);
                case "btime" -> btime = Integer.parseInt(tokens[++i]);
                case "winc" -> winc = Integer.parseInt(tokens[++i]);
                case "binc" -> binc = Integer.parseInt(tokens[++i]);
            }
        }

        if (hist.size() % 2 == 0) {
            wtime = btime;
            winc = binc;
        }

        double thinkTime = Math.min(wtime / 40.0 + winc, wtime / 2.0 - 1000.0);
        return thinkTime;
    }
}
