import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

 public class Kosaraju

{

    /** DFS function **/

    public void DFS(List<Integer>[] graph, int v, boolean[] visited, List<Integer> comp) 

    {

        visited[v] = true;

        for (int i = 0; i < graph[v].size(); i++)

            if (!visited[graph[v].get(i)])

                DFS(graph, graph[v].get(i), visited, comp);

        comp.add(v);

    }

    /** function fill order **/

    public List<Integer> fillOrder(List<Integer>[] graph, boolean[] visited) 

    {        

        int V = graph.length;

        List<Integer> order = new ArrayList<Integer>();

 

        for (int i = 0; i < V; i++)

            if (!visited[i])

                DFS(graph, i, visited, order);

        return order;

    }

    /** function to get transpose of graph **/

    public List<Integer>[] getTranspose(List<Integer>[] graph)

    {

        int V = graph.length;

        List<Integer>[] g = new List[V];

        for (int i = 0; i < V; i++)

            g[i] = new ArrayList<Integer>();

        for (int v = 0; v < V; v++)

            for (int i = 0; i < graph[v].size(); i++)

                g[graph[v].get(i)].add(v);

        return g;

    }

    /** function to get all SCC **/

    public List<List<Integer>> getSCComponents(List<Integer>[] graph)

    {

        int V = graph.length;

        boolean[] visited = new boolean[V];

        List<Integer> order = fillOrder(graph, visited);       

        /** get transpose of the graph **/

        List<Integer>[] reverseGraph = getTranspose(graph);        

        /** clear visited array **/

        visited = new boolean[V];

        /** reverse order or alternatively use a stack for order **/

        Collections.reverse(order);

 

        /** get all scc **/

        List<List<Integer>> SCComp = new ArrayList<>();

        for (int i = 0; i < order.size(); i++)

        {

            /** if stack is used for order pop out elements **/

            int v = order.get(i);

            if (!visited[v]) 

            {

                List<Integer> comp = new ArrayList<>();

                DFS(reverseGraph, v, visited, comp);

                SCComp.add(comp);

            }

        }

        return SCComp;

    }

    /** main 
     * @throws IOException **/

    public static void main(String[] args) throws IOException

    {

        Scanner scan = new Scanner(System.in);
        Kosaraju k = new Kosaraju();

        FileReader fileReaderProva2 = new FileReader("ciao-trust.txt");
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
        double [][] rel=new double [arrayDiStringhe2.length][2];
        int contArchi=0;
        for (int n=0; n<arrayDiStringhe2.length; n++){
        	item4=arrayDiStringhe2[n].split("  ");
        		rel[n][0]= Double.parseDouble(item4[1]);
        		rel[n][1]= Double.parseDouble(item4[2]);	
      			contArchi++;
        }
        System.out.println("Num. Archi: "+contArchi);  	
     
        System.out.println("Inserisci numero utenti:");

        /** number of vertices **/

        int V = scan.nextInt();

        List<Integer>[] g = new List[V];

        for (int i = 0; i < V; i++)

            g[i] = new ArrayList<Integer>();
        
        int E = contArchi;
        /** all edges **/

        System.out.println("Numero archi: "+ E +". Procedo alla costruzione del grafo.");
        for (int i = 0; i < E; i++)
        {
            int x = (int) rel[i][0];
            int y = (int) rel[i][1];
            /** add edge **/

            g[x].add(y);  
        }

        /** print all strongly connected components **/

        List<List<Integer>> scComponents = k.getSCComponents(g);

        //System.out.println(scComponents); 
        
        int cont=1;
		try{
				PrintWriter writer = new PrintWriter("group.txt", "UTF-8");
				for (int j=0;j<scComponents.size();j++){
					if(scComponents.get(j).size()>1){
						for (int h=0;h<scComponents.get(j).size();h++){
							writer.println(scComponents.get(j).get(h)+" "+cont);	
						}
						cont++;
					}
				}
					    
			    writer.close();
			}
			catch (IOException e) {
				// do something
			}
		System.out.println("Fine calcolo componenti fortemente connesse."); 
			
	}
   
    
}