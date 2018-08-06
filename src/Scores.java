import java.util.*;
import java.util.Map.Entry;	

	class Scores implements Comparable<Scores>{

		Integer uid;
		Map<Integer, Double> scoreMap; // pid -> score

		public Scores(int _uid){
			this.uid = _uid;
			scoreMap = new TreeMap<Integer, Double>();
		}

		public int getUid(){
			return uid;
		}

		public void put(int pid, double score){
			scoreMap.put(pid, score);
		}

		public int numProd(){
			return scoreMap.keySet().size();
		}

		public Set<Integer> getAllProd(){
			return scoreMap.keySet();
		}
		
		public int getSize(){
			return scoreMap.keySet().size();
		}
		
		public Map<Integer,Double> getProd(){
			return scoreMap;
		}

		public void setProd(Map<Integer, Double> m){
			this.scoreMap = m;
		}

		public double getScore(int prod){
			return scoreMap.get(prod);
		}

		public int compareTo(Scores obj){
			return (uid < obj.getUid() ? -1 : (uid > obj.getUid() ? 1 : 0));
		}

		public boolean equals(Object obj){
			if (obj instanceof Scores)
				return (((Scores) obj).getUid() == uid);
			return false;
		}
		
}	
