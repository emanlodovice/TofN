
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author eman
 */
public class Polynomial {
    public static void main(String[] args) throws CloneNotSupportedException {
        Normal a = new Normal('a', 1);
        Normal b = new Normal('a', 1);
        Log log = new Log(1, null, 1);
        Variable v = log;
        Object c = b;
        System.out.println(c.getClass());
        Term aa = new Term(2, 1, 'x', 2);
        Term bb = new Term(-1, 1, 'x', 2);
//        Term aa = new Term(2, 1);
//        Term bb = new Term(-1, 1);
        ArrayList<Variable> withLog = new ArrayList<Variable>();
        withLog.add(new Log(1, aa.add(bb), 1));
        Term cc = new Term(1, 1, withLog);
        System.out.println(aa.add(bb) + " add");
        System.out.println(aa.multiply(bb) + " multiply");
        System.out.println(aa.divide(bb) + " divide");
        Term x = new Term(1, 1, 'x', 1);
        ArrayList<Term> ts = new ArrayList<>();
        ts.add(x);
        Polynomial p = new Polynomial(ts);
        System.out.println(p.substitute(aa.divide(bb), 'x') + " substitute");
        
    }
    
    ArrayList<Term> terms;
    public static int ctr = 0;
    
    public Polynomial(ArrayList<Term> terms) {
        this.terms = terms;
    }
    
    public Polynomial() {
        this.terms = new ArrayList<>();
    }
    
    public void addTerm(Term term) {
        this.terms.add(term.clone());
    }
    
    public void simplify() {
        
    }
    
    public Polynomial add(Polynomial p) {
        Polynomial res = this.clone();
        for(Term t: p.terms) {
            res.addTerm(t);
        }
        res.simplify();
        return res;
    }
    
    public Polynomial subtract(Polynomial p) {
        Polynomial neg = new Polynomial();
        neg.addTerm(new Term(-1));
        Polynomial self = this.clone();
        return self.add(p.multiply(neg));
    }
    
    public Polynomial multiply(Polynomial p) {
        Polynomial res = new Polynomial();
        for (Term t1: this.terms) {
            for (Term t2: p.terms) {
                Polynomial prod = t1.multiply(t2);
                System.out.println(prod + " prod");
                res = res.add(prod);
            }
        }
        return res;
    }
    
    public Polynomial divide(Polynomial p) {
        return null;
    }
    
    public Polynomial substitute(Polynomial p, char v) {
        System.out.println(this + " this");
        System.out.println(p + " p");
        Polynomial res = new Polynomial();
        for(Term t: this.terms) {
            System.out.println("Substitute term");
            res = res.add(t.substitute(p, v));
        }
        return res;
    }
    
    
    public Polynomial clone() {
        ArrayList<Term> cloneTerms = new ArrayList<>();
        for (Term t: this.terms) {
            cloneTerms.add(t.clone());
        }
        return new Polynomial(cloneTerms);
    }
    
    @Override
    public String toString() {
        if (terms.isEmpty()) {
            return "0";
        }
        String res = "";
        for (int i = 0; i < terms.size(); i++) {
            res += terms.get(i).toString();
            if (i + 1 != terms.size()) {
                res += " + ";
            }
        }
        return res;
    }
}

class Term {
    int num;
    int den;
    ArrayList<Variable> variables;
    
    public Term(int num, int den, ArrayList<Variable> variables) {
        init(num, den, variables);
    }
    
    public Term(int num, int den, char var, int degree) {
        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(new Normal(var, degree));
        init(num, den, vars);
    }
    
    public Term(int num) {
        this.init(num, 1, new ArrayList<>());
    }
    
    public Term(int num, int den) {
        this.init(num, den, new ArrayList<>());
    }
    
    private void init(int num, int den, ArrayList<Variable> variables) {
        this.num = num;
        this.den = den;
        this.variables = (ArrayList<Variable>)variables.clone();
    }
    
    public Polynomial add(Term t) {
        Polynomial p = new Polynomial();
        if (!this.variables.equals(t.variables)) {
            p.addTerm(this.clone());
            p.addTerm(t.clone());
        }   else {
            int num = (this.num * t.den) + (t.num * this.den);
            int den = this.den * t.den;
            if (num == 0) {
                return p;
            }
            int gcf = Term.gcf(num, den);
            num /= gcf;
            den /= gcf;
            p.addTerm(new Term(num, den, this.cloneVariables()));
        }
        return p;
    }
    
    public Polynomial multiply(Term t) {
        Polynomial p = new Polynomial();
        int num = this.num * t.num;
        int den = this.den * t.den;
        int gcf = Term.gcf(num, den);
        num /= gcf;
        den /= gcf;
        if ((num < 0 && den < 0) || (num > 0 && den < 0)) {
            num *= -1;
            den *= -1;
        }
        ArrayList<Variable> vars1 = this.cloneVariables();
        ArrayList<Variable> vars2 = t.cloneVariables();
        for (Variable var1: vars1) {
            boolean wasAdded = false;
            for (Variable var2: vars2) {
                if (var2.getClass() == var1.getClass()) {
                    if ((var1 instanceof Normal)) {
                        Normal v1 = (Normal) var1;
                        Normal v2 = (Normal) var2;
                        if (v1.var == v2.var) {
                            v2.degree += v1.degree;
                            wasAdded = true;
                            break;
                        }
                    }   else {
                        Log v1 = (Log) var1;
                        Log v2 = (Log) var2;
                        if (v1.base == v2.base && v1.ex.equals(v2.ex)) {
                            v2.degree += v1.degree;
                            wasAdded = true;
                            break;
                        }
                    }
                }
            }
            if (!wasAdded) {
                vars2.add(var1);
            }
        }
        p.addTerm(new Term(num, den, vars2));
        return p;
    }
    
