/* ==========================================
 * JGraphT : a free Java graph-theory library 
 * ========================================== 
 * 
 * Project Info:  http://jgrapht.sourceforge.net/ 
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh) 
 * 
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors. 
 * 
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version. 
 * 
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA. 
 */
/* -------------------
 * BronKerboschCliqueFinder.java 
 * ------------------- 
 * (C) Copyright 2005-2008, by Ewgenij Proschak and Contributors. 
 * 
 * Original Author:  Ewgenij Proschak 
 * Contributor(s):   John V. Sichi 
 * 
 * $Id: BronKerboschCliqueFinder.java 645 2008-09-30 19:44:48Z perfecthash $ 
 * 
 * Changes 
 * ------- 
 * 21-Jul-2005 : Initial revision (EP); 
 * 26-Jul-2005 : Cleaned up and checked in (JVS); 
 * 
 */ 
 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*; 
 
import org.jgrapht.*;
import org.jgrapht.graph.*; 
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

//import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph; 
import edu.uci.ics.jung.graph.Hypergraph;
 
 
/**
 * This class implements Bron-Kerbosch clique detection algorithm as it is 
 * described in [Samudrala R.,Moult J.:A Graph-theoretic Algorithm for 
 * comparative Modeling of Protein Structure; J.Mol. Biol. (1998); vol 279; pp. 
 * 287-302] 
 * 
 * @author Ewgenij Proschak 
 */ 
public class BronKerboschCliqueFinder<V, E> 
{ 
    //~ Instance fields -------------------------------------------------------- 
 
 
    private Collection<Set<V>> cliques;

	private Graph<V, E> graph; 
 
    //~ Constructors ----------------------------------------------------------- 
 
    /**
     * Creates a new clique finder. 
     * 
     * @param graph the graph in which cliques are to be found; graph must be 
     * simple 
     */ 
    public BronKerboschCliqueFinder(Graph<V, E> graph) 
    { 
        this.graph = graph; 
    } 
 
    //~ Methods ---------------------------------------------------------------- 
 
    /**
     * Finds all maximal cliques of the graph. A clique is maximal if it is 
     * impossible to enlarge it by adding another vertex from the graph. Note 
     * that a maximal clique is not necessarily the biggest clique in the graph. 
     * 
     * @return Collection of cliques (each of which is represented as a Set of 
     * vertices) 
     */ 
    public Collection<Set<V>> getAllMaximalCliques() 
    { 
        // TODO jvs 26-July-2005:  assert that graph is simple 
 
        cliques = new ArrayList<Set<V>>(); 
        List<V> potential_clique = new ArrayList<V>(); 
        List<V> candidates = new ArrayList<V>(); 
        List<V> already_found = new ArrayList<V>(); 
        candidates.addAll(graph.vertexSet());
        System.out.println("I candidati sono:"+candidates.size());
        findCliques(potential_clique, candidates, already_found); 
        cliques.toString();
        return cliques; 
    } 
 
    /**
     * Finds the biggest maximal cliques of the graph. 
     * 
     * @return Collection of cliques (each of which is represented as a Set of 
     * vertices) 
     */ 
    public Collection<Set<V>> getBiggestMaximalCliques() 
    { 
        // first, find all cliques 
        getAllMaximalCliques(); 
 
        int maximum = 0; 
        Collection<Set<V>> biggest_cliques = new ArrayList<Set<V>>(); 
        for (Set<V> clique : cliques) { 
            if (maximum < clique.size()) { 
                maximum = clique.size(); 
            } 
        } 
        for (Set<V> clique : cliques) { 
            if (maximum == clique.size()) { 
                biggest_cliques.add(clique); 
            } 
        } 
        return biggest_cliques; 
    } 
 
    private void findCliques( 
        List<V> potential_clique, 
        List<V> candidates, 
        List<V> already_found) 
    { 
        List<V> candidates_array = new ArrayList<V>(candidates); 
        if (!end(candidates, already_found)) { 
            // for each candidate_node in candidates do 
            for (V candidate : candidates_array) { 
                List<V> new_candidates = new ArrayList<V>(); 
                List<V> new_already_found = new ArrayList<V>(); 
 
                // move candidate node to potential_clique 
                potential_clique.add(candidate); 
                candidates.remove(candidate); 
 
                // create new_candidates by removing nodes in candidates not 
                // connected to candidate node 
                for (V new_candidate : candidates) { 
                    if (graph.containsEdge(candidate, new_candidate)) { 
                        new_candidates.add(new_candidate); 
                    } // of if 
                } // of for 
 
                // create new_already_found by removing nodes in already_found 
                // not connected to candidate node 
                for (V new_found : already_found) { 
                    if (graph.containsEdge(candidate, new_found)) { 
                        new_already_found.add(new_found); 
                    } // of if 
                } // of for 
 
                // if new_candidates and new_already_found are empty 
                if (new_candidates.isEmpty() && new_already_found.isEmpty()) { 
                    // potential_clique is maximal_clique 
                    cliques.add(new HashSet<V>(potential_clique)); 
                } // of if 
                else { 
                    // recursive call 
                    findCliques( 
                        potential_clique, 
                        new_candidates, 
                        new_already_found); 
                } // of else 
 
                // move candidate_node from potential_clique to already_found; 
                already_found.add(candidate); 
                potential_clique.remove(candidate); 
            } // of for 
        } // of if 
    } 
 
