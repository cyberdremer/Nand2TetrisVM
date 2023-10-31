import java.io.*;
import java.util.HashMap;

public class CodeWriter {
    private FileWriter writeToOutput = null;
    private int arithmeticOp;
    CodeWriter(File fileIn) throws FileNotFoundException {

        try{
            writeToOutput = new FileWriter(fileIn);
            arithmeticOp = 0;


        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setFileName(String filename){


    }

    public void writeArithmetic(String command) throws IOException {
        if (command.equals("add")){
            writeToOutput.write(arithmeticCommandTemplate() + "M=D+M\n");
        } else if (command.equals("sub")) {
            writeToOutput.write(arithmeticCommandTemplate() + "M=D-M\n");
        } else if (command.equals("neg")) {
            writeToOutput.write("@SP\n" + "A=M\n" + "A=A-1\n" + "M=-M\n");

        } else if (command.equals("and")){
            writeToOutput.write(arithmeticCommandTemplate() + "M=D&M\n");
            
        } else if (command.equals("or")) {
            writeToOutput.write(arithmeticCommandTemplate() + "M=D|M\n");
        } else if (command.equals("not")) {
            writeToOutput.write(arithmeticCommandTemplate() + "M=!M\n");

        } else if (command.equals("eq")) {
            writeToOutput.write(arithmeticCommandTemplateComparison("JEQ"));
            arithmeticOp++;

        } else if (command.equals("gt")) {
            writeToOutput.write(arithmeticCommandTemplateComparison("JGT"));
            arithmeticOp++;
            
        }
        else if (command.equals("lt")){
            writeToOutput.write(arithmeticCommandTemplateComparison("JLT"));
            arithmeticOp++;
        } else if (command.equals("end")) {
            writeToOutput.write("(END" + ")\n" + "@END\n" + "0;JMP\n");

        } else {
            throw new IllegalArgumentException("Illegal Arithmetic Command.");
        }


    }

    private String arithmeticCommandTemplate(){
        return "@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n";

    }
    private String arithmeticCommandTemplateComparison(String cmp){
        return "@SP\n" + "AM=M-1\n"
                + "D=M\n" + "@SP\n" +
                "AM=M-1\n" + "D=M-D\n" +
                "@FALSE\n" + arithmeticOp + "\n" +
                "D;" + cmp +"\n" +
                "@SP\n" + "A=M-1\n" +"M=-1\n" +
                "@CONTINUE" + arithmeticOp + "\n" +
                "0;JMP\n" + "(FALSE " + arithmeticOp + ")\n"+
                "@SP\n" + "A=M-1\n" + "M=0\n" + "(CONTINUE" + arithmeticOp + ")\n";



    }
    public void WritePushPop(commandType cType, String segment, int index) throws IOException {
        if (cType.equals(commandType.C_PUSH)){
            if (segment.equals("constant")){
                writeToOutput.write("@" + index + "\n" + "D=A\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");

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
                writeToOutput.write(pushCommandTemplate(String.valueOf(index + 16), index, true));

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
                writeToOutput.write(popCommandTemplate(String.valueOf(index + 16), index, true));

            }
            else {
                throw new IllegalArgumentException("Illegal segment type");
            }

        }
        else {
            throw new IllegalArgumentException("Illegal popping or pushing operation.");
        }



    }

    private String pushCommandTemplate(String segment, int index, boolean DirectMem){
        String memAddress = (DirectMem)? "" : "@" + index + "\n" + "A=D+A\nD=M\n";
        return "@" + segment + "\n" +
                "D=M\n" + memAddress +
                "@SP\n" + "A=M\n" +
                "M=D\n" + "@SP\n" +
                "M=M+1\n";
    }

    private String popCommandTemplate(String segment, int index, boolean DirectMem){
        String memAddress = (DirectMem)? "D=A\n" :"D=M\n@" + index + "\nD=D+A\n";
        return "@" + segment + "\n" +
                memAddress +
                "@R13\n" +
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


}
