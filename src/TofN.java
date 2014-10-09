import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TofN {
	public static void main(String[] args) {
                System.out.println(Util.count_op("i=i-2"));
		String input = Reader.read("input.in");		
		Computer c = new Computer(input);
		c.exec();                
	}
}

class Computer {
	private String input;
	ArrayList<Loop> loops;
	public Computer(String input) {
		this.input = input;
	}
	
	public void exec() {
		loops = new ArrayList<Loop>();
		int ctr = 1;
		while (this.input.length() > 0) {			
			loops.add(tokenize());
			System.out.println(ctr + ") " + loops.get(loops.size() - 1));
			ctr++;
		}
	}
	
	private Loop tokenize() {
		Loop loop = new Loop();
		int index = input.indexOf("for");
		if (index >= 0) {
			String status = "LOOP_CON";
			input = input.substring(index + 3);
			int index_open_paren = input.indexOf("(");
			int index_close_paren = input.indexOf(")");
			String[] loop_params = input.substring(index_open_paren + 1, index_close_paren).split(";");
			if (loop_params.length > 0) {
				loop.dec = loop_params[0].trim();
                                loop.operations.add(loop_params[0].trim());
				loop.con = loop_params[1].trim();
                                loop.operations.add(loop_params[1].trim());
				String[] ops = loop_params[2].split(",");
                                for (String op: ops) {
                                    op = op.trim();
                                    if (op.length() > 0) {
                                        loop.operations.add(op);
                                        if ((loop.con.contains(op.charAt(0)+"") && Character.isAlphabetic(op.charAt(0))) || (loop.con.contains(op.charAt(op.length() - 1) + "") && Character.isAlphabetic(op.charAt(op.length() - 1)) && (op.indexOf("++") >= 0 || op.indexOf("--") >= 0))) {
                                            loop.inc = op;
                                        }
                                    }                                    
                                }
                                
			}
			input = input.substring(index_close_paren + 1).trim();
			if (input.charAt(0) == '{') {
				input = input.substring(1).trim();
				while(true) {
					if (input.indexOf("for") == 0) {
						loop.innerLoops.add(tokenize());
					}	else if (input.indexOf("}") == 0) {
						input = input.substring(1).trim();
						break;
					}	else {
						int semi_colon_ind = input.indexOf(";");
						loop.operations.add(input.substring(0, semi_colon_ind));
						input = input.substring(semi_colon_ind + 1).trim();
					}
				}
			}	else if (input.charAt(0) == ';') {
				try {
					input = input.substring(1).trim();
				}	catch(Exception e) {
					input = "";
				}
			}	else {
				int semi_colon_ind = input.indexOf(";");
				loop.operations.add(input.substring(0, semi_colon_ind));
				try {
					input = input.substring(semi_colon_ind + 1).trim();
				} catch(Exception e) {
					input = "";
				}
			}
		}
		return loop;
	}
}

class Loop {
	String dec = "";
	String con = "";
	String inc = "";
	ArrayList<String> operations = new ArrayList<String>();
	ArrayList<Loop> innerLoops = new ArrayList<Loop>();
	
	@Override
	public String toString() {
		return "Declaration: " + dec + ", Condition: " + con + ", Increment: " + inc + ", Operations: " + operations.toString() + ", NestedLoops: " + innerLoops.toString(); 
	}
}


class Util {
    public static final String operators = "+-*/=<>";
    
    public static int count_op(String in) {
        int count = 0;
        int prev_ind = -2;
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (operators.contains(c + "")) {
                if (prev_ind + 1 != i) {
                    count++;                    
                }
                prev_ind = i;
            }
        }
        return count;
    }
}

        
class Reader {
    public static String read(String filename) {
            String res = "";
            BufferedReader reader = null;
            try {
                    reader = new BufferedReader(new FileReader(new File(filename)));		
                    String line = reader.readLine();
                    while (line != null) {
                            res += line;
                            line = reader.readLine();
                    }
            } catch (IOException e) {
                    e.printStackTrace();
            }
            return res;
    }
}