    private boolean end(List<V> candidates, List<V> already_found) 
    { 
        // if a node in already_found is connected to all nodes in candidates 
        boolean end = false; 
        int edgecounter; 
        for (V found : already_found) { 
            edgecounter = 0; 
            for (V candidate : candidates) { 
                if (graph.containsEdge(found, candidate)) { 
                    edgecounter++; 
                } // of if 
            } // of for 
            if (edgecounter == candidates.size()) { 
                end = true; 
            } 
        } // of for 
        return end; 
    } 
    
    public static void main (String[] args) throws IOException {

			if(args.length<5){
				System.out.println("Usage: BronKerboschCliqueFinder <num_agents> <trust_file> <out_file> <minCliqueSize> <MaxCliqueSize> (given  " + args.length + " parameters)");
				System.exit(-1);			
			}
			    	
    	Graph<String, DefaultWeightedEdge> graph = new
    		DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

			int numAgent = Integer.parseInt(args[0]);
    	Integer cont=0;
    	for (int h=1; h<=numAgent; h++){
    		cont++;
    		graph.addVertex(Integer.toString(cont));
    	} // from 1 to  numAgent
    	
    	FileReader fileReaderProva2 = new FileReader(args[1]);
        BufferedReader fileBufereReader2 = new BufferedReader(fileReaderProva2); 
        //ArrayList<String> datiRel = new ArrayList<String>();

        int contArchi=0;
       
        String line = fileBufereReader2.readLine();
				String item; 
				String s1, s2; 
				String first , second; 
        while(line!=null){
					item = line.trim(); //leading and trailing whitespaces
					//System.out.println("Line: " + item); 

					s1 = item.split("\\s+")[0].trim(); 
					s2 = item.split("\\s+")[1].trim();
					first = Integer.toString(Double.valueOf(s1).intValue()); 
					second = Integer.toString(Double.valueOf(s2).intValue()); 
					//System.out.println("Adding vertices " + s1 + " " + s2);
					if(graph.addEdge(first, second)!=null)
						contArchi++;
					else{
						if(graph.getEdge(first, second)!=null)
							System.err.println("WARN: duplicate edge! [" + first + "->" + second + "]");
						else
							System.err.println("Impossible to add an adge (unknown error): " + first + " " + second);
					}

          line = fileBufereReader2.readLine();
        }

				System.out.println("archi: " + contArchi + ", edge-set-size: " + graph.edgeSet().size()); 

       assert(graph.edgeSet().size()==contArchi);

				//maximum degree
				int mx = 0; int d = 0; 
				Set vset = graph.vertexSet();
				Iterator<String> it1 = vset.iterator();
				String vertex;  
				while(it1.hasNext()){
					vertex = it1.next(); 
					d=graph.degreeOf(vertex);
					//System.out.println("Vertex: " + vertex + "degree: " + d); 
					if(d>=mx)
						mx = d;
				}

			 System.out.println("Max degree: " + mx);
       System.out.println("Running BronKerbosh..");

       BronKerboschCliqueFinder bk = new BronKerboschCliqueFinder(graph);
       Collection result = bk.getAllMaximalCliques();
       Collection c = new ArrayList();
       c.addAll(result);
       Object[] array = c.toArray();
       System.out.println("Numero di cricche:"+array.length);
       int conta = 1;
       String v;
			 int maxSizeClique = 0; int s; 
			 int minSize = Integer.parseInt(args[3]);
			 int maxSize = Integer.parseInt(args[4]);
			 if(minSize>maxSize || minSize<0 || maxSize<0){
			 		System.err.println("WARN: minSize not <= maxSize or any negative values, considering the maximal cliques of all sizes"); 
			 		minSize = maxSize = -1;
				}
			 Map<Integer, Integer> cliquesSize = new TreeMap<Integer, Integer>(); 
				try{
					PrintWriter writer = new PrintWriter(args[2], "UTF-8");
						for ( int i = 0; i < array.length; i++ ) {
			    	   HashSet element =  (HashSet) array[ i ];
							 s = element.size(); 

								if(cliquesSize.get(s)==null)
									cliquesSize.put(s,1);
								else
									cliquesSize.put(s, cliquesSize.get(s) + 1);

							 if(s>=maxSizeClique)
							 	maxSizeClique = s; 

							if(minSize<0 || s>=minSize && s<=maxSize){
			    	   Iterator it;
			    	   it=element.iterator();
			    	    while(it.hasNext()) {
			    	      v= (String) it.next();
			    	      writer.println(v+" "+conta);
			    	   	}
			    	   conta++;
							 }			    	   
			    	  } 
				    // Per stampare tutte le cricche
					//writer.println(result);						    
			    writer.close();


				PrintWriter writerDist = new PrintWriter("DIST-" + args[2], "UTF-8");
				Iterator<Integer> itMap = cliquesSize.keySet().iterator(); 
				Integer currentKey; 
				while(itMap.hasNext()){
					currentKey = itMap.next(); 
					writerDist.println(currentKey.toString() + " "  + cliquesSize.get(currentKey).toString()); 
				}

			    writerDist.close();

					System.out.println("Max size for cliques: " + maxSizeClique);
			}
			catch (IOException e) {
				// do something
			}
		System.out.println("Fine calcolo cricche."); 
    }
} 
