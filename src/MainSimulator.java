import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import java.util.concurrent.*;

public class MainSimulator{
	
	//input files 
	public static  String RELIABILITY_FILE = "ciao-trust.txt";
	public static  String RATING_FILE = "ciao-rating-sorted.txt";
	public static  String GROUP_FILE = "group.txt";
	public static  String REP_FILE = "reputation.txt";
	public static 	String SIM_KEY = "CIAO";
	//public static String SIM_KEY = "EPI"; 

	//Array indeces
	public static final short REP_SUM_INDEX = 0;
	public static final short REP_COUNT_INDEX = 1;
	public static final short REP_AVG_INDEX = 2;

	public static final short RATING_UID_INDEX = 0;
	public static final short RATING_PROD_ID_INDEX = 1;
	public static final short RATING_CATEGORY_ID_INDEX = 2;
	public static final short RATING_RATING_INDEX = 3;
	public static final short RATING_HELP_INDEX = 4;

	public static final short PRECISION_INDEX = 0;
	public static final short RECALL_INDEX = 1;

	//KEYS for config file
	public static final String DELTA_KEY = "delta";
	public static final String THREESHOLD_RATING_PRODUCT_KEY = "productRatingThreeshold";
	public static final String NUM_AGENT_KEY = "nAgent";
	public static final String ALPHA_KEY = "alpha";
	public static final String MIN_PRODUCTS_KEY = "minProducts";
	public static final String CONFIG_FILE = "config.txt"; 
	public static final String K_KEY = "top_k";
	public static final String N_CORES_KEY = "ncores"; 
	public static final String PARAL_KEY = "paral"; 
	public static final String DEBUG_KEY = "debug"; 

	public static final String REL_FILE_KEY = "rel-file"; 
	public static final String RATING_FILE_KEY = "rating-file"; 
	public static final String GROUP_FILE_KEY = "groups-file";
	public static final String REP_FILE_KEY = "rep-file"; 

	public static final String SIM_KEY_KEY = "sim-key"; 

	//Constants
	public static int N_CORES = 4;
	private static final String DESC = null;
	public static double MAX_HELPF = 6.0;
	public static int K_MAX = 10;
	public static boolean PARAL = true;
	public static int DEBUG = 1; 

	public static int FIRST_AGENT = 1; 
	
	//Class attributes
	public static double alpha, delta; 
	public static int numAgent, minAcquisti; 
	public static int soglia; 
	public static int top_k;
	
	public MainSimulator(String configFile){

		Properties p = new Properties(); 
	
		try{
			if(configFile==null)
				p.load(new BufferedReader(new FileReader(CONFIG_FILE)));
			else
				p.load(new BufferedReader(new FileReader(configFile)));
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1); 
		}

		this.numAgent = Integer.parseInt(p.getProperty(NUM_AGENT_KEY));
		this.alpha = Double.parseDouble(p.getProperty(ALPHA_KEY));
		this.delta = Double.parseDouble(p.getProperty(DELTA_KEY));
		this.soglia = Integer.parseInt(p.getProperty(THREESHOLD_RATING_PRODUCT_KEY));
		this.minAcquisti = Integer.parseInt(p.getProperty(MIN_PRODUCTS_KEY));
		this.top_k = Integer.parseInt(p.getProperty(K_KEY)); 
		this.N_CORES = Integer.parseInt(p.getProperty(N_CORES_KEY));
		int par = Integer.parseInt(p.getProperty(PARAL_KEY));

		int dbg = Integer.parseInt(p.getProperty(DEBUG_KEY));

		String s = null;

		//read names of files from configuration
		s= p.getProperty(REL_FILE_KEY);
		if(s!=null){
			RELIABILITY_FILE = s;
		}

		System.out.println("Reliability file: " + RELIABILITY_FILE); 

		s= p.getProperty(REP_FILE_KEY);
		if(s!=null)
			REP_FILE = s;

		s= p.getProperty(RATING_FILE_KEY);
		if(s!=null)
			RATING_FILE = s;

		s= p.getProperty(GROUP_FILE_KEY);
		if(s!=null)
			GROUP_FILE = s;		

		s= p.getProperty(SIM_KEY_KEY);
		if(s!=null)
			SIM_KEY = s;		

		if(par!=0)
			PARAL = true;
		else
			PARAL = false; 
		
		if(dbg!=0)
			DEBUG = 1;
		else
			DEBUG = 0; 

		//print arguments 
		System.out.println("\n ** \n numAgent: " + numAgent + " \n ALPHA: " + alpha + "\n soglia: " + soglia + "\n delta=" + delta + "\n minAcquisti=" + minAcquisti +"\n top_k=" + top_k +"\n ncores=" + this.N_CORES + "\n paral=" + this.PARAL);

