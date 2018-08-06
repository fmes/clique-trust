import java.util.*;

	public class Product2Recommenders implements Comparable<Product2Recommenders>{

		int pid; 
		Map<Integer, Integer> recRatings; // recommender -> rating

		public Product2Recommenders(int _pid){
			this.pid = _pid; 
			recRatings = new TreeMap<Integer, Integer>();
		}

		public int getPid(){
			return pid;
		}

		public void put(int recid, int rating){
			recRatings.put(recid, rating);
		}

		public int numRec(){
			return recRatings.keySet().size();
		}

		public Set<Integer> getAllRecs(){
			return recRatings.keySet();
		}

		public Map<Integer, Integer> getAllRatings(){
			return (Map<Integer,Integer>) ((TreeMap<Integer, Integer>) recRatings).clone();
		}

		public int getRate(int recId){
			return recRatings.get(recId);
		}

		public int compareTo(Product2Recommenders obj){
			return (pid < obj.getPid() ? -1 : (pid > obj.getPid() ? 1 : 0));
		}

		public boolean equals(Object obj){
			if (obj instanceof Product2Recommenders)
				return (((Product2Recommenders) obj).getPid() == this.pid);
			return false;
		}
	}
