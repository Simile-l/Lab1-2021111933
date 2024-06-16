import org.example.TextToGraph;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Test1_Bridge {
  @BeforeClass
  public static void setUp() {
    TextToGraph.readFileAndBuildGraph("test.txt");
  }
  
  @Test
  public void testQueryBridgeWords() {
    String result = TextToGraph.queryBridgeWords("but", "spider");
    String expectedResult = "No bridge words from but to spider!";
    assertEquals(expectedResult, result);
    if (expectedResult.equals(result)) {
      System.out.println("Test 1 success");
    }else{
      System.out.println("Test fail! ");
    }
  }
}