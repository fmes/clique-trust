

public class Prodotto implements Comparable<Prodotto>
{ 
  private int id;
  private double score;
 
  public Prodotto (int i, double s) 
  {
   id=i;
   score=s;
  
  } 
  
  public int getId() {
	  return id;
  }
  
  public double getScore() {
	  return score;
  }
  
  

  
@Override
public int compareTo(Prodotto o) {
	if(o.getScore()<score) return +1;
	  else
	if(o.getScore()>score) return -1;
	  return 0;
}

/*
  public int compare(Prodotto o, Prodotto o1) {
	  if(o.getScore()<o1.getScore()) return +1;
		else
		if(o.getScore()>o1.getScore()) return -1;
		return 0;
    }
*/
  }
