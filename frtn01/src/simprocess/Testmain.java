package simprocess;

public class Testmain {
	
	
	public static void main(String args[]){
		String a = "ab cd ef gh";
		String[] b = a.split(" ");
		for(int i = 0; i < 4; i++){
			System.out.println(b[i]);
		}
		String c = "4.12 5.18";
		String[] d = c.split(" ");
		for(int i = 0; i < 2; i++){
			System.out.println(d[i]);
			System.out.println(Double.parseDouble(d[i]));
		}
		
		double g = 4.1231;
		
	System.out.println(Double.toString(4.123123) + " " + Double.toString(-7.231212) + " \n");
	}
}