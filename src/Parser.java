import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private commandType argumentType;
    private String command;
    private BufferedReader br;
    Parser(String filename) throws FileNotFoundException{
        try{
            br = new BufferedReader(new FileReader(filename));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }




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
                    ex.printStackTrace();                }

            }
            readLine = readLine.replaceAll("\\s", "").replaceAll("//.*","");
            if (readLine.length() !=0 ){
                command = readLine;
                break;
            }
        }
    }




    public commandType commandType(){
        String[] args = command.split(" ");
        String argument1;
        int argument2;
        if (args.length == 1){
            if (args[0].equals("add") || args[0].equals("sub") || args[0].equals("neg")
             || args[0].equals("eq") || args[0].equals("gt") || args[0].equals("gt") || args[0].equals("and") 
            || args[0].equals("or") || args[0].equals("not")){
                argumentType = commandType.C_ARITHMETIC;
                argument1 = args[0];
            }
        } else if (args.length == 3) {
            if (args[0].equals("push")){

            }
            else if(args[0].equals("pop")){

            }
            
        }


    }
}
