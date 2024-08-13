package app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class App {
    public static void main( String[] args ){
        long startTime = System.nanoTime();

        if (args.length > 0) {
            String path = args[0];
            System.out.println("Введённый путь: " + path);

            Path pathFile = Paths.get(path);
            String nameFile=pathFile.getFileName().toString().split("\\.")[0];
            String outputFilePath = pathFile.getParent()+"/"+nameFile+"_out.txt";
            System.out.println("Выходной путь= "+ outputFilePath);

           try {
               List<String> lines = Files.readAllLines(pathFile);
               GroupService groupService = new GroupService();
               List<Set<String>> goupList = groupService.findGroups(lines);
               goupList.forEach(e -> System.out.println(e+" "));

               groupService.write(outputFilePath, goupList);
           }catch (IOException e) {
               throw new RuntimeException("Ошибка чтения файла, "+ e);
           }

        } else
            System.out.println("Введите путь к файлу");

        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1000000000;
        System.out.println("Время выполнения программы: " + totalTime + " секунд");
    }

}
