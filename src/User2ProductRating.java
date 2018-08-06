	import java.util.*;
	
	class User2ProductRating implements Comparable<User2ProductRating>{

		int userid; 
		Map<Integer, Integer> userRatings; // pid -> rating

		public User2ProductRating(int _userid){
			this.userid = _userid; 
			userRatings = new TreeMap<Integer, Integer>();
		}


		public User2ProductRating(int _userid, Map<Integer, Integer> userRatings){
			this.userid = _userid; 
			this.userRatings = userRatings; 
		}

		public int getUid(){
			return userid;
		}

		public void put(int pid, int rating){
			userRatings.put(pid, rating);
		}

		public int numProd(){
			return userRatings.keySet().size();
		}

		public void removeProd(int key){
			userRatings.remove(key);
		}

		public int getRate(int pid){
			return userRatings.get(pid); 
		}

		public Map<Integer, Integer> getAllRatings(){
			return (Map<Integer, Integer>) ((TreeMap<Integer, Integer>) userRatings).clone();
		}

		public Set<Integer> getAllProd(){
			return userRatings.keySet(); 
		}

		/* Remove all the products under 
		*  the given value of rating
		*/
		public void removeProds(int ratingThreeshold){
			Set<Integer> products = new TreeSet(userRatings.keySet());
			for(Integer k : products)
				if(userRatings.get(k)<ratingThreeshold)
					userRatings.remove(k);
		}

		public User2ProductRating getTop(int k){
			List<Map.Entry<Integer, Integer>> list = MainSimulator.sortByValue(userRatings);

			Map<Integer, Integer> newMap = new TreeMap<Integer, Integer>(); 

				int count = 0;
				int n = list.size();
				int toSkip = n - k; 
				if(toSkip<0)
					toSkip = 0; 
				Iterator<Map.Entry<Integer, Integer>> it = list.iterator();
				while(it.hasNext() && count<toSkip){
						Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) it.next();

						if(MainSimulator.DEBUG==1)
							System.err.println("getTop(), uid=" + userid + ", Skipping product=" + entry.getKey() + ", NOT in the top-" + k + " products, rating=" + entry.getValue());
//					newScoreMap.put(entry.getKey(), entry.getValue());
						count++;
				}

				while(it.hasNext()){
						Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) it.next();
						newMap.put(entry.getKey(), entry.getValue()); 
						if(MainSimulator.DEBUG==1)
							System.err.println("getTop(), uid=" + userid + ", Considering  product " + entry.getKey() + " in the top-" + k + " products, rating=" + entry.getValue());
				}

				return new User2ProductRating(userid, newMap);
			}
		

		public int compareTo(User2ProductRating obj){
			return (userid < obj.getUid() ? -1 : (userid > obj.getUid() ? 1 : 0));
		}

		public boolean equals(Object obj){
			if (obj instanceof User2ProductRating)
				return (((User2ProductRating)obj).getUid() == userid); 
			return false;
		}
	}
