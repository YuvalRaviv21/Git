package Git.src;

import java.io.IOException;

public class printJob {
	public static void main(String[] args) throws IOException {
		String a = "C:\\Users\\yuval\\Desktop\\.wit\\staging_area\\week1\\images";
		Integer[] t = {4,7};
		print(t);
	}
    public static void print(Object[] arr) {
        // TODO: implement your code here
    	if(arr !=null ) {
	    	int size = arr.length;
	    	String ans = "[";
	    	for(int i = 0; i < size; i++)
	    		ans = ans + arr[i].toString()+", ";
	    	System.out.println(ans.substring(0,ans.length()-2)+"]");
    	}
    }
}
