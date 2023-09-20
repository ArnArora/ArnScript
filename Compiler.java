import java.util.*;
import java.io.*;

public class Compiler{
  //keep track of variables
  public static ArrayList<Variable> vars = new ArrayList<>();
  public static ArrayList<Variable> loopVars = new ArrayList<>();
  public static boolean checkFalse;
  public static int line = 0;
  public static void main(String[] args) throws Exception{
    //read in file
    try{
      File programText = new File("sampleArnScript.txt");
      Scanner fin = new Scanner(programText);
      /*read the first word and go to appropriate function for each
       * for some methods, evaluate each parameter first
     */
      checkFalse = false;
      //a default variable to make it easier
      vars.add(new Variable("space", new Token(" ", true)));
      while (fin.hasNextLine()){
        //keep track of line number
        line++;
        //action represents the keyword for that line
        String action = fin.next();
        if (action.equals("")){
          fin.nextLine();
          continue;
        }
        //check if comment line
        if (action.length()>2 && action.substring(0,2).equals("**")){
          fin.nextLine();
          continue;
        }
        //check if conditional statement has ended
        if (checkFalse){
          if (action.equals("end")){
            checkFalse = false;
          }else{
            fin.nextLine();
          }
          continue;
        }
        String remaining = fin.nextLine().trim();
        //pass in action, the expression after the keyword, and whether it is currently a conditional statement
        perform(action, remaining, checkFalse);
      }
    }catch (FileNotFoundException e){
      throw new Exception("Cannot find program file");
    }
  }
  
  //takes in a start keyword and the rest of the line
  //performs action accordingly to keyword
  public static void perform(String action, String remaining, boolean checkFalse) throws Exception{
    //declaring an integer variable
    if (action.equals("decInt")){
          String varName = getNext(remaining);
          remaining = updateInput(remaining);
          if (!(getNext(remaining).equals("->"))){
            throw new Exception("Use -> symbol to assign value at line "+line);
          }
          remaining = updateInput(remaining);
          Token value = simplify(remaining);
          if (value.isString){
            throw new Exception("Must be an integer variable at line "+line);
          }
          vars.add(new Variable(varName, value));
        }else if (action.equals("decStr")){//declaring a string variable
          String varName = getNext(remaining);
          remaining = updateInput(remaining);
          if (!(getNext(remaining).equals("->"))){
            throw new Exception("Use -> symbol to assign value at line "+line);
          }
          remaining = updateInput(remaining);
          Token value = simplify(remaining);
          if (!value.isString){
            throw new Exception("Must be a String variable at line "+line);
          }
          vars.add(new Variable(varName, value));
        }else if (action.equals("changeInt")){//changing an integer variable
          String varName = getNext(remaining);
          remaining = updateInput(remaining);
          if (!(getNext(remaining).equals("->"))){
            throw new Exception("Use -> symbol to assign value at line "+line);
          }
          remaining = updateInput(remaining);
          Token value = simplify(remaining);
          if (value.isString){
            throw new Exception("Must be an integer value for reassignment at line "+line);
          }
          for (Variable var: vars){
            if (var.name.equals(varName)){
              var.token = value;
            }
          }
        }else if (action.equals("changeStr")){//changing a string variable
          String varName = getNext(remaining);
          remaining = updateInput(remaining);
          if (!(getNext(remaining).equals("->"))){
            throw new Exception("Use -> symbol to assign value at line "+line);
          }
          remaining = updateInput(remaining);
          Token value = simplify(remaining);
          if (!value.isString){
            throw new Exception("Must be a String value for reassignment at line "+line);
          }
          for (Variable var: vars){
            if (var.name.equals(varName)){
              var.token = value;
            }
          }
        }else if (action.equals("display")){//print
          System.out.println(simplify(remaining).value);
        }else if (action.equals("check")){//if statement
          boolean isValid = checkConditional(remaining);
          if (!isValid){
            checkFalse = true;
          }
        }else if (action.equals("repeat")){//for loop
          try{
            String loopVar = getNext(remaining);
            remaining = updateInput(remaining);
            int startNum = Integer.parseInt(getNext(remaining));
            remaining = updateInput(remaining);
            int loopNum = Integer.parseInt(getNext(remaining));
            remaining = updateInput(remaining);
            if (!getNext(remaining).equals("->")){
              throw new Exception();
            }
            //keep track of variable for loop
            remaining = updateInput(remaining);
            String act = getNext(remaining);
            remaining = updateInput(remaining);
            setValue(loopVar, "0");
            for (int i = startNum; i<loopNum; i++){
              setValue(loopVar, String.valueOf(i));
              perform(act, remaining, checkFalse);
            }
            loopVars.clear();
          }catch (Exception e){
            throw new Exception("Does not follow syntax for repeat loop at line "+line);
          }
        }else if (action.equals("end")){
        }else{//it is an evaluation function   
          simplify(action+" "+remaining);
        }
  }
  