    public Polynomial divide(Term t) {
        Polynomial p = new Polynomial();
        int num = this.num * t.den;
        int den = this.den * t.num;
        int gcf = Term.gcf(num, den);
        num /= gcf;
        den /= gcf;
        if ((num < 0 && den < 0) || (num > 0 && den < 0)) {
            num *= -1;
            den *= -1;
        }
        ArrayList<Variable> vars1 = this.cloneVariables();
        ArrayList<Variable> vars2 = t.cloneVariables();
        for (Variable var2: vars2) {            
            boolean wasAdded = false;
            for (Variable var1: vars1) {
                if (var2 instanceof Normal) {
                    ((Normal) var2).degree *= -1;
                }   else {
                    ((Log) var2).degree *= -1;
                }
                if (var2.getClass() == var1.getClass()) {
                    if ((var1 instanceof Normal)) {
                        Normal v1 = (Normal) var1;
                        Normal v2 = (Normal) var2;
                        if (v1.var == v2.var) {
                            v1.degree += v2.degree;
                            wasAdded = true;
                            break;
                        }
                    }   else {
                        Log v1 = (Log) var1;
                        Log v2 = (Log) var2;
                        if (v1.base == v2.base && v1.ex.equals(v2.ex)) {
                            v1.degree += v2.degree;
                            wasAdded = true;
                            break;
                        }
                    }
                }
            }
            if (!wasAdded) {
                vars1.add(var2);
            }
        }
        p.addTerm(new Term(num, den, vars1));
        return p;
    }
    
    public Polynomial substitute(Polynomial p, char var) {
        System.out.println(Polynomial.ctr + " ctr");
        Polynomial.ctr++;
        Polynomial pp = new Polynomial();
        Term t = new Term(this.num, this.den);
        System.out.println(t.num + " t " + t.den);
        pp.addTerm(t);        
        System.out.println(pp.terms + " here");
        for (Variable v: this.variables) {
            Polynomial temp = v.substitute(p, var); 
            System.out.println(pp + " pp");
            System.out.println(temp + " temp");
            pp = pp.multiply(temp);
            System.out.println(pp + " ppp");
        }
        System.out.println(pp + " pp res");
        return pp;
    }
    
    public static int gcf(int num, int num2) {        
        int min = Math.abs(Math.min(num, num2));
        while(true) {
            if (num % min == 0 && num2 % min == 0) {
                break;
            }   else {
                min--;
            }
        }
        return min;
    }
    
    @Override
    public String toString() {
        String res = "";
        if (this.num != 0 || this.den != 0) {
            res = this.num + ((this.den == 1) ? "" : "/" + this.den);
            for (Variable var: this.variables) {
                res += var;
            }
        }
        return res;
    }
    
    @Override
    public Term clone() {
        ArrayList<Variable> a = new ArrayList<>();
        for (Variable v: variables) {
            a.add(v.clone());
        }
        
        Term clone = new Term(this.num, this.den, variables);
        return clone;
    }
    
    public ArrayList<Variable> cloneVariables() {
        ArrayList<Variable> vars = new ArrayList<>();
        for (Variable var: this.variables) {
            vars.add(var.clone());
        }
        return vars;
    }
}

interface Variable {
    @Override
    public boolean equals(Object o);
    
    public Variable clone();
    
    public Polynomial substitute(Polynomial p, char v);
    
}

class Normal implements Variable {
    char var;
    int degree;
    
    public Normal(char var, int degree) {
        this.var = var;
        this.degree = degree;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Normal) {
            Normal oo = (Normal) o;
            return (oo.var == this.var) && (oo.degree == this.degree);
        }
        return false;
    }
    
    @Override
    public String toString() {
        if (degree != 0) {
            return var + "^" + degree;
        }
        return "";
    }
    
    @Override
    public Normal clone() {
        return new Normal(var, degree);
    }
    
    @Override
    public Polynomial substitute(Polynomial p, char v) {
        Polynomial res = new Polynomial();
        if (v != this.var) {
            res.addTerm(new Term(1, 1, this.var, this.degree));
        }   else {
            res = p.clone();
            for (int i = 1; i < this.degree; i++) {
                res = res.multiply(p);
            }
        }
        return res;
    }
}

class Log implements Variable {
    int base;
    Polynomial ex;
    int degree;
    
    public Log(int base, Polynomial ex, int degree) {
        this.base = base;
        this.ex = ex;
        this.degree = degree;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Log) {
            Log oo = (Log) o;
            return (oo.base == this.base) && (oo.ex.equals(this.ex)) && (oo.degree == this.degree);
        }
        return false;
    }
    
    @Override
    public String toString() {
        String res = "";
        res += "log" + base + "(" + ex.toString() + ")" + ((degree != 1) ? "^" + degree : ""); 
        return res;
    }
    
    @Override
    public Log clone() {
        return new Log(base, ex.clone(), degree);
    }

    @Override
    public Polynomial substitute(Polynomial p, char v) {
        System.out.println(p + " Log");
        Polynomial pp = new Polynomial();
        Log clone = this.clone();
        clone.ex = this.ex.substitute(p, v);
        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(clone);
        pp.addTerm(new Term(1, 1, vars));        
        return pp;
    }
}