		assert(this.alpha >=0 && this.alpha <=1);
		assert(this.delta >=0 && this.delta <=1);
	}

	public  static int A2I(int a){ //return the index of the agent in any array, given its id
		assert(a>0);
		return a-1;
	}

	public static int I2A(int i){ //return the ID of the agent given its index in any array
		return i+1;
	}

	class R{
		private int status;

		public R(int _status){
			status = _status;
		}

		public int getStatus(){
			return status;
		}
	}

	//Worker (thread) for parallel computation of trust matrix
	class TrustMatrixCalculator implements Callable<R>{
					
			int numAgent;
			double alpha, delta;
			SparseMatrix trust, reliability;
			double [][] repValue;
			ArrayList<ArrayList<Integer>> gruppi;
			int i; 

			public TrustMatrixCalculator(
				int _row_index,
				double _alpha, 
				int _numAgent, 
				double _delta, 
				ArrayList<ArrayList<Integer>> _gruppi, 
				double [][] _repValue, 
				SparseMatrix _trust, 
				SparseMatrix _reliability)
			{	
				this.numAgent = _numAgent;
				this.alpha = _alpha;
				this.delta = _delta; 
				this.trust = _trust;
				this.reliability = _reliability;
				this.repValue = _repValue;
				this.gruppi = _gruppi;
				this.i = _row_index;
			}

	    public R call(){
				computeTrustRow(i, alpha, numAgent, delta, gruppi, repValue, trust, reliability);
				return new R(0);
			}
		}

	/*
	* Look for a file containing trust values for certain values of 
	* alpha and delta, will return a sparse matrix containing trust values, 
	* null otherwise 
	*/
	public SparseMatrix readTrustMatrix(){
		if(!trustFileExist(alpha, delta))
			return null;

		SparseMatrix trust = new SparseMatrix(numAgent);
	 		try{		  
    		FileInputStream fis = new FileInputStream(trustFileName(alpha,delta));
      	BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
          
      	System.out.println("Trying to fill trust matrix from file " + trustFileName(alpha,delta));
	    
	    	String line;
      	while((line=reader.readLine())!=null && line.trim()!=""){
					//System.out.println(line);
					String[] ret = line.trim().split("\\s+");
					assert(ret.length==3);
					trust.put(Integer.parseInt(ret[0]),Integer.parseInt(ret[1]),Double.parseDouble(ret[2]));
      	}
        	System.out.println("Trust (sparse) matrix read, number of non-zero entries=" + trust.nnz());
	  		} catch (Exception e) {
	    		e.printStackTrace();
	  		}
	  	return trust;
	}

	/* TRUST MATRIX COMPUTATION */

	/* Calculate trust values 
	*  by the formula alpha*rel + (1-alpha)*rep + delta
	*  (computeTrustRow(), computeTrust(), saveTrustMatrixOnFile()).
	* The computation can be parallel or not (parmater par)
	*/
	public  SparseMatrix computeAndSaveTrustMatrix(
			int numRows, //compute only numRows rows of the matrix, DEBUGGING ONLY
			double[][] repValue, 
			ArrayList<ArrayList<Integer>> gruppi,
			SparseMatrix reliability, 
			boolean par) throws Exception{

			assert(alpha>=0 && alpha <=1.0 && delta >=0.0 && delta <=1.0 && numAgent > 0); 
	
			System.out.println("Starting computation of trust matrix, alpha=" + alpha + ", delta=" + delta + ", numAgent=" + numAgent + ", numRows=" + numRows);
		  SparseMatrix trust = new SparseMatrix(numAgent);
		  double currentValue;

		//	int start = 23300;  // DEBUG!!!
			int start = FIRST_AGENT;
			
			if(numRows==-1) // i.e. alle the rows must be calculated
				numRows = numAgent;

			if(par){ //parallel computation
				ExecutorService es = Executors.newFixedThreadPool(N_CORES);
				ArrayList<TrustMatrixCalculator> calculators = new ArrayList<TrustMatrixCalculator>();
			
		  	for(int i=A2I(start);i<=A2I(numRows);i++)
					calculators.add(new TrustMatrixCalculator(i, alpha, numAgent, delta, gruppi, repValue, trust, reliability));

		 		List<Future<R>> ret = es.invokeAll(calculators);

				for(Future r : ret)
					r.get(); // wait until the task is done and get result

				es.shutdown();
			}

		  else 
				for(int i=A2I(start);i<=A2I(numRows);i++) // sequential code
					computeTrustRow(i, alpha, numAgent, delta, gruppi, repValue, trust, reliability);

			System.out.println("** Trust matrix, non zero elements: " + trust.nnz());

		  FileWriter trust_out_file= new FileWriter(trustFileName(alpha,delta));
			saveTrustMatrixOnFile(trust, trust_out_file, numAgent);
			trust_out_file.close();

			return trust;
		 } // end trust comp and writing on file..

	public void computeTrustRow(
		int i,
		double alpha, //alpha
		int numAgent,  // number of agents
		double delta,
		ArrayList<ArrayList<Integer>> gruppi,  // groups (cricche)
		double[][] repValue, //reputation
		SparseMatrix trust,
		SparseMatrix adjacency_matrix){

		assert(alpha>=0 && alpha <=1.0 && delta >=0.0 && delta <=1.0 && numAgent>0 && trust!=null && adjacency_matrix!=null && adjacency_matrix.nnz()>0);

		if(DEBUG==1)
			System.err.println("computeTrustRow(): i=" + i + ", alpha=" + alpha + ", numAgent=" + numAgent + ", delta=" + delta);

		if(i%100==0)
			System.out.println("computeTrustRow(" + i + ")");
		for(int j=A2I(FIRST_AGENT); j<=A2I(numAgent); j++)
			computeTrust(i, j, alpha, numAgent, delta, gruppi, repValue, trust, adjacency_matrix);
	}

	public static <K, V>  List<Map.Entry<K, V>> sortByValue(Map<K, V> map) {
			
			if(MainSimulator.DEBUG==1)
				System.err.println("sortByValue()");
	    	List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
				Collections.sort(list, new Comparator<Object>() {
				@SuppressWarnings("unchecked")
				public int compare(Object o1, Object o2) {
					return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
				}
			});

			return list; 
		}

	public static boolean print_and_true(String s){
		System.err.println(s);
		return true;
	}

	public static boolean print_and_false(String s){
		System.out.println(s);
		return false;
	}

	/* Compute the single element i,m of the trust matrix */
	public  void computeTrust(
		int i, int m, // for the potential edge i,m
		double alpha, //alpha
		int numAgent,  // number of agents
		double delta,
		ArrayList<ArrayList<Integer>> gruppi,  // groups (cricche)
		double[][] repValue, //reputation
		SparseMatrix trust,
		SparseMatrix adjacency_matrix)
		{
			if(i==m)
				return;

		if(m%100 == 0 && DEBUG==1)
			System.err.println("computeTrust(), i="+i+", j="+m);

		double v=0.0;
		double vv=0.0;

		double vv1, vv2, vv3; 

		Set<Integer> inter = new HashSet<Integer>();

		inter.addAll(gruppi.get(i));
		inter.retainAll(gruppi.get(m));

		assert(repValue[REP_AVG_INDEX][m]>=0.0 && repValue[REP_AVG_INDEX][m]<=1.0 || print_and_true("WARNING! repValue[" + m + "]:" + repValue[REP_AVG_INDEX][m]));

		//boolean m1 = adjacency_matrix.control(i, m);
		boolean b = (inter.size()==0);
		double rel = adjacency_matrix.get(i, m);

		vv1 = alpha*(rel<0 ? 0 : rel);
		vv2 = (1-alpha)*repValue[REP_AVG_INDEX][m]; 
		vv3 =  delta*(b==false ? 1.0 : 0.0); 

		if(DEBUG==1)
			System.err.println("REL(" + i + "," + m + ")=" + (rel < 0 ? 0.0 : rel) + ", REP(" + m + ")=" + repValue[REP_AVG_INDEX][m] + ", alpha*REL=" + vv1 + ", (1-alpha)*REP=" + vv2 + ", delta*(bool_same_clique)=" + vv3);

		vv = vv1 + vv2 + vv3;
		//alpha*(rel<0 ? 0 : rel) + (1-alpha)*repValue[REP_AVG_INDEX][m] + delta*(b==false ? 1.0 : 0.0);

		v = (vv <= 1.0 ? vv : 1.0);

		assert(vv>1.0 || v==vv);

		trust.put(i,m,v);

		if(DEBUG==1)
			System.err.println("computeTrust(): delta=" + delta + ", alpha=" + alpha + ", i= " + i +  ", j= " + m + ", rep(j)=" + repValue[REP_AVG_INDEX][m] + "inter!=0:  " + !b + ", vv=" + vv + ", v=" + v + ", t(" + i + "," + m + ")="+ (trust.get(i,m)<0 ? 0 : trust.get(i,m)));

		//print_and_true("computeTrust(): i=" + i + ", m=" + m + ", v=" + v);

		assert(v==0.0 || trust.control(i,m));
	}

	public void saveTrustMatrixOnFile(SparseMatrix trust, FileWriter trust_out_file, int numAgent){
		try{
			for(int i=A2I(FIRST_AGENT); i<=A2I(numAgent); i++)
				for(int j=A2I(FIRST_AGENT); j<=A2I(numAgent); j++)
					if(i!=j && trust.get(i,j) > 0.0)
		    		trust_out_file.write(i+" " + j + " " + trust.get(i,j) + "\n");
			trust_out_file.flush();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	public  boolean trustFileExist(double alpha, double delta){
		String n = trustFileName(alpha,delta); 
		if(new File(trustFileName(alpha,delta)).exists()){
			System.out.println("FOUND trust file " + n);
			return true;
		}
		else{
			System.out.println("Trust file " + n +" NOT FOUND");
			return false;
		}
	}

	public  String trustFileName(double alpha, double delta){
		return SIM_KEY + "_trust_ALPHA_"+alpha+"_DELTA_"+delta+".txt";
	}

	/* END TRUST MATRIX COMPUTATION */

	/* Read file containing information about cliques */ 
	public  ArrayList<ArrayList<Integer>> readGroupFile(String filename) throws Exception{ 
	   System.out.println("** Reading file "+filename+"..");
	
	   	FileReader groupFormation= new FileReader(filename);
	   	BufferedReader a = new BufferedReader(groupFormation);
	   	ArrayList<String> datiRelGroup = new ArrayList<String>();
	   	String s8 = a.readLine();
	    	while(s8!=null){
	    		datiRelGroup.add(s8);
	      	s8 = a.readLine();
	    	}

	    String [] arrayDiStringheGroup = datiRelGroup.toArray(new String[0]);
	    String[] item = null;

	    ArrayList<ArrayList<Integer>> gruppi=new ArrayList<ArrayList<Integer>>(numAgent);

	    for(int i=0; i<numAgent; i++){
	      gruppi.add(new ArrayList<Integer>());
	    }

	    for (int v=0; v<arrayDiStringheGroup.length;v++){
	      item=arrayDiStringheGroup[v].split("\\s+");
	      gruppi.get(A2I((int) Double.parseDouble(item[0]))).add(A2I((int) Double.parseDouble(item[1])));
	    }

	    System.out.println("done!");
	    System.out.flush();

			return gruppi; 
	}

	/* Read information about reliability among users*/ 
	public  SparseMatrix readReliabilityFile(String filename) throws Exception{ 
	    System.out.println("** Reading file "+filename+"..");
	    FileReader fileReaderProva2 = new FileReader(filename);
	    BufferedReader fileBufereReader2 = new BufferedReader(fileReaderProva2); 
	    
	    SparseMatrix adjacency_matrix = new SparseMatrix(numAgent);
		   
	    String item = fileBufereReader2.readLine();
			String s1, s2, s3;
			int v1, v2; double rel;  
			String token[];  
			while(item!=null){
	      //datiRel.add(s2);

				token = item.trim().split("\\s+");

				s1 = token[0]; 
				s2 = token[1]; 
				if(token.length>2){
					s3 = item.trim().split("\\s+")[2]; 
					rel = Double.valueOf(s3); // binary 
				}

				else 
					rel = 1.0; 

				v1 = Double.valueOf(s1).intValue();
				v2 = Double.valueOf(s2).intValue();

				adjacency_matrix.put(A2I(v1), A2I(v2), rel);

	      item = fileBufereReader2.readLine();
	    }

	    System.out.println("done! ");

		  System.out.println("**Number of reliability relationships from file "+filename+":" + adjacency_matrix.nnz());

			return adjacency_matrix;
		}

	public double[][] readRepFile(int length) throws Exception{

		  System.out.print("** Reading file " + REP_FILE + "..");

			//field 0 sum of helpf
			//Field 1 number of helpf
			//Field 2 average normalized helpfullness

		  FileReader fr = new FileReader(REP_FILE);
		  BufferedReader fbr = new BufferedReader(fr);
		  ArrayList<String> data = new ArrayList<String>();

		  String s = fbr.readLine();
		  while(s!=null){
				s = s.trim(); 
				if(!s.startsWith("#"))
		  		data.add(s);

		    s = fbr.readLine();
		  }

		  System.out.println("done!");

			assert(data.size() == length);

		  String [] strArr = data.toArray(new String[0]);
		  String[] item;  int id;
		  double [][] repValue=new double [3][length];
		  for (int k=0; k<strArr.length; k++){
		  	item=strArr[k].trim().split("\\s+"); // should be 3 data items
				assert(item.length==4); //agent _id + 3 fields
		    repValue[REP_SUM_INDEX][k]= Double.parseDouble(item[REP_SUM_INDEX+1]); // sum
		    repValue[REP_COUNT_INDEX][k]= Double.parseDouble(item[REP_COUNT_INDEX+1]); // count
		    repValue[REP_AVG_INDEX][k]= Double.parseDouble(item[REP_AVG_INDEX+1]); // normalized average helpfullness
		  }

			return repValue;
	}

	public void saveRepFile(double [][] repValues, int length) throws Exception{

		  System.out.println("** Saving reputation values into file " + REP_FILE + "..");

			//field 0 agent id
			//field 1 sum of helpf
			//Field 2 number of helpf
			//Field 3 average normalized helpfullness

			FileWriter fileoutput=new FileWriter(REP_FILE);
			fileoutput.write("#AgId\t SUM(Helpfulness)\t COUNT(Helpfulness) \t AVG(Helpfulness)/MAX(helpfulness)\n");
	    	for(int i=0; i<length; i++){
			  	fileoutput.write(i + " " + repValues[REP_SUM_INDEX][i] + " " + repValues[REP_COUNT_INDEX][i] + " " + repValues[REP_AVG_INDEX][i] + "\n");
			}

			fileoutput.close();
			System.out.println("File " + REP_FILE + " written..");
	}

	/* Read rating file */ 
	public  double[][] readRatingFile(String filename) throws Exception{

		  System.out.print("** Reading file " + filename + "..");

			//field 0 UserId RATING_UID_INDEX
			//Field 1 Product ID  RATING_PROD_ID_INDEX
			//Field 2 Category RATING_CATEGORY_ID_INDEX
			//Field 3 Rating RATING_RATING_INDEX
			//Field 4 Helpfulness RATING_HELP_INDEX 
			//Field 5 timestamp --

		  FileReader fileReaderProva = new FileReader(filename);
		  BufferedReader fileBufereReader = new BufferedReader(fileReaderProva);
		  ArrayList<String> datiAcquisto = new ArrayList<String>();

		  String s = fileBufereReader.readLine();
		  while(s!=null){
		  	datiAcquisto.add(s.trim());
		    s = fileBufereReader.readLine();
		  }

		  System.out.println("done!");

			//Array rating
		  String [] arrayDiStringhe = datiAcquisto.toArray(new String[0]);
		  String[] item3 = null;
			System.out.println("Rating Array length: " + arrayDiStringhe.length); 
		  double [][] rating=new double [arrayDiStringhe.length][6];

		  for (int k=0; k<arrayDiStringhe.length; k++){
		  	item3=arrayDiStringhe[k].trim().split("\\s+");
				if(item3.length<4)
					System.err.println("Skipping line: " + k); 
				else{

					//if(DEBUG==1)
						//	System.err.println("readRatingfile(), item.length= " + item3.length); 
					for(int i=0; i<item3.length; i++) //read at least 4 fields (uid, pid, cat, rating[, helpf])
		    		rating[k][i]=  Double.parseDouble(item3[i]);				
					}
		  }

			return rating;
	}
	
	/* Compute raputation based on helpfulness */ 
	public  double[][] getReputation(double rating[][]) throws Exception{

			double[][] repValue; 

			if(new File(REP_FILE).exists())
				repValue = readRepFile(numAgent);

			else{

				int contScore=0;
		  	double somma=0;
		  	repValue = new double[3][numAgent]; // 0 = sum; 1 = count; 2 = avg

				Arrays.fill(repValue[REP_SUM_INDEX], 0.0);
				Arrays.fill(repValue[REP_COUNT_INDEX], 0.0);
				Arrays.fill(repValue[REP_AVG_INDEX], 0.0);

				int count = 0;

				System.out.println("computeReputation(), rating.length= " + rating.length); 

		 		for(int k=0; k<rating.length; k++){
					if(DEBUG==1)
						System.out.println("computeRep(), uid=" + (int) rating[k][RATING_UID_INDEX]); 
					repValue[REP_SUM_INDEX][A2I((int) rating[k][RATING_UID_INDEX])]+= rating[k][RATING_HELP_INDEX];  // normalized helfulness
		    	repValue[REP_COUNT_INDEX][A2I((int) rating[k][RATING_UID_INDEX])]+=1.0; // count 
		  	}			

		  	for(int k = 0; k<repValue[REP_AVG_INDEX].length; k++)
					if(repValue[REP_SUM_INDEX][k]>0.0) {
						repValue[REP_AVG_INDEX][k]=repValue[REP_SUM_INDEX][k]/(repValue[REP_COUNT_INDEX][k]*MAX_HELPF);
						assert(repValue[REP_AVG_INDEX][k]>0.0 && repValue[REP_AVG_INDEX][k]<=1.0);
						//System.out.println("User " + I2A(k) + ": " + repValue[REP_AVG_INDEX][k]); 
						count++;
					}

				//save reputations in a file
				saveRepFile(repValue, numAgent); 
			}

			return repValue; 
	}

	/* return a map (prodId -> Product2Recommenders */    
	public Map<Integer, Product2Recommenders> allRecommenderRatings(double[][] rating){

		Map<Integer, Product2Recommenders> allRec = new TreeMap<Integer, Product2Recommenders>();
		int uid, pid, vrating; 

		for(int i = 0; i<rating.length; i++){
			uid= A2I((int) rating[i][RATING_UID_INDEX]); 
			pid = (int) rating[i][RATING_PROD_ID_INDEX];
			vrating= (int) rating[i][RATING_RATING_INDEX];

			if((allRec.get(pid))==null)
				allRec.put(pid, new Product2Recommenders(pid));
	
			allRec.get(pid).put(uid, vrating); //add recommender and rating for the product pid
		}

		System.out.println("allRecommenderRatings(): selected " + allRec.keySet().size() + " products to compute scores..");

		return allRec; 		
	}

	/*
		Select, from the rating array, the users that have bought at least minAcquisti 
		items 
		//NO!! with a value of rating which is at least soglia (soglia and minAcquisti can be -1, i.e. no filter) NO!!
	*/
	public Map<Integer, User2ProductRating> selectUserRatings(
		double rating[][], 
		ArrayList<ArrayList<Integer>> groups, 
		int minP, 
		int minRating){	

		//at least minAcquisti purchases
		//at least soglia rating for all the products

		int skipped = 0;

		System.out.println();
		System.out.println("** START selectUserRatings(minProducts=" + minP + ", minRating=" + minRating + ")");

		Map<Integer, User2ProductRating> allUsersRatings = new TreeMap<Integer,User2ProductRating>();

		int uid, pid, vrating;

		for(int i = 0; i<rating.length; i++){
			uid= A2I((int) rating[i][RATING_UID_INDEX]);
			pid = (int) rating[i][RATING_PROD_ID_INDEX];
			vrating= (int) rating[i][RATING_RATING_INDEX];

			if(groups.get(uid).isEmpty()){
				if(DEBUG==1)
					System.err.println("WARN: Skipping user " + uid + " because it does not appear in any group!"); 
				skipped++;
				continue; 
			}

			if((allUsersRatings.get(uid))==null)
				allUsersRatings.put(uid, new User2ProductRating(uid));
	
			allUsersRatings.get(uid).put(pid, vrating); //add product and rating fo user uid
		}

		System.out.println("** selectUserRatings(): there are " + allUsersRatings.keySet().size() + " buyers (i.e. at least 1 product)!");

		Set<Integer> allUsersWithRating; 

		if(minP>0){
			allUsersWithRating = new TreeSet();
			allUsersWithRating.addAll(allUsersRatings.keySet());
			for(Integer u : allUsersWithRating ){
				User2ProductRating data = allUsersRatings.get(u);
				if(data.numProd()<minP){
					allUsersRatings.remove(u);
					if(DEBUG==1)
						System.err.println("[DEBUG] selectUserRatings(): Removing user " + u + " as with a number of products < " + minP);
				}
			 	else if(DEBUG==1)
						System.err.println("[DEBUG] selectUserRatings(): User " + u + " with " + data.numProd() +" products rated" );
			}
		}

		System.out.println("** SelectUserRatings(): users with at least " + minP + " products are " + allUsersRatings.keySet().size());

		if(minRating>0){
			allUsersWithRating = new TreeSet();
			allUsersWithRating.addAll(allUsersRatings.keySet());
			for(Integer u : allUsersWithRating ){
				User2ProductRating data = allUsersRatings.get(u); 
				data.removeProds(minRating); //remove all the products with rating under the value "s"
				if(data.numProd()==0){
					allUsersRatings.remove(u);
					if(DEBUG==1)
						System.err.println("[DEBUG] selectUserRatings(): Removing user " + u + " (all his products below the threshold " + minRating + ")");
					}
				}
			}

		System.out.println("** selectUserRatings(), DONE! #users=" + allUsersRatings.keySet().size() + " (minProd= " + minP + ", minRating=" + minRating + ")");

		return allUsersRatings;
	} 

	/* 
	*	Compute the scores for each product p bought by the user x. 
  * Parameters: 	
	* - k (get the best k in terms of scores)
	* - s (filter the results by a certain threshold)
	*/
	Map<Integer, Scores> computeAllScores(
		Map<Integer, User2ProductRating> userRatings,
		Map<Integer, Product2Recommenders> productRecommenders,
		SparseMatrix trust, 
		int k, // top k
		double s){ // threshold
			//Compute score(x,p) = sum_{y \in recomenders(x,p)} rating(y,p)*trust(x,y)
			Map<Integer, Scores> ret = new TreeMap<Integer, Scores>();
			double currentScore = 0.0; 
			double sumT = 0.0; 
			double trustV = 0.0;
			Scores scores; 

					System.err.println("** START computeAllScores(), top_k=" + k + ", threshold=" + s);

			//FOR EACH USER
			for(Integer uid : userRatings.keySet()){
				scores = new Scores(uid); 
				ret.put(uid, scores); 

				Set<Integer> allUserProducts = userRatings.get(uid).getAllProd();  

				if(DEBUG==1)
					System.err.println("[DEBUG] computeAllScores(), uid=" + uid + ", number of products=" + allUserProducts.size());

				//FOR EACH USER PRODUCT
				for(Integer pid : allUserProducts){ // for all the products rated by the user, computes the score
					currentScore = 0.0;
					sumT = 0.0; 
					Map<Integer, Integer> ratings  = productRecommenders.get(pid).getAllRatings(); 	
					//if(DEBUG==1)
						//System.err.println("computeAllScores(), uid=" + uid + ", pid=" + pid + ", number of recommenders=" + ratings.size());
					for(Integer recId : ratings.keySet()){ // all the recommenders for the product pid
						if(!((trustV = trust.get(uid, recId)) > 0.0)) // the value returned from the sparse matrix may be -1
							trustV = 0.0;

						currentScore+=ratings.get(recId)*trustV;
						sumT+=trustV;

						if(DEBUG==1)
							System.err.println("   [DEBUG] computeAllScores(), uid=" + uid + ", pid=" + pid + ", recommender=" + recId + ", trust=" + trustV + ", rating=" + ratings.get(recId) + ", currentScore=" + currentScore + ", sumT = " + sumT);
					}
					
					if(sumT>0){
						currentScore = currentScore/sumT; 

					//THREESHOLD
					if(currentScore>s) 
						scores.put(pid, currentScore); // score(uid, pid);
					}

					if(DEBUG==1)
						System.err.println("[DEBUG] computeAllScores(), uid=" + uid + ", pid=" + pid + ", finalScore=" + currentScore + (currentScore>s ? ", SELECTED" : ", NOT SELECTED"));
				} // END FOR EACH USER PRODUCT

			//GET THE BEST K PRODUCTS
			if(k>0){

				System.err.println("** computeAllScores(), uid=" + uid + ", sorting selected scores  to select the top_" + k + "");

				List<Map.Entry<Integer, Double>> sorted = sortByValue(scores.getProd()); // ascendent order

				Map<Integer, Double> newScoreMap = new TreeMap<Integer, Double>();
				Map.Entry<Integer, Double> entry; 
				
				int start = (sorted.size() - k);
				if(start<0){
					start = 0;
					if(DEBUG==1)
						System.err.println("[DEBUG] computeAllScore(), uid=" + uid + ", number of selected scores is less than "+ k); 
				}
				//select all the items from start to the end of the list
				for(Iterator<Map.Entry<Integer, Double>> it = sorted.subList(start, sorted.size()).iterator(); it.hasNext();){
					entry = it.next(); 
					if(DEBUG==1)
						System.err.println("[DEBUG] computeAllScores(), uid=" + uid + ", selecting product=" + entry.getKey() + " as one of the top-" + k + " products, score=" + entry.getValue());
					newScoreMap.put(entry.getKey(), entry.getValue());
				}			

				//replace the products with the computed subset
				scores.setProd(newScoreMap); 
			}

			// uid -> (pid->score, pid-> score, ..., pid->score, ...)
			ret.put(uid, scores);
		}

		System.out.println("** computeAllScores(), DONE! Final scores for " + ret.keySet().size() + " users!");

		return ret; 
	}
	
		
		/* First argument, the scores of the products bought by the agent. 
		*  Second argument. The top-k products bought by the agent. 
		*/ 
	double [][] computePAR(Map<Integer, Scores> ret, Map<Integer, User2ProductRating> ret2) throws IOException{
		int numeratore=0;
		Set<Integer> set;
		System.out.println("** START computePAR(), ret.size()=" + ret.size() + ", ret2.size()=" + ret2.size()); 
		double [][] valutazioni = new double [numAgent][2];

		for (Iterator<Integer> i=ret.keySet().iterator(); i.hasNext();){ //for each user
					Integer e = i.next();
					set = new HashSet<Integer>(ret.get(e).getAllProd()); 
					if(ret2.get(e)==null)
						continue; // no data, no good bought for the user!
					set.retainAll(ret2.get(e).getAllRatings().keySet()); //
					numeratore = set.size();
					if(numeratore==0)
						valutazioni[ret.get(e).uid][PRECISION_INDEX]= valutazioni[ret.get(e).uid][RECALL_INDEX]=0;
					else {
						if(ret.get(e).getAllProd().size()!=0) {
							valutazioni[ret.get(e).uid][PRECISION_INDEX]= (double) numeratore/ret.get(e).getAllProd().size();//Precision
						}
							
						else
							valutazioni[ret.get(e).uid][PRECISION_INDEX]=0;

						if(ret2.get(e).getAllRatings().size()!=0)
							valutazioni[ret.get(e).uid][RECALL_INDEX]= (double) numeratore/ret2.get(e).getAllRatings().keySet().size(); //Recall
						else
							valutazioni[ret.get(e).uid][RECALL_INDEX]=0;
			    }
			  }
			
		System.out.println("** computePAR(), DONE! ");
		System.out.println("** computePAR(), Writing file " + SIM_KEY + "-parameters.txt");
		FileWriter fileoutput=new FileWriter(SIM_KEY + "-parameters.txt");
	    for(int i=0; i<valutazioni.length; i++){
				if(ret.keySet().contains(i))
	    		fileoutput.write(i + " " + valutazioni[i][PRECISION_INDEX] + " " + valutazioni[i][RECALL_INDEX]+"\r\n");
		}
	    fileoutput.close();
		return valutazioni;

		
	}

	public static void main(String [] args) throws Exception{

		//Simulator
		MainSimulator simulator = new MainSimulator(args[0]);	 // argument can be path of config file 
	
		//Agents, items, reliability matrix and reputation
		ArrayList<String> items= new ArrayList<String>();

		//Trust matrix
		SparseMatrix trust; 

		//reliability 
		SparseMatrix reliability = simulator.readReliabilityFile(RELIABILITY_FILE);
		
		//groups
		ArrayList<ArrayList<Integer>> groups = simulator.readGroupFile(GROUP_FILE);

		//Read file of ratings
		double rating[][] = simulator.readRatingFile(RATING_FILE);

		//Compute reputation from helpfulness data read so far
		double repValue[][] = simulator.getReputation(rating); //read from file or compute and store into a file

		if((trust = simulator.readTrustMatrix())==null) // no any trust matrix saved for those values of delta and alpha
			trust = simulator.computeAndSaveTrustMatrix(-1 /*!!DEBUG */, repValue, groups, reliability, PARAL); // compute the trust matrix and save into a file

		//all the users with at least MINACQUISTI products
		//return a map user -> list-of(pid, rating)
		Map<Integer, User2ProductRating> userRatings = simulator.selectUserRatings(rating, groups, minAcquisti, -1); // last parameter could be threeshold
		//System.out.println("User2rating size (minAcquisti=" + minAcquisti + "s=-1): " + userRatings.size());

		//Let GOOD(x) be the set of products bought by x and evaluated as good by  x
		Map<Integer, User2ProductRating> bestRatings = simulator.selectUserRatings(rating, groups, minAcquisti, soglia); // last parameter is the threeshold
		//System.out.println("best rating, selected  (minAcquisti=" + minAcquisti + ", s= " +soglia + "):" +  userRatings.size());

		//all the recommenders
		System.out.println(); 
		System.out.println("** Mapping products to recommenders..");
		Map<Integer, Product2Recommenders> allRecomRatings = simulator.allRecommenderRatings(rating);

		//for all products rated/bought by all the users x, lets compute the score(x,p)
		// Score(x,p) for each p bought by  x: score(x,p) = sum_{y \in recomenders(x,p)} rating(y,p)*trust(x,y)
		// Infine Prec(x,y) = (|good(x) INTER top_k(x)|)/|k| e Recall(x,y)=(|good(x) INTER top_k(x)|)/|good(x)| -- TODO 

		System.out.println(); 
		System.out.println("** For each user x and every product p(x), computing the top-" + MainSimulator.top_k + " scores(x,p)...");
		Map<Integer, Scores> allScores = simulator.computeAllScores(userRatings, allRecomRatings, trust, MainSimulator.top_k, (double) soglia);
		//System.out.println("all-scores-size: " + allScores.size());  		

		if(DEBUG==1){
			System.err.println(); 
			System.err.println("[DEBUG] Best rated products users: (threshold=" + soglia + ")");
			for(int uid: bestRatings.keySet()){
				System.err.println("\t[DEBUG] uid=" + uid + ", bestRating-size: " + bestRatings.get(uid).numProd());
				for(int pid: bestRatings.get(uid).getAllProd())					
					System.err.println("\t\t[DEBUG] pid=" + pid + ", rating: " + bestRatings.get(uid).getRate(pid));
			}

			System.err.println("[DEBUG] Tracing all the scores (TOP-" + MainSimulator.top_k + "):");
			for(int uid: allScores.keySet()){
				Scores sc = allScores.get(uid);
				System.err.println("\t[DEBUG] uid=" + uid + ", scores-size (#of recomm. products): " + allScores.get(uid).numProd());
				for(Integer pid : sc.getAllProd())
					System.err.println("\t\t[DEBUG] pid: " + pid + ", score:" + sc.getScore(pid));
			}
		}

		double [][] par=simulator.computePAR(allScores,bestRatings);				
		}
}
