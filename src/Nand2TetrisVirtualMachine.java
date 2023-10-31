import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Nand2TetrisVirtualMachine {
    public static ArrayList<File> getVMFiles(File dir){
        File[] files = dir.listFiles();
        ArrayList<File> grabbedFiles = new ArrayList<File>();
        for (File f:files){
            if (f.getName().endsWith(".vm")){
                grabbedFiles.add(f);
            }
        }
        return grabbedFiles;

    }
    public static void main(String[] args) throws IOException {
       File fileIn = new File(args[0]);
       String fileOutPath = "";
       File writeOut;
       CodeWriter codeWriter;
       ArrayList<File> vmFiles = new ArrayList<File>();
       if (fileIn.isFile()){
           String path = fileIn.getAbsolutePath();
           if(!path.matches(".*vm")){
               throw new IllegalArgumentException("Non-VM file read!");

           }
           vmFiles.add(fileIn);
           fileOutPath = fileIn.getAbsolutePath().substring(0,fileIn.getAbsolutePath().lastIndexOf(".")) + ".asm";
       } else if (fileIn.isDirectory()) {
           vmFiles = getVMFiles(fileIn);
           if (vmFiles.size() == 0){
               throw new IllegalArgumentException("No VM files found.");
           }
           fileOutPath = fileIn.getAbsolutePath() + "/" + fileIn.getName() + ".asm";

       }
       writeOut = new File(fileOutPath);
       codeWriter = new CodeWriter(writeOut);
       for (File f:vmFiles){
           Parser parser = new Parser(f.getAbsolutePath());
           parser.advance();
           commandType c;
           while (parser.getCommand() != null){
               c = parser.commandType();
               if (c.equals(commandType.C_ARITHMETIC)){
                   codeWriter.writeArithmetic(parser.getArgument1());

               } else if (c.equals(commandType.C_POP) || c.equals(commandType.C_PUSH)) {
                   codeWriter.WritePushPop(c,parser.getArgument1(), parser.getArgument2());

               }
               parser.advance();

           }

       }
        codeWriter.closeFile();

        System.out.println("File created : " + fileOutPath);
    }
}