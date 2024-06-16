import org.example.TextToGraph;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Test5_Bridge {
  @BeforeClass
  public static void setUp() {
    TextToGraph.readFileAndBuildGraph("test.txt");
  }
  
  @Test
  public void testQueryBridgeWords() {
    String result = TextToGraph.queryBridgeWords("many", "books");
    String expectedResult = "No books in the graph!";
    assertEquals(expectedResult, result);
    if (expectedResult.equals(result)) {
      System.out.println("Test 5 success");
    }else{
      System.out.println("Test fail! ");
    }
  }
}