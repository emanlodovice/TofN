
import java.util.ArrayList;

public class Polynomial {
    ArrayList<Term> terms;
    public static int ctr = 0;
    
    public Polynomial(ArrayList<Term> terms) {
        this.terms = terms;
    }
    
    public Polynomial() {
        this.terms = new ArrayList<>();
    }
    
    public Polynomial(Term t) {
        this.terms = new ArrayList<>();
        this.terms.add(t);
    }
    
    public Polynomial(String input) {
        this.terms = new ArrayList<>();        
        Term t = null;
        boolean previsTimes = false;
        int op = 1;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '-' || c == '+') {
                if (t == null) {
                    t = new Term((c == '-') ? -1 : 1);
                }   else {
                    if (previsTimes) {
                        t = t.multiply(new Term((c == '-') ? -1 : 1)).terms.get(0);
                        previsTimes = false;
                    }   else {
                        terms.add(t);
                        t = new Term((c == '-') ? -1 : 1);
                    }
                }
                previsTimes = false;
            }   else if (c == '*' || c == '/') {
                previsTimes = true;
                op = (c == '*') ? 1 : 0;
            }   else {
                if (c != ' ') {
                    previsTimes = false;
                    if (t == null) {
                        t = new Term(1);
                    }
                    Term toOp = null;
                    if (Character.isAlphabetic(c)) {
                        toOp = new Term(1, 1, c, 1);
                    }   else {
                        toOp = new Term(Integer.parseInt(c + ""));
                    }
                    if (op == 1) {
                        t = t.multiply(toOp).terms.get(0);
                    }   else {
                        t = t.divide(toOp).terms.get(0);
                    }
                }
            }
        }
        if (t != null) {
            terms.add(t);
        }        
        
    }
    
    public void addTerm(Term term) {
        this.terms.add(term.clone());
    }
    
    public void simplify() {
        ArrayList<Term> simped = new ArrayList<>();
        String added="";
        for (int i=0; i < this.terms.size(); i++) {
            if (!added.contains("-" + i + "-")) {
                Term t = this.terms.get(i);
                for (int j = i + 1; j < this.terms.size(); j++) {
                    if (!added.contains("-" + j + "-")) {
                        Term tt = this.terms.get(j);
                        if (t.variables.equals(tt.variables)) {
                            Polynomial res = t.add(tt);
                            if (res.terms.size() > 0) {
                                t = t.add(tt).terms.get(0);
                            }   else {
                                t = null;
                            }
                            added += "-" + j + "-";
                        }
                    }
                }
                if (t != null) {
                    simped.add(t);
                }
                added += "-" + i + "-";
            }
        }
        this.terms = simped;
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
                res = res.add(prod);
            }
        }
        return res;
    }
    
    public Polynomial multiply(Polynomial p, char var) {
        Polynomial res = new Polynomial();
        for (Term t1: this.terms) {
            if (!t1.hasVariable(var)) {
                for (Term t2: p.terms) {
                    Polynomial prod = t1.multiply(t2);
                    res = res.add(prod);
                }
            }   else {
                res.addTerm(t1.clone());
            }
        }
        return res;
    }
    
    public Polynomial divide(Term t) {
        Polynomial res = new Polynomial();
        if (t.num == 0) {
            return new Polynomial(new Term(0));
        }
        for (Term t1: this.terms) {            
            Polynomial prod = t1.divide(t);
            res = res.add(prod);
        }
        return res;
    }
    
    public Polynomial substitute(Polynomial p, char v) {
        Polynomial res = new Polynomial();
        for(Term t: this.terms) {
            res = res.add(t.substitute(p, v));
        }
        return res;
    }
    
    public int direction(int var) {
        int direction = 0;
        for (Term t: this.terms) {
            boolean hasThisVar = false;
            for (Variable v: t.variables) {
                if (v.getClass().getSimpleName().contentEquals("Normal")) {
                    if (((Normal)v).var == var) {
                        hasThisVar = true;
                        break;
                    }
                }
            }
            if (hasThisVar) {
                double coef = t.num / ((1.0) * t.den);
                if (coef > 1.0) {
                    direction = 1;
                    break;
                }   else if (coef < 1.0 && coef != -1.0) {
                    direction = -1;
                    break;
                }
            }   else {
                if (t.num / ((1.0) * t.den) > 0) {
                    direction = 1;
                }   else {
                    direction = -1;
                }
            }
        }
        return direction;
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
        if (num == 0 || den == 0) {
            return new Polynomial(new Term(0));
        }
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
        if (num == 0 || den == 0) {
            return new Polynomial(new Term(0));
        }
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
        Polynomial.ctr++;
        Polynomial pp = new Polynomial();
        Term t = new Term(this.num, this.den);
        pp.addTerm(t);
        for (Variable v: this.variables) {
            Polynomial temp = v.substitute(p, var);
            pp = pp.multiply(temp);
        }
        return pp;
    }
    
    public boolean hasVariable(char var) {
        for (Variable v: this.variables) {
            if (v.getClass().getSimpleName().contentEquals("Normal")) {
                if (((Normal)v).var == var) {
                    return true;
                }
            }
        }
        return false;
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
        if ((this.num != 0 || this.den != 0)) {
            if (!(this.num == 1 && this.den == 1 && this.variables.size() > 0)) {
                res = this.num + ((this.den == 1) ? "" : "/" + this.den);
            }
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
            return var + ((degree != 1) ? "^" + degree: "");
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
        Polynomial pp = new Polynomial();
        Log clone = this.clone();
        clone.ex = this.ex.substitute(p, v);
        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(clone);
        pp.addTerm(new Term(1, 1, vars));        
        return pp;
    }
}