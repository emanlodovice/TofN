import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Lodovice3 {
	public static void main(String[] args) {
		String input = Reader.read("input1.c");		
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
                    loops.add(this.tokenize());
                    Loop cur =  loops.get(loops.size() - 1);
                    System.out.print(ctr + ") ");
                    Polynomial runningTime = cur.runningTime();
                    if (runningTime != null) {
                        runningTime.simplify();
                        System.out.println("T(n) = " + runningTime);
                    }
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
				loop.con = loop_params[1].trim();
                                if (loop.con.length() > 0) {
                                    loop.nonRepOps.add(loop.con);             
                                    loop.operations.add(loop.con);
                                }
				String[] ops = loop_params[2].split(",");
                                for (String op: ops) {
                                    op = op.trim();
                                    if (op.length() > 0) {
                                        loop.operations.add(op);
                                        if ((loop.con.contains(op.charAt(0)+"") && Character.isAlphabetic(op.charAt(0))) || (loop.con.contains(op.charAt(op.length() - 1) + "") && Character.isAlphabetic(op.charAt(op.length() - 1)) && (op.indexOf("++") >= 0 || op.indexOf("--") >= 0))) {
                                            loop.inc = op;
                                            if (Character.isAlphabetic(op.charAt(0))) {
                                                loop.iterator = op.charAt(0);
                                            }   else {
                                                loop.iterator = op.charAt(op.length() - 1);
                                            }
                                        }
                                    }                                    
                                }
                                String[] decs = loop_params[0].trim().split(",");
                                for (String op: decs) {
                                    op = op.trim();
                                    if (op.length() > 0) {
                                        loop.nonRepOps.add(op);
                                        String toTest = op.replace(" ", "");
                                        int equalIndex = toTest.indexOf("=");
                                        if (equalIndex > 0) {
                                            if (loop.inc.contains(toTest.charAt(equalIndex - 1)+"") && Character.isAlphabetic(toTest.charAt(equalIndex - 1))) {
                                                loop.dec = op;
                                            }
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
        char iterator = 'i';
        String conOperator = "";
        ArrayList<String> nonRepOps = new ArrayList<String>();
	ArrayList<String> operations = new ArrayList<String>();
	ArrayList<Loop> innerLoops = new ArrayList<Loop>();
	
	@Override
	public String toString() {
            return "Declaration: " + dec + ", Condition: " + con + ", Increment: " + inc + ", Non-repeated operations: " + nonRepOps + ", Operations: " + operations.toString() + ", NestedLoops: " + innerLoops.toString(); 
	}
        
        public Polynomial update() {
            String update = this.inc;
            Polynomial p = null;
            if (update.contains("++")) {
                p = new Polynomial(iterator + " + 1");
            }   else if (update.contains("--")) {
                p = new Polynomial(iterator + " - 1");
            }   else {
                int equalIndex = update.indexOf("=");
                if (equalIndex > 0) {
                    p = new Polynomial(update.substring(equalIndex + 1));
                    char beforeEqual = update.charAt(equalIndex - 1);
                    Polynomial toOp = new Polynomial();
                    toOp.addTerm(new Term(1, 1, iterator, 1));                    
                    if (beforeEqual == '/') {
                        p.simplify();
                        Polynomial temp = p;
                        p = toOp.divide(temp.terms.get(0));
                    }   else if (beforeEqual == '*') {
                        p = p.multiply(toOp);
                    }   else if (beforeEqual == '+') {
                        p = p.add(toOp);
                    }   else if (beforeEqual == '-') {
                        Polynomial temp = p;
                        p = toOp.subtract(temp);
                    }
                }
            }
            if (p != null) {
                p.simplify();
            }            
            return p;
        }
        
        public Polynomial initialization() {
            Polynomial init = null;
            
            int equalIndex = this.dec.indexOf("=");
            if (equalIndex > 0) {
                init = new Polynomial(dec.substring(equalIndex + 1));
            }
            if (init != null) {
                init.simplify();
            }
            return init;
        }
        
        public Polynomial codition () {
            Polynomial p = null;
            if(this.con.length() > 0) {
                int index = this.con.lastIndexOf("=");
                int startOpIndex = index;
                if (index > 0) {
                    char c = this.con.charAt(index - 1);
                    if (c == '!') {
                        this.conOperator = "!=";
                        startOpIndex--;
                    }   else if (c == '=') {
                        this.conOperator = "==";
                        startOpIndex--;
                    }   else if (c == '>') {
                        this.conOperator = ">=";
                        startOpIndex--;
                    }   else if (c == '<') {
                        this.conOperator = "<=";
                        startOpIndex--;        
                    }
                }   else {
                    int lessIndex = this.con.indexOf("<");
                    if (lessIndex > 0) {
                        startOpIndex = lessIndex;
                        this.conOperator = "<";
                    } else {
                        startOpIndex = this.con.indexOf(">");
                        this.conOperator = ">";
                    }
                }                

                String leftString = this.con.substring(0, startOpIndex);
                String rightString = this.con.substring(startOpIndex + this.conOperator.length());

                if (rightString.contains(this.iterator + "")) {
                    String temp = leftString;
                    leftString = rightString;
                    rightString = temp;
                    this.reverseCon();
                }

                Polynomial left = new Polynomial(leftString);
                left.simplify();
                Polynomial right = new Polynomial(rightString);
                right.simplify();

                for (Term t: left.terms) {
                    if (!t.hasVariable(this.iterator)) {
                        Polynomial toTranspose = new Polynomial(t);
                        right = right.subtract(toTranspose);
                        left = left.subtract(toTranspose);
                    }
                }

                for (Term t: left.terms) {
                    if (t.hasVariable(this.iterator)) {
                        Term toDivide = new Term(t.num, t.den);
                        if (t.num < 0) {
                            this.reverseCon();
                        }
                        right = right.divide(toDivide);
                        left = left.divide(toDivide);
                    }
                }

                p = right;
            }
            return p;
        }
        
        public Polynomial upperBound() {            
            Polynomial cond = this.codition();
            if ((this.conOperator.contains(">") && this.update().direction(this.iterator) == -1) || (this.conOperator.contains("<") && this.update().direction(this.iterator) == 1) || (this.conOperator.contentEquals("!="))) {
                Polynomial p = null;
                if (this.conOperator.contains(">")) {
                    p = this.initialization();
                }   else {
                    p = cond;
                }
                Polynomial update = this.update();
                for (Term t: update.terms) {
                    if (t.hasVariable(this.iterator)) {
                        double coef = t.num / (double)t.den;
                        if (coef > 1.0) {
                            Log log = new Log((int) coef, p, 1);
                            ArrayList<Variable> vars = new ArrayList<>();
                            vars.add(log);
                            p = new Polynomial(new Term(1, 1, vars));
                        }   else if (coef < 1.0) {
                            coef = t.den / (double)t.num;
                            Log log = new Log((int) coef, p, 1);
                            ArrayList<Variable> vars = new ArrayList<>();
                            vars.add(log);
                            p = new Polynomial(new Term(1, 1, vars));
                        }
                    }
                }
                
                for (Term t: update.terms) {
                    if (!t.hasVariable(this.iterator)) {                        
                        p = p.divide(new Term(Math.abs((int)(t.num / t.den))));
                    }
                }
                if (this.conOperator.contentEquals("<") || this.conOperator.contentEquals(">")) {                    
                    p.addTerm(new Term(-1));
                    p.simplify();
                }
                return p;
            } else {
                return null;
            }
        }
        
        public Polynomial lowerBound() {
            Polynomial p = null;
            if (this.conOperator.contains(">")) {
                p = this.codition();
            }   else {
                p = this.initialization();
            }
            return p;
        }
        
        public Polynomial runningTime() {
            if (this.con.length() == 0 || this.inc.length() == 0 || this.dec.length() == 0) {
                System.out.println("Infinite loop here");
                return null;
            }
            int nonRepeatingStatementsTime = Util.count_operations(this.nonRepOps);
            Polynomial nonRepeating = new Polynomial(new Term(nonRepeatingStatementsTime));
            int repeatingStatementsTime = Util.count_operations(this.operations);
            Polynomial runningTime = new Polynomial(new Term(repeatingStatementsTime));
            for(Loop l: this.innerLoops) {
                Polynomial run = l.runningTime();
                if (run == null) {
                    return null;
                }
                runningTime = runningTime.add(run);
            }
                
            Polynomial upper = this.upperBound();            
            Polynomial lower = this.lowerBound();
            if (upper == null) {
                if (this.conOperator.contentEquals("==")) {
                    try {
                        int low = Integer.parseInt(lower.toString());
                        int high = Integer.parseInt(this.codition().toString());
                        if (low != high) {
                            return nonRepeating;
                        }
                    }   catch(Exception e){}
                    return runningTime.add(nonRepeating);
                }   else {
                    System.out.println("Infinite loop there");
                    return null;
                }   
            }
            try {
                int low = Integer.parseInt(lower.toString());
                if (low != 1) {
                    int def = 1 - low;
                    lower = new Polynomial(new Term(1));
                    Polynomial toAdd = new Polynomial(new Term(def));
                    upper = upper.add(toAdd);
                }
            }   catch(Exception e) {}
            
            try {
                int low = Integer.parseInt(lower.toString());
                int high = Integer.parseInt(upper.toString());
                if (high < low) {
                    return nonRepeating;
                }
            }   catch(Exception e) {}
            Polynomial toMul = upper.clone();
            toMul = toMul.subtract(lower);
            toMul = toMul.add(new Polynomial(new Term(1)));
            
            runningTime = runningTime.multiply(toMul, this.iterator);
            Polynomial toSub = upper.clone();
            toSub = toSub.multiply(upper.add(new Polynomial(new Term(1))));
            toSub = toSub.divide(new Term(2));
            runningTime = runningTime.add(nonRepeating);
            runningTime = runningTime.substitute(toSub, this.iterator);
            return runningTime;
        }
        
        private void reverseCon() {
            if (this.conOperator.contains(">")) {
                this.conOperator = this.conOperator.replaceAll(">", "<");
            }   else {
                this.conOperator = this.conOperator.replaceAll("<", ">");
            }
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
    
    public static int count_operations(ArrayList<String> code) {
        int count = 0;
        for (String s: code) {
            count += Util.count_op(s);
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