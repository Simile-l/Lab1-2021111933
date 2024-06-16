import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class TextToGraphTest {

    @Test
    public void testRandomWalk1() {
        // Initial data
        TextToGraph graph = new TextToGraph();
        graph.idToWord[0] = "cute";
        graph.graph[0][0] = 0;
        graph.nextWordId = 1;

        // begin test
        String guessResult = "cute";
        String actualResult = graph.randomWalk().trim();
        assertEquals(guessResult, actualResult);
    }

    @Test
    public void testRandomWalk2() {
        // Initial data
        TextToGraph graph = new TextToGraph();
        graph.idToWord[0] = "cute";
        graph.idToWord[1] = "cat";
        graph.graph[0][0] = 0;
        graph.graph[0][1] = 1;
        graph.graph[1][0] = 0;
        graph.graph[1][1] = 0;
        graph.nextWordId = 2;

        // begin test
        String actualResult = graph.randomWalk().trim();
        System.out.println(actualResult);
    }

    @Test
    public void testRandomWalk3() {
        // Initial data
        TextToGraph graph = new TextToGraph();
        String[] inputText = "I, have, a, cute, cat, and, a, cute, dog, and, two, cute, birds ".split("\\s+");

        HashSet<String> deleteRepeat = new HashSet<>(Arrays.asList(inputText));
        graph.idToWord = deleteRepeat.toArray(new String[0]);
        graph.nextWordId = graph.idToWord.length;
        int preIndex = -1;
        for (String word : inputText) {
            for (int j = 0; j < graph.nextWordId; j++) {
                if (graph.idToWord[j].equals(word)) {
                    if (preIndex == -1) {
                        preIndex = j;
                    } else {
                        graph.graph[preIndex][j]++;
                        preIndex = j;
                    }
                }
            }
        }
        // begin test
        String actualResult = graph.randomWalk().trim();
        System.out.println(actualResult);
    }

    @Test
    public void testRandomWalk4() {
        // Initial data
        TextToGraph graph = new TextToGraph();
        String[] inputText = "There, is, a, common, saying, in, China, that, practice, is, the, sole, criterion, for, testing, truth ".split("\\s+");

        HashSet<String> deleteRepeat = new HashSet<>(Arrays.asList(inputText));
        graph.idToWord = deleteRepeat.toArray(new String[0]);
        graph.nextWordId = graph.idToWord.length;
        int preIndex = -1;
        for (String word : inputText) {
            for (int j = 0; j < graph.nextWordId; j++) {
                if (graph.idToWord[j].equals(word)) {
                    if (preIndex == -1) {
                        preIndex = j;
                    } else {
                        graph.graph[preIndex][j]++;
                        preIndex = j;
                    }
                }
            }
        }
        // begin test
        String actualResult = graph.randomWalk().trim();
        System.out.println(actualResult);
    }


}
