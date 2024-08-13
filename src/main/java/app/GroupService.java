package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GroupService {

    public List<Set<String>> findGroups(List<String> lines) {
        String REGEX = "^\"\\d*\"(;\"\\d*\")*;?$";
        List<Map<String, Integer>> listPositionWordsWithMapWordAndGroupNumber = new ArrayList<>(); //[позиция_слова:{слово:номер_группы}]
        List<Set<String>> groupList = new ArrayList<>(); //[номер_группы:[строки_группы]]
        Map<Integer, Integer> mergedGroupNumberToFinalGroupNumber = new HashMap<>(); //{номер_слитой_группы:номер_группы_в_которую_слили}

        for (String line : lines) {
            if(line.matches(REGEX)) {
                String[] words = line.split(";");

                TreeSet<Integer> foundGroups = new TreeSet<>();
                List<NewWord> newWords = new ArrayList<>();

                for (int position = 0; position < words.length; position++) {
                    String word = words[position];

                    if (listPositionWordsWithMapWordAndGroupNumber.size() == position)
                        listPositionWordsWithMapWordAndGroupNumber.add(new HashMap<>());

                    if (word.equals("\"\""))
                        continue;

                    Map<String, Integer> mapKeyWordAndValueGroupNumber = listPositionWordsWithMapWordAndGroupNumber.get(position);
                    Integer groupNumber = mapKeyWordAndValueGroupNumber.get(word); // получаем у текущего слова номер группы, или вернётся null если её нет

                    if (groupNumber != null) {
                        groupNumber = getNumberGroupAfterMergeGroup(mergedGroupNumberToFinalGroupNumber,groupNumber);
                        foundGroups.add(groupNumber); // сохраним, что нашли группу с номером groupNumber
                    } else
                        newWords.add(new NewWord(word, position)); // если слово не состоит ни в какой группе, то оно новое. Создаем его объект.
                }

                int groupNumber = getGroupNumberForAdd(foundGroups,groupList);
                addWordToGroup(groupNumber,newWords,listPositionWordsWithMapWordAndGroupNumber);
                mergeGroup(foundGroups,groupNumber,mergedGroupNumberToFinalGroupNumber,groupList);
                groupList.get(groupNumber).add(line);
            }
        }
        groupList.removeAll(Collections.singleton(null));
        return groupList;
    }

    private int getNumberGroupAfterMergeGroup(Map<Integer, Integer> mergedGroupNumberToFinalGroupNumber,int groupNumber) {
        while (mergedGroupNumberToFinalGroupNumber.containsKey(groupNumber)) // проверяем, чтобы номмер группы groupNumber не был объеденён с другой
            //А если был, то получаем номмер группы, с которой произошло слияние
            groupNumber = mergedGroupNumberToFinalGroupNumber.get(groupNumber);
        return groupNumber;
    }

    private void mergeGroup(TreeSet<Integer> foundInGroups, int groupNumber, Map<Integer, Integer> mergedGroupNumberToFinalGroupNumber, List<Set<String>> linesGroups){
        for (int mergeGroupNumber : foundInGroups) {
            if (mergeGroupNumber != groupNumber) { // когда номер текущей группы отличается от номера в списке найденных групп, то делаем объединение
                mergedGroupNumberToFinalGroupNumber.put(mergeGroupNumber, groupNumber); // сохраняем, что группа с номером mergeGroupNumber объеденяется с группой с номером groupNumber
                linesGroups.get(groupNumber).addAll(linesGroups.get(mergeGroupNumber));// делаем объединение групп, добавляем всё в группу с номером groupNumber
                linesGroups.set(mergeGroupNumber, null);// группу с номером mergeGroupNumber удаляем
            }
        }
    }

    private int getGroupNumberForAdd(TreeSet<Integer> foundInGroups, List<Set<String>> groups){
        int groupNumber;
        if (foundInGroups.isEmpty()) {
            groupNumber = groups.size();
            groups.add(new HashSet<>());
        } else
            groupNumber = foundInGroups.first();

        return groupNumber;
    }

    private void addWordToGroup(int groupNumber, List<NewWord> newWords,List<Map<String, Integer>> listPositionWordsWithMapWordGroupNumber){
        for (NewWord newWord : newWords)
            listPositionWordsWithMapWordGroupNumber.get(newWord.position).put(newWord.value, groupNumber);
    }


    public void write(String outputFilePath,  List<Set<String>> groupList){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            long countGroupMoreOneStr =  groupList.stream().filter(listStr -> listStr.size() > 1).count();
            groupList.sort(Comparator.comparingInt(Set::size));
            Collections.reverse(groupList);
            writer.write("Количество групп более чем с одной строкой: " + countGroupMoreOneStr + "\n\n");
            int groupNumber = 1;
            for (Set<String> group : groupList) {
                writer.write("Группа " + groupNumber++ + "\n");
                for (String line : group) {
                    writer.write(line + "\n");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
