package Javafish;

import java.util.*;
import static Javafish.Constants.*;

class Entry {
    int lower;
    int upper;

    Entry(int lower, int upper) {
        this.lower = lower;
        this.upper = upper;
    }
}

public class Searcher {
    private Map<Tuple, Entry> tpScore;
    private Map<Position, Move> tpMove;
    private Set<Position> history;
    private int nodes;

    public Searcher() {
        this.tpScore = new HashMap<>();
        this.tpMove = new HashMap<>();
        this.history = new HashSet<>();
        this.nodes = 0;
    }

    public int bound(Position position, int gamma, int depth, boolean canNull) {
        this.nodes++;
        depth = Math.max(depth, 0);
        if (position.score() <= -MATE_LOWER) {
            return -MATE_UPPER;
        }
        Entry entry = tpScore.getOrDefault(new Tuple(position, depth, canNull), new Entry(-MATE_UPPER, MATE_UPPER));
        if (entry.lower >= gamma) return entry.lower;
        if (entry.upper < gamma) return entry.upper;
        if (canNull && depth > 0 && history.contains(position)) {
            return 0;
        }
        int best = -MATE_UPPER;
		
		if(depth > 2 && canNull && Math.abs(position.score()) < 500)
		{
			Move move=null;
			int score=-bound(position.rotate(true), 1 - gamma, depth - 3, true);
			best = Math.max(best, score);
			if(best >= gamma)
			{
				if(move!=null)
					tpMove.put(position, move);
				return boundEnd(position, gamma, depth, canNull, best, entry);
			}
		}
		if(depth==0)
		{
			Move move=null;
			int score=position.score();
			best = Math.max(best, score);
			if(best >= gamma)
			{
				if(move!=null)
					tpMove.put(position, move);
				return boundEnd(position, gamma, depth, canNull, best, entry);
			}
		}
		Move killer = tpMove.get(position);
		if(killer==null&& depth > 2)
		{
			bound(position, gamma, depth - 3, false);
			killer = tpMove.get(position);
		}

		int val_lower = QS - depth * QS_A;

		if(killer!=null&& position.value(killer) >= val_lower)
		{
			Move move=killer;
			int score=-bound(position.move(killer), 1 - gamma, depth - 1, true);
			best = Math.max(best, score);
			if(best >= gamma)
			{
				if(move!=null)
					tpMove.put(position, move);
				return boundEnd(position, gamma, depth, canNull, best, entry);
			}
		}
		
		List<MoveScore> moveScores = new ArrayList<>();
		for (Move move : position.genMoves()) {
			moveScores.add(new MoveScore(move, position.value(move)));
		}

		moveScores.sort((a, b) -> Integer.compare(b.score, a.score)); // descending
		for(MoveScore ms: moveScores)
		{
			int val=ms.score;
			Move move=ms.move;
			if (val < val_lower)
				break;

			if (depth <= 1 && position.score() + val < gamma)
			{
				int score=val < MATE_LOWER?position.score() + val: MATE_UPPER;
				best = Math.max(best, score);
				if(best >= gamma)
				{
					if(move!=null)
						tpMove.put(position, move);
					return boundEnd(position, gamma, depth, canNull, best, entry);
				}
				
				break;
			}
			int score=-bound(position.move(move), 1 - gamma, depth - 1, true);
			best=Math.max(best, score);
			if(best >= gamma)
			{
				if(move!=null)
					tpMove.put(position, move);
				return boundEnd(position, gamma, depth, canNull, best, entry);
			}
		}
	
		return boundEnd(position, gamma, depth, canNull, best, entry);
    }

	private int boundEnd(Position position, int gamma, int depth, boolean canNull, int best, Entry entry)
	{
        if (depth > 2 && best == -MATE_UPPER)
		{
            Position flipped = position.rotate(true);
            boolean in_check = bound(flipped, MATE_UPPER, 0, true) == MATE_UPPER;
            best = in_check?-MATE_LOWER:0;
		}
        if (best >= gamma)
            tpScore.put(new Tuple(position, depth, canNull), new Entry(best, entry.upper));
        else if (best < gamma)
            tpScore.put(new Tuple(position, depth, canNull), new Entry(entry.lower, best));
		return best;
	}
	
    public Iterator<DepthScore> search(List<Position> history) {
        this.nodes = 0;
        this.history = new HashSet<>(history);
        this.tpScore.clear();
        return new Iterator<DepthScore>() {
            private int depth = 1;
			private int gamma = 0;
			int lower = -MATE_LOWER;
			int upper = MATE_LOWER;

            @Override
            public boolean hasNext() {
                return depth < 1000;
            }

            @Override
            public DepthScore next() {
                while (lower < upper - EVAL_ROUGHNESS) {
                    int score = bound(history.get(history.size() - 1), gamma, depth, false);
                    if (score >= gamma) {
                        lower = score;
                    }
                    if (score < gamma) {
                        upper = score;
                    }
                    DepthScore result = new DepthScore(depth, gamma, score, tpMove.get(history.get(history.size() - 1)));
                    gamma = (lower + upper + 1) / 2;
                    return result;
                }
                depth++;
				lower=-MATE_LOWER;
				upper=MATE_LOWER;
                return next();
            }
        };
    }
}

class MoveScore {
    Move move;
    int score;

    MoveScore(Move move, int score) {
        this.move = move;
        this.score = score;
    }
}

class DepthScore {
    int depth;
    int gamma;
    int score;
    Move bestMove;

    DepthScore(int depth, int gamma, int score, Move bestMove) {
        this.depth = depth;
        this.gamma = gamma;
        this.score = score;
        this.bestMove = bestMove;
    }
}

record Tuple(Position position, int depth, boolean canNull) {}