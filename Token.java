import java.io.*;
import java.util.*;

public class Token{
  boolean isString;
  String value;
  
  public Token(String v){
    value = v;
    try{
      int temp = Integer.parseInt(value);
      isString = false;
    }catch (Exception e){
      isString = true;
    }
  }
  
  public Token(String v, boolean b){
    value = v;
    isString = b;
  }
  
  public Integer getValue(){
    if (isString){
      return null;
    }
    return Integer.parseInt(value);
  }
  
  //add two tokens
  public void add(Token t){
     if (t.isString){
       if (this.isString){
         this.value = this.value + t.value;
       }else{
         this.value = this.getValue() + t.value;
       }
     }else{
       if (this.isString){
         this.value = this.value+t.getValue();
       }else{
         this.value = Integer.toString(this.getValue()+t.getValue());
       }
     }
  }
  
  //subtract two integer tokens
  public void subtract(Token t) throws Exception{
    if (t.isString || this.isString){
      throw new Exception("Cannot use this method on String literal");
    }
    this.value = Integer.toString(this.getValue()-t.getValue());
  }
  
  //multiply two integer tokens
  public void multiply(Token t) throws Exception{
    if (t.isString || this.isString){
      throw new Exception("Cannot use this method on String literal");
    }
    this.value = Integer.toString(this.getValue()*t.getValue());
  }
  
  //divide two integer tokens
  public void divide(Token t) throws Exception{
    if (t.isString || this.isString){
      throw new Exception("Cannot use this method on String literal");
    }
    this.value = Integer.toString(this.getValue()/t.getValue());
  }
  
  //modulo of two integer tokens
  public void mod(Token t) throws Exception{
    if (t.isString || this.isString){
      throw new Exception("Cannot use this method on String literal");
    }
    this.value = Integer.toString(this.getValue()%t.getValue());
  }
  
  public String toString(){
    return isString+" "+value;
  }
}