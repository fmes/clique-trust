import java.awt.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.jgrapht.alg.BronKerboschCliqueFinder;

/*
 * Given a set of friends, this class will find the maximal cliques using the 
 * Bron-Kerbosch algorithm
 */
public class BronKerbosch {
    private Set<Set<Friend>> cliques;
    public BronKerbosch(){}
    /*
     *  Find the maximal cliques in a given set of friends
     *
     *  @return the set of all maximal cliques (each of which is in turn a set
     *  of friends) present in the graph.
     *
     */
    
    
    public static void main (String[] args) throws IOException {
    	
    	 		FileReader fileReaderProva2 = new FileReader(args[0]);
         BufferedReader fileBufereReader2 = new BufferedReader(fileReaderProva2); 
         ArrayList<String> datiRel = new ArrayList<String>();
        
         String s2 = fileBufereReader2.readLine();
         while(s2!=null){
           datiRel.add(s2);
           s2 = fileBufereReader2.readLine();
         }
         String [] arrayDiStringhe2 = datiRel.toArray(new String[0]);
         //for (int n=0;n<arrayDiStringhe2.length;n++) 
         	//System.out.println("ElementoNuovo:" + arrayDiStringhe2[n]);
         	System.out.println("Numero tuple file trust: " + arrayDiStringhe2.length);
         String[] item4 = null;
         HashMap<String, Friend> Users = new HashMap<String, Friend>();
         HashSet<Friend> JustFriends = new HashSet<Friend>();
         //double [][] rel=new double [arrayDiStringhe2.length][2];
         int contArchi=0;
         Friend current = new Friend();
 		 Friend inner= new Friend();
         for (int n=0; n<arrayDiStringhe2.length; n++){
         	item4=arrayDiStringhe2[n].split("  ");
         	//rel[n][0]= Double.parseDouble(item4[1]);
    		//rel[n][1]= Double.parseDouble(item4[2]);
    		current.id= Double.toString(Double.parseDouble(item4[1])).replace(".0", "");
    		inner.id=Double.toString(Double.parseDouble(item4[2])).replace(".0", "");
    		
    		current.friends.put(inner, 1);
    		JustFriends.add(current);
  			contArchi++;
         }
         
         System.out.println(contArchi);
         System.out.println(JustFriends.size());
 		
    	BronKerbosch bk = new  BronKerbosch();
    	Set<Set<Friend>> cliques = bk.maxCliques(JustFriends);
    	for(Set<Friend> clique : cliques) {
    		System.out.println("-----");
    		for(Friend f: clique) {
    			System.out.println(f.id);
    		}
    	}
    }
    
    public Set<Set<Friend>> maxCliques(Set<Friend> people){
        cliques = new HashSet();
        ArrayList<Friend> potential_clique = new ArrayList<Friend>();
        ArrayList<Friend> candidates = new ArrayList<Friend>();
        ArrayList<Friend> already_found = new ArrayList<Friend>();
        candidates.addAll(people);
        findCliques(potential_clique,candidates,already_found);
        return cliques;
    }
    
    private void findCliques(ArrayList<Friend> potential_clique, ArrayList<Friend> candidates, ArrayList<Friend> already_found) {
    	ArrayList<Friend> candidates_array = new ArrayList(candidates);
        if (!end(candidates, already_found)) {
            // for each candidate_node in candidates do
            for (Friend candidate : candidates_array) {
                ArrayList<Friend> new_candidates = new ArrayList<Friend>();
                ArrayList<Friend> new_already_found = new ArrayList<Friend>();

                // move candidate node to potential_clique
                potential_clique.add(candidate);
                candidates.remove(candidate);

                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                for (Friend new_candidate : candidates) {
                    if (candidate.friends.containsKey(new_candidate))
                    {
                        new_candidates.add(new_candidate);
                    }
                }

                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                for (Friend new_found : already_found) {
                    if (candidate.friends.containsKey(new_found)) {
                        new_already_found.add(new_found);
                    }
                }

                // if new_candidates and new_already_found are empty
                if (new_candidates.isEmpty() && new_already_found.isEmpty()) {
                    // potential_clique is maximal_clique
                    cliques.add(new HashSet<Friend>(potential_clique));
                }
                else {
                    findCliques(
                        potential_clique,
                        new_candidates,
                        new_already_found);
                }

                // move candidate_node from potential_clique to already_found;
                already_found.add(candidate);
                potential_clique.remove(candidate);
            }
        }
    }
    private boolean end(ArrayList<Friend> candidates, ArrayList<Friend> already_found)
    {
        // if a node in already_found is connected to all nodes in candidates
        boolean end = false;
        int edgecounter;
        for (Friend found : already_found) {
            edgecounter = 0;
            for (Friend candidate : candidates) {
                if (found.friends.containsKey(candidate)) {
                    edgecounter++;
                }
            }
            if (edgecounter == candidates.size()) {
                end = true;
            }
        }
        return end;
    }
}
