import org.example.TextToGraph;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Test2_Bridge {
  @BeforeClass
  public static void setUp() {
    TextToGraph.readFileAndBuildGraph("test.txt");
  }
  
  @Test
  public void testQueryBridgeWords() {
    String result = TextToGraph.queryBridgeWords("is", "possible");
    String expectedResult = "The bridge word from is to possible : quite.";
    assertEquals(expectedResult, result);
    if (expectedResult.equals(result)) {
      System.out.println("Test 2 success");
    }else{
      System.out.println("Test fail! ");
    }
  }
}