  //checks if conditional is true
  public static boolean checkConditional(String input) throws Exception{
    int index1 = input.indexOf(">=");
    int index2 = input.indexOf("<=");
    //find symbol and its index
    int finalIndex;
    String symbol;
    if (index1==-1 && index2==-1){
      int index3 = input.indexOf(">");
      int index4 = input.indexOf("<");
      if (index3>-1){
        finalIndex = index3;
        symbol = ">";
      }else if (index4>-1){
        finalIndex = index4;
        symbol = "<";
      }else{
        symbol = "";
        finalIndex = 0;
      }
    }else{
      if (index1>-1){
        finalIndex = index1;
        symbol = ">=";
      }else{
        finalIndex = index2;
        symbol = "<=";
      }
    }
    //break into three parts and compare the end parts
    String left = input.substring(0, finalIndex).trim();
    String right = input.substring(finalIndex+symbol.length()).trim();
    if (left.equals("") || symbol.equals("") || right.equals("")){
      throw new Exception("Must be a proper conditional statement (comparison statement) at line "+line);
    }
    Token l = simplify(left);
    Token r = simplify(right);
    int comp;
    if (!(l.isString || r.isString)){
      comp = l.getValue()-r.getValue();
    }else{
      comp = l.value.compareTo(r.value);
    }
    if (symbol.equals(">")){
      return comp>0;
    }else if (symbol.equals("<")){
      return comp<0;
    }else if (symbol.equals(">=")){
      return comp>=0;
    }else{
      return comp<=0;
    }
  }
  
  //takes an expression and simplifies into one token
  public static Token simplify(String input) throws Exception{
    Token result = null;
    try{
      Token start = new Token(getNext(input));
      input = updateInput(input);
      //lit -> turn into quote
      //readInt -> turn into integer
      //readStr -> turn into quote
      //variable -> turn into whatever value
      //then take each of these resulting tokens and pass into evaluate function
      if (start.value.equals("lit")){
        String literal = getNext(input);
        result = new Token(literal.substring(1, literal.length()-1), true);
        input = updateInput(input);
        result = evaluate(input, result);
      }else if (start.value.equals("readInt")){
        Scanner newScanner = new Scanner(System.in);
        start = new Token(newScanner.nextLine(), false);
        newScanner.close();
        result = evaluate(input, start);
      }else if (start.value.equals("readStr")){
        Scanner newScanner = new Scanner(System.in);
        start = new Token(newScanner.nextLine(), true);
        newScanner.close();
        result = evaluate(input, start);
      }else if (start.isString){
        start = getValue(start.value);
        result = evaluate(input, start);
      }else{
        result = evaluate(input, start);
      }
    }catch (Exception e){
      throw new Exception("The expression cannot be simplified at line "+line);
    }
    return result;
  }
  
  //helper for simplify, able to recurcsively call
  public static Token evaluate(String input, Token val) throws Exception{
    String operation = getNext(input);
    //base case
    if (operation.length()==0){
      return val;
    }
    input = updateInput(input);
    Token number = new Token(getNext(input));
    input = updateInput(input);
    if (number.value.equals("")){
      throw new Exception("There must be two values for the operator at line "+line);
    }
    //same thing as simplify function
    if (number.isString){
      if (number.value.equals("lit")){
        String literal = getNext(input);
        if (literal.equals("")){
          throw new Exception("Must have something in parentheses after literal at line "+line);
        }
        number.value = literal.substring(1, literal.length()-1);
        input = updateInput(input);
      }else if (number.value.equals("readInt")){
        Scanner newScanner = new Scanner(System.in);
        number = new Token(newScanner.nextLine(), false);
        newScanner.close();
      }else if (number.value.equals("readStr")){
        Scanner newScanner = new Scanner(System.in);
        number = new Token(newScanner.nextLine(), true);
        newScanner.close();
      }else{
        number = getValue(number.value);
      }
    }
    //check which operation and simplify
    if (operation.equals("+")){
      val.add(number);
    }else if (operation.equals("-")){
      val.subtract(number);
    }else if (operation.equals("*")){
      val.multiply(number);
    }else if (operation.equals("/")){
      val.divide(number);
    }else if (operation.equals("%")){
      val.mod(number);
    }else{
       throw new Exception("Not a valid operation (must be +, -, *, /, or %) at line "+line);
    }
    return evaluate(input, val);
  }
  
  //set value for repeat loop
  public static void setValue(String name, String value){
    boolean found = false;
    for (int i = 0; i<loopVars.size(); i++){
        if (loopVars.get(i).name.equals(name)){
          loopVars.get(i).token = new Token(value, false);
          found = true;
        }
      }
    if (!found){
      loopVars.add(new Variable(name, new Token(value, false)));
    }
  }
  //check if variable exists
  public static Token getValue(String name) throws Exception{
    for (Variable var: vars){
        if (var.name.equals(name)){
          return var.token;
        }
      }
    for (Variable var: loopVars){
        if (var.name.equals(name)){
          return var.token;
        }
      }
    throw new Exception("No such variable with name "+name+ "at line "+line);
  }
  
  //keep track of what has been read
  public static String updateInput(String input){
    return input.substring(nextIndex(input)).trim();
  }
  
  //find index of next space in program (to find next word)
  public static int nextIndex(String input){
    int index = input.indexOf(" ");
    if (index==-1){
      return input.length();
    }
    return index;
  }
  
  //get next word in program
  public static String getNext(String input){
    int space = nextIndex(input);
    return input.substring(0, space);
  }
}