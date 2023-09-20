class Variable {
    public int numbers;
    public String letters;
    public String name;

    public Variable(String n) {
        numbers = 0;
        letters = null;
        name = n;
    }

    public int getNumbers() {
        return numbers;
    }

    public void setNumbers(int n) {
        numbers = n;
    }

    public String getLetters() {
        return letters;
    }

    public void setLetters(String l) {
        letters = l;
    }
    
    public String toString(){
      return name +  " "  + letters + " " + numbers;
    }
}