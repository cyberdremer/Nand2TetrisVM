import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private commandType argumentType;
    private String command;
    String argument1;
    int argument2;
    private BufferedReader br;
    private static final ArrayList<String> ArithmeticCommandsList = new ArrayList<String>();



    //Initialize a list of arithmetic commands.
    static {
        ArithmeticCommandsList.add("add");
        ArithmeticCommandsList.add("sub");
        ArithmeticCommandsList.add("neg");
        ArithmeticCommandsList.add("eq");
        ArithmeticCommandsList.add("gt");
        ArithmeticCommandsList.add("lt");
        ArithmeticCommandsList.add("and");
        ArithmeticCommandsList.add("or");
        ArithmeticCommandsList.add("not");


    }


    //Open a file, throw a FileNotFound exception if the file is not found.
    Parser(String filename) throws FileNotFoundException{
        argumentType = null;
        argument2 = -1;
        argument1 = "";

        try{
            br = new BufferedReader(new FileReader(filename));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }





    //March through the file and find a command, ignores comments and whitespace.
    public void advance() throws IOException{
        String readLine;
        while (true){
            readLine = br.readLine();
            if(readLine == null){
                try {
                    if(br != null){
                        br.close();
                        command = null;
                    }
                }
                catch (IOException ex){
                    ex.printStackTrace();
                }
                finally {
                    break;
                }

            }
            readLine = readLine.replaceAll("//.*","");
            if (readLine.length() !=0 && !readLine.trim().equals("") && !readLine.trim().equals("\n") ){
                command = readLine;
                break;
            }
        }
    }





    //Determine the commandType of the read line, sets the appropriate args and stores the command.
    //Done through tokenizing.

    public commandType commandType(){
        String[] args = command.split(" ");
        if (ArithmeticCommandsList.contains(args[0])){
            argumentType = commandType.C_ARITHMETIC;
            argument1 = args[0];
        } else if (args[0].equals("return")) {
            argumentType = commandType.C_RETURN;
            argument1 = args[0];

        }
        else{
            argument1 = args[1];
            if(args[0].equals("push")){
                argumentType = commandType.C_PUSH;
                String tempargs2 = args[2].trim();
                argument2 = Integer.parseInt(tempargs2);
                
            } else if (args[0].equals("pop")) {
                argumentType = commandType.C_POP;
                String tempargs2 = args[2].trim();
                argument2 = Integer.parseInt(tempargs2);

                
            } else if (args[0].equals("label")) {
                argumentType = commandType.C_LABEL;
                
                
            } else if (args[0].equals("if-goto")) {
                argumentType = commandType.C_IF;
                
            } else if (args[0].equals("goto")) {
                argumentType = commandType.C_GOTO;
                
            } else if (args[0].equals("function")) {
                argumentType = commandType.C_FUNCTION;
                String tempargs2 = args[2].trim();
                argument2 = Integer.parseInt(tempargs2);

            } else if (args[0].equals("call")) {
                argumentType = commandType.C_CALL;
                String tempargs2 = args[2].trim();
                argument2 = Integer.parseInt(tempargs2);

            } else {
                throw  new IllegalArgumentException("Unknown Command");
            }
        }
        return argumentType;



    }

    public String getCommand(){
        return command;
    }


    public String getArgument1(){
        return argument1;

    }

    public int getArgument2(){
        return argument2;
    }
}
