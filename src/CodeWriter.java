import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeWriter {
    private FileWriter writeToOutput = null;
    private static final Pattern recognizeLabel = Pattern.compile("^[^0-9][0-9A-Za-z\\_\\:\\.\\$]+");
    private int arithmeticOp;
    private int numOfLabels = 0;

    private String fileName = "";
    CodeWriter(File fileIn) throws FileNotFoundException {
        //Open file, if unable to open, throw FileNotFoundException

        try{
            fileName = fileIn.getName();
            writeToOutput = new FileWriter(fileIn);
            arithmeticOp = 0;


        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //Change the name of the file, necessary for dealing with multiple files for compilation
    public void changeFileName(File file){
        fileName = file.getName();
    }



    //Take in a command and parse through for specific keywords, and apply correct assembly instructions
    public void writeArithmetic(String command) throws IOException {
        if (command.equals("add")){
            writeToOutput.write(arithmeticCommandTemplate() + "M=M+D\n");
        } else if (command.equals("sub")) {
            writeToOutput.write(arithmeticCommandTemplate() + "M=M-D\n");
        } else if (command.equals("neg")) {
            writeToOutput.write("D=0\n@SP\nA=M-1\nM=D-M\n");

        } else if (command.equals("and")){
            writeToOutput.write(arithmeticCommandTemplate() + "M=M&D\n");
            
        } else if (command.equals("or")) {
            writeToOutput.write(arithmeticCommandTemplate() + "M=M|D\n");
        } else if (command.equals("not")) {
            writeToOutput.write("@SP\nA=M-1\nM=!M\n");

        } else if (command.equals("eq")) {
            writeToOutput.write(arithmeticCommandTemplateComparison("JNE"));
            arithmeticOp++;

        } else if (command.equals("gt")) {
            writeToOutput.write(arithmeticCommandTemplateComparison("JLE"));
            arithmeticOp++;
            
        }
        else if (command.equals("lt")){
            writeToOutput.write(arithmeticCommandTemplateComparison("JGE"));
            arithmeticOp++;
        } else if (command.equals("end")) {
            writeToOutput.write("(END" + ")\n" + "@END\n" + "0;JMP\n");

        } else {
            throw new IllegalArgumentException("Illegal Arithmetic Command.");
        }


    }


    //Template for arithmetic commands such as add, subtract, and, or.
    private String arithmeticCommandTemplate(){
        return "@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n";

    }
    //Template for arithmetic commands that involved jumping to specific labels.
    private String arithmeticCommandTemplateComparison(String cmp){
        return "@SP\n" +
                "AM=M-1\n"
                + "D=M\n"
                + "A=A-1\n"
                + "D=M-D\n"
                + "@FALSE"
                + arithmeticOp
                + "\n"
                + "D;" + cmp +"\n" +
                "@SP\n"
                +"A=M-1\n"
                +"M=-1\n" +
                "@CONTINUE" + arithmeticOp + "\n" +
                "0;JMP\n"
                + "(FALSE" + arithmeticOp + ")\n"+
                "@SP\n"
                + "A=M-1\n"
                + "M=0\n"
                + "(CONTINUE" + arithmeticOp + ")\n";



    }

    //Write or push and pop, depending on the command type, then dependent on the segment that is parsed.
    public void WritePushPop(commandType cType, String segment, int index) throws IOException {
        if (cType.equals(commandType.C_PUSH)){
            if (segment.equals("constant")){
                writeToOutput.write("@" + index + "\n" + "D=A\n" +"@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");

            } else if (segment.equals("local")) {
                writeToOutput.write(pushCommandTemplate("LCL",index,false));
                
            } else if (segment.equals("argument")) {
                writeToOutput.write(pushCommandTemplate("ARG",index, false));
            } else if (segment.equals("this")) {
                writeToOutput.write(pushCommandTemplate("THIS",index,false));
            } else if (segment.equals("that")) {
                writeToOutput.write(pushCommandTemplate("THAT",index,false));
                
            } else if (segment.equals("temp")) {
                writeToOutput.write(pushCommandTemplate("R5",index+5, false));
            } else if (segment.equals("pointer") && index == 0) {
                writeToOutput.write(pushCommandTemplate("THIS", index, true));
                
            }
            else if (segment.equals("pointer") && index == 1) {
                writeToOutput.write(pushCommandTemplate("THAT", index, true));

            } else if (segment.equals("static")) {
                writeToOutput.write("@" + fileName + index + "\n" + "D=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

            }
            else {
                throw new IllegalArgumentException("Illegal segment type");
            }


        } else if (cType.equals(commandType.C_POP)) {
            if (segment.equals("local")) {
                writeToOutput.write(popCommandTemplate("LCL",index,false));

            } else if (segment.equals("argument")) {
                writeToOutput.write(popCommandTemplate("ARG",index, false));
            } else if (segment.equals("this")) {
                writeToOutput.write(popCommandTemplate("THIS",index,false));
            } else if (segment.equals("that")) {
                writeToOutput.write(popCommandTemplate("THAT",index,false));

            } else if (segment.equals("temp")) {
                writeToOutput.write(popCommandTemplate("R5",index+5, false));
            } else if (segment.equals("pointer") && index == 0) {
                writeToOutput.write(popCommandTemplate("THIS", index, true));

            }
            else if (segment.equals("pointer") && index == 1) {
                writeToOutput.write(popCommandTemplate("THAT", index, true));

            } else if (segment.equals("static")) {
                writeToOutput.write("@" + fileName + index  + "\nD=A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n");

            }
            else {
                throw new IllegalArgumentException("Illegal segment type");
            }

        }
        else {
            throw new IllegalArgumentException("Illegal popping or pushing operation.");
        }



    }


    //Template for push command, takes in an index and whether or not we are working with a memory address to pus

    private String pushCommandTemplate(String segment, int index, boolean DirectMem){
        String memAddress = (DirectMem)? "" : "@" + index + "\n" + "A=D+A\nD=M\n";
        return "@" + segment + "\n" +
                "D=M\n" + memAddress +
                "@SP\n" + "A=M\n" +
                "M=D\n" + "@SP\n" +
                "M=M+1\n";
    }

    //Template for pop command, takes in an index and is affected by whether or not we are directly accessing a memory address.
    private String popCommandTemplate(String segment, int index, boolean DirectMem){
        String memAddress = (DirectMem)? "D=A\n" :"D=M\n@" + index + "\nD=D+A\n";
        return "@" + segment + "\n" +
                memAddress +
                "@R13\n" +
                "M=D\n" +
                "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "@R13\n" +
                "A=M\n" +
                "M=D\n";


    }

    public void closeFile() throws IOException {
        writeToOutput.close();
    }

    //Function to write an initialization assembly block
    public void writeInit() throws IOException {
        writeToOutput.write("@256\n" + "D=A\n" + "@SP\n" + "M=D\n");
        writeCall("Sys.init", 0);

    }
    //Generate labels
    public void writeLabel(String label) throws IOException {
        Matcher verifyLabel = recognizeLabel.matcher(label);
        if (verifyLabel.find()){
            writeToOutput.write("(" + label + ")\n");
        }
        else {
            throw new IllegalArgumentException("Invalid label");
        }

    }
    //Write a GOTO label
    public void writeGoto(String label) throws IOException {
        Matcher verifyLabel = recognizeLabel.matcher(label);
        if (verifyLabel.find()){
            writeToOutput.write("@" + label + "\n" + "0;JMP\n");
        }
        else {
            throw new IllegalArgumentException("Invalid label");
        }


    }
    //Write an assembly function for a write-if
    public void writeIf(String label) throws IOException {
        Matcher verifyLabel = recognizeLabel.matcher(label);
        if (verifyLabel.find()){
            writeToOutput.write("@SP\n" + "AM=M-1\n" + "D=M\n" + "@" + label + "\n" +
                    "D;JNE\n");
        }
        else {
            throw new IllegalArgumentException("Invalid label");
        }

    }


    //Write the assembly function for a Call, push LCL, ARG, THIS and THAT.
    public void writeCall(String funcName, int numOfArg) throws IOException {
        String newLabel = "RETURN_LABEL" + numOfLabels++;
        writeToOutput.write("@" + newLabel + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\nM=M+1\n");
        writeToOutput.write(pushCommandTemplate("LCL",0,true));
        writeToOutput.write(pushCommandTemplate("ARG",0,true));
        writeToOutput.write(pushCommandTemplate("THIS",0,true));
        writeToOutput.write(pushCommandTemplate("THAT",0,true));

        writeToOutput.write("@SP\n" +
                "D=M\n" + "@5\n" + "D=D-A\n" + "@" + numOfArg + "\n" +
                "D=D-A\n" + "@ARG\n" + "M=D\n" +
                "@SP\n" + "D=M\n" +
                "@LCL\n" + "M=D\n" +
                "@" + funcName + "\n" +
                "0;JMP\n" +
                "(" + newLabel + ")\n");








    }
    //Initialize the vars for a function
    public void writeFunction(String functionName, int numLocal) throws IOException {
        writeToOutput.write("(" + functionName + ")\n");
        for (int i = 0; i < numLocal; i++){
            WritePushPop(commandType.C_PUSH, "constant" , 0);
        }





    }


    //Template for writing the function return, restores this, that arg and lcl of the caller.

    public void writeReturn() throws IOException {
        writeToOutput.write("@LCL\n" + "D=M\n" +
                "@R11\n" + "M=D\n" +
                "@5\n" + "A=D-A\n" +
                "D=M\n" + "@R12\n" + "M=D\n" +
                popCommandTemplate("ARG", 0, false) +
                "@ARG\n" + "D=M\n" +
                "@SP\n" + "M=D+1\n"+
                restoreTemplate("THAT") +
                restoreTemplate("THIS") +
                restoreTemplate("ARG") +
                restoreTemplate("LCL") +
                "@R12\n" + "A=M\n" + "0;JMP\n"
                );



    }


    //Template for a restore depending on the memory template.


    public String restoreTemplate(String memSegment){
        return "@R11\n" + "D=M-1\n" +
                "AM=D\n" +
                "D=M\n" +
                "@" + memSegment + "\n" +
                "M=D\n";


    }






}
