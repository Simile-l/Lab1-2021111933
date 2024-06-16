package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * 软件工程实验三.
 */
public class TextToGraph {
  static final int MAX_WORDS = 10000;  // 假设最多有10000个不同的单词
  static int[] wordIds = new int[MAX_WORDS]; // 存储单词到ID的映射
  static int[][] graph = new int[MAX_WORDS][MAX_WORDS]; // 存储图的邻接矩阵，权值表示边的权重
  static int nextWordId = 0; // 用于分配下一个单词的ID
  static String[] idToWord = new String[MAX_WORDS]; // ID到单词的映射，便于输出时查找
  static String readDOTFilePath = "output\\demo.dot";
  static String storePNGFilePath = "output\\graph.png";
  static String enhanceDOTFilePath = "output\\enhanceDOT.dot";
  static String enhanceStorePNGFilePath =
          "output\\enhanceGraph.png";
  static String DOTString =
          "static\\Graphviz-11.0.0-win64\\bin\\dot.exe";
  private static Random rand = new Random();
  /**
  * 主函数.
  */
  
  public static void main(String[] args) {
    // Scanner scanner = new Scanner(System.in); 这是Spotbugs修改前的内容，修改后为下一行
    Scanner scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    System.out.println("请输入要读取的文本文件的完整路径和文件名："); // C:\Users\Jy\Desktop\SE_lab3_m\test.txt
    String fileName = scanner.nextLine();
    readFileAndBuildGraph(fileName); // 读文件、建立图结构

    while (true) {
      System.out.println("\n功能选择：\n1. 展示有向图\n2. 查询桥接词\n"
              + "3. 生成新文本\n4. 最短路径（起点+终点）\n5. 最短路径（起点）\n6. 随机游走\n0. 退出");
      int choice = scanner.nextInt();
      switch (choice) {
        case 1:
          displayDirectedGraph(); // 这个方法目前的实现是俺用来测试文本读取对不对的，没真的生成图
          showDirectedGraph(); // 展示有向图，这个方法（即函数）的参数是可以调整的，见ppt；
          // 附加内容：将生成的有向图以图像文件形式保存到磁盘
          break;
        case 2:
          // 定义变量类型名称，提示用户输入形式
          // queryBridgeWords(); // 查询桥接词，具体参数和返回值类型见ppt
          // 选择合适的格式输出
          System.out.println("请输入两个单词以查询桥接词（用空格分隔）：");
          String word1 = scanner.next();
          String word2 = scanner.next();
          String result = queryBridgeWords(word1, word2);
          System.out.println(result);
          break;
        case 3:
          // 定义变量类型名称，提示用户输入形式
          //generateNewText(); // 生成新文本，具体参数和返回值类型见ppt
          // 选择合适的格式输出
          System.out.println("请输入新文本以生成桥接词增强版文本：");
          scanner.nextLine();
          String inputText = scanner.nextLine();
          String newText = generateNewText(inputText);
          System.out.println("生成的新文本为：");
          System.out.println(newText);
          break;
        case 4:
          // 定义变量类型名称，提示用户输入形式
          System.out.println("请输入两个单词，使用换行分割:");
          try {
            String from = scanner.next();
            String to = scanner.next();
            calcShortestPath(from, to); // 权值最短路径，需要实现，具体参数和返回值类型见ppt
          } catch (Exception e) {
            e.printStackTrace();
          }
          break;
        case 5:
          // 定义变量类型名称，提示用户输入形式
          System.out.println("请输入一个单词，将会计算最短距离：");
          String userInput = scanner.next();
          calShortestPath(userInput.toLowerCase());
          // 计算一个单词的最短路径(); // 一个输入参数，然后遍历其他单词，调用calcShortestPath(该单词，被遍历的单词)即可
          // 选择合适的格式输出（感觉这里就可以直接文字输出，不然也太复杂了）
          break;
        case 6:
          // 定义变量类型名称，不需要输入
          // randomWalk(); // 随机游走，具体参数和返回值类型见ppt
          // 文件形式输出
          System.out.println("Starting random walk. Type 'stop' to save the path and exit.");
          String walkResult = randomWalk();
          break;
        case 0:
          System.exit(0);
          break;
        default:
          System.out.println("输入格式有误，请重新输入！");
      }
    }
  }
  
  /**
   * 从文本文件中读取数据，生成对应的有向图.
   */
  public static void readFileAndBuildGraph(String fileName) {
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))) {
      // Spotbugs修改过
      StringBuilder currentLine = new StringBuilder();
      String word;
      String previousWordIdStr = "-1"; // 初始化前一个单词的ID字符串为无效值

      // 逐行读取文件内容
      while ((word = br.readLine()) != null) {
        // 将当前行的文本添加到StringBuilder中，并处理换行符
        currentLine.append(word).append(" ");
        // 移除所有非字母字符，并将所有字符转为小写
        String cleanLine = currentLine.toString().replaceAll("[^a-zA-Z ]", "").toLowerCase();
        // 分割处理后的行以获得单词列表
        String[] words = cleanLine.split("\\s+");

        // 遍历单词列表
        for (String w : words) {
          if (!w.isEmpty()) { // 忽略空字符串
            // 获取或分配单词的ID
            int currentWordId = getWordId(w);
            if (currentWordId == -1) {
              currentWordId = assignWordId(w);
            }
            // 如果有前一个单词ID，则更新图中的权值
            if (!previousWordIdStr.equals("-1")) {
              int previousWordId = Integer.parseInt(previousWordIdStr);
              graph[previousWordId][currentWordId]++;
            }
            // 更新前一个单词ID的记录
            previousWordIdStr = String.valueOf(currentWordId);
          }
        }
        // 清空StringBuilder以处理下一行
        currentLine.setLength(0);
      }
    } catch (IOException e) {
      // 打印异常信息
      System.err.println("Error reading file: " + e.getMessage());
    }
  }
  
  
  /**
   * 根据单词获取其ID，如果单词尚未分配ID，则为其分配一个新的ID，输入需要获取或者分配ID的单词，输出ID.
   */
  private static int getWordId(String word) {
    // 遍历已有的单词ID列表
    for (int i = 0; i < nextWordId; i++) {
      // 如果找到相同的单词（通过hashCode和内容比较），返回其ID
      if (wordIds[i] == word.hashCode() && Objects.equals(idToWord[i], word)) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * 新增word时分配ID.
   */
  private static int assignWordId(String word) {
    // 分配新ID给单词
    wordIds[nextWordId] = word.hashCode(); // 存储单词的hashCode作为辅助查找
    idToWord[nextWordId] = word; // 存储单词本身以供显示
    return nextWordId++; // 返回新分配的ID并递增计数器
  }
  
  /**
   * 展示图结构.
   */
  private static void displayDirectedGraph() {
    System.out.println("有向图结构:");
    // 遍历所有已分配ID的单词
    for (int i = 0; i < nextWordId; i++) {
      System.out.print(idToWord[i] + " -> {");
      for (int j = 0; j < nextWordId; j++) {
        // 如果存在边，打印目标单词和权值
        if (graph[i][j] > 0) {
          System.out.print(idToWord[j] + ":" + graph[i][j] + " ");
        }
      }
      System.out.println("}");
    }
  }
  
  /**
   * 生成有向图.
   */
  private static void showDirectedGraph() {
    // 生成DOT文件内容
    StringBuilder dot = new StringBuilder("digraph G {\n");
    for (int i = 0; i < nextWordId; i++) {
      for (int j = 0; j < nextWordId; j++) {
        if (graph[i][j] > 0) {
          dot.append(idToWord[i])
              .append(" -> ")
              .append(idToWord[j])
              .append(" [label=\"")
              .append(graph[i][j])
              .append("\"];\n");
        }
      }
    }
    dot.append("}");
    // 将 StringBuilder 转换为 String
    String dotString = dot.toString();
    try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream(readDOTFilePath), StandardCharsets.UTF_8))) {
      // Spotbugs修改过
      bufferedWriter.write(dotString);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    generatePng(readDOTFilePath, storePNGFilePath);
  }
  
  /**
   * 最短路.
   */
  private static void calcShortestPath(String from, String to) {
    DefaultDirectedWeightedGraph<String, DefaultEdge> directedWeightedGraph =
        new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
    
    // 加顶点
    for (int i = 0; i < nextWordId; i++) {
      directedWeightedGraph.addVertex(idToWord[i]);
    }
    
    for (int i = 0; i < nextWordId; i++) {
      for (int j = 0; j < nextWordId; j++) {
        if (graph[i][j] > 0) {
          // 加权值
          DefaultEdge edge1 = directedWeightedGraph.addEdge(idToWord[i], idToWord[j]);
          directedWeightedGraph.setEdgeWeight(edge1, graph[i][j]);
        }
      }
    }
    
    DijkstraShortestPath<String, DefaultEdge> dijkstraAlg
        = new DijkstraShortestPath<>(directedWeightedGraph);
    double shortestPath = dijkstraAlg.getPathWeight(from, to);
    if (Double.isInfinite(shortestPath)) {
      System.out.println("无法到达");
      return;
    }
    List<DefaultEdge> path = dijkstraAlg.getPath(from, to).getEdgeList();
    
    // 打印路径和总权重
    System.out.println("最短路径从" + from + "到" + to + "：");
    for (DefaultEdge edge : path) {
      System.out.println(directedWeightedGraph.getEdgeSource(edge)
          + " -> " + directedWeightedGraph.getEdgeTarget(edge));
    }
    System.out.println("总权重：" + shortestPath);
    
    // 定义一个正则表达式来匹配想要提取的部分
    String regex = "(\\w+)\\s*->\\s*(\\w+)";
    
    // 编译正则表达式
    Pattern pattern = Pattern.compile(regex);
    
    try (BufferedReader reader = Files.newBufferedReader(Paths.get(readDOTFilePath));
         BufferedWriter writer = Files.newBufferedWriter(Paths.get(enhanceDOTFilePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        // 创建匹配器
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
          String part1 = matcher.group(1);
          String part2 = matcher.group(2);
          for (DefaultEdge edge : path) {
            if (part1.equals(directedWeightedGraph.getEdgeSource(edge))
                && part2.equals(directedWeightedGraph.getEdgeTarget(edge))) {
              line = line.replace("]", ", color=\"red\", penwidth=3.0]");
            }
          }
        }
        writer.write(line);
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    generatePng(enhanceDOTFilePath, enhanceStorePNGFilePath);
  }
  
  /**
   * 生成PNG图片.
   */
  private static void generatePng(String dotFile, String pngFile) {
    // 生成对应的图片
    String command = DOTString + " -Tpng " + dotFile + " -o " + pngFile;
    try {
      Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("生成的有向图的路径在: `" + pngFile + "` ");
  }
  
  /**
   * 计算最短路径.
   */
  public static void calShortestPath(String from) {
    int fromId = -1;
    for (int i = 0; i < idToWord.length; i++) {
      if (idToWord[i].equals(from)) {
        fromId = i;
        break;
      }
    }
    if (fromId == -1) {
      System.out.println("不存在你输入的端点");
      return;
    }
    
    int shortest = Integer.MAX_VALUE;
    ArrayList<Integer> shortestPath = new ArrayList<>();
    
    for (int i = 0; i < nextWordId; i++) {
      if (graph[fromId][i] > 0) {
        if (graph[fromId][i] < shortest) {
          shortestPath.clear();
          shortest = graph[fromId][i];
          shortestPath.add(i);
        } else if (graph[fromId][i] == shortest) {
          shortestPath.add(i);
        }
      }
    }
    
    System.out.println("最短距离是：" + shortest + "\n分别有以下终点：");
    for (int i = 0; i < shortestPath.size(); i++) {
      System.out.println(idToWord[shortestPath.get(i)]);
    }
  }
  
  /**
   * 查询桥接词.
   */
  public static String queryBridgeWords(String word1, String word2) {
    // 获取word1和word2的ID
    int id1 = getWordId(word1);
    int id2 = getWordId(word2);
    // 检查word1和word2是否在图中
    if (id1 == -1) {
      if (id2 == -1) {
        return "No " + word1 + " and " + word2 + " in the graph!";
      } else {
        return "No " + word1 + " in the graph!";
      }
    }
    if (id2 == -1) {
      return "No " + word2 + " in the graph!";
    }
    // 存储可能的桥接词
    List<String> bridgeWords = new ArrayList<>();
    // 遍历所有单词作为潜在的桥接词
    for (int i = 0; i < nextWordId; i++) {
      if (graph[id1][i] > 0 && graph[i][id2] > 0) {
        bridgeWords.add(idToWord[i]);
      }
    }
    
    if (bridgeWords.isEmpty()) {
      return "No bridge words from " + word1 + " to " + word2 + "!";
    } else {
      // 构建输出字符串，包含所有桥接词
      StringBuilder output = new StringBuilder("The bridge word from "
          + word1 + " to " + word2 + " : ");
      for (int i = 0; i < bridgeWords.size(); i++) {
        output.append(bridgeWords.get(i));
        if (i < bridgeWords.size() - 2) {
          output.append(", ");
        } else if (i == bridgeWords.size() - 2) {
          output.append(" and ");
        }
      }
      output.append(".");
      return output.toString();
    }
  }
  
  /**
   * 生成新文本函数.
   */
  public static String generateNewText(String inputText) {
    String cleanInput = inputText.replaceAll("[^a-zA-Z ]", "").toLowerCase(); // 清理输入文本
    String[] words = cleanInput.split("\\s+"); // 分割单词
    StringBuilder generatedText = new StringBuilder();
    Random rand = new Random();
    
    for (int i = 0; i < words.length - 1; i++) {
      generatedText.append(words[i]); // 添加当前单词
      String word1 = words[i];
      String word2 = words[i + 1];
      int id1 = getWordId(word1);
      int id2 = getWordId(word2);
      
      // 查找桥接词
      List<String> bridgeWords = new ArrayList<>();
      if (id1 != -1 && id2 != -1) {
        for (int j = 0; j < nextWordId; j++) {
          if (graph[id1][j] > 0 && graph[j][id2] > 0) {
            bridgeWords.add(idToWord[j]);
          }
        }
      }
      
      // 如果找到了桥接词，则随机选择一个插入
      if (!bridgeWords.isEmpty()) {
        String chosenBridge = bridgeWords.get(rand.nextInt(bridgeWords.size()));
        generatedText.append(" ").append(chosenBridge).append(" ");
      } else {
        generatedText.append(" "); // 如果没有桥接词，直接加空格
      }
    }
    generatedText.append(words[words.length - 1]); // 添加最后一个单词
    return generatedText.toString();
  }
  
  /**
   * 随机游走.
   */
  private static String randomWalk() {
    int[][] visit = new int[nextWordId][nextWordId]; // 用于记录边是否被访问过的visit数组
    
    int currentRandomWordId = rand.nextInt(nextWordId); // 随机选择一个起始单词
    int nextRandomWordId;
    
    StringBuilder pathBuilder = new StringBuilder(); // 用于构建随机游走路径
    while (true) {
      pathBuilder.append(idToWord[currentRandomWordId]).append(" "); // 将当前单词加入路径中
      
      // 检查是否需要停止游走
      Scanner scanner = new Scanner(
          new InputStreamReader(System.in, StandardCharsets.UTF_8)); // Spotbugs修改后
      System.out.println("Current path: " + pathBuilder.toString());
      System.out.println("Press enter to continue, or 'stop' to end random walk.");
      String input = scanner.nextLine();
      if (input.equals("stop")) {
        // 写入路径到文件
        try (PrintWriter out = new PrintWriter(
            new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream("output\\random.txt"), StandardCharsets.UTF_8)))) {
          out.println(pathBuilder.toString());
        } catch (IOException e) {
          System.err.println("Error writing file: " + e.getMessage());
        }
  
        return pathBuilder.toString();
      }
      
      // 随机选择下一个单词
      List<Integer> candidateIds = new ArrayList<>(); // 用于存储候选单词的id
      for (int i = 0; i < nextWordId; i++) {
        if (graph[currentRandomWordId][i] > 0) {
          // 如果该单词有出边，则将其加入候选列表中,这里不考虑是否被访问过
          candidateIds.add(i);
        }
      }
      if (!candidateIds.isEmpty()) {
        // 如果候选列表不为空，则随机选取一个作为下一个单词
        int randomIndex = rand.nextInt(candidateIds.size());
        nextRandomWordId = candidateIds.get(randomIndex);
        if (visit[currentRandomWordId][nextRandomWordId] == 0) {
          visit[currentRandomWordId][nextRandomWordId] = 1; // 标记边被访问过
        } else {
          // 如果某一条边被重复访问，则游走结束
          currentRandomWordId = nextRandomWordId;
          pathBuilder.append(idToWord[currentRandomWordId]).append(" "); // 将当前单词加入路径中
          System.out.println("Current path: " + pathBuilder.toString());
          System.out.println("This edge has been followed. Ending random walk.");
          try (PrintWriter out = new PrintWriter(
              new BufferedWriter(
                  new OutputStreamWriter(
                      new FileOutputStream("output\\random.txt"), StandardCharsets.UTF_8)))) {
            out.println(pathBuilder.toString());
          } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
          }
  
          return pathBuilder.toString();
        }
      } else {
        // 如果候选列表为空，则游走结束
        System.out.println("No more edges to follow. Ending random walk.");
        // 写入路径到文件
        try (PrintWriter out = new PrintWriter(
            new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream("output\\random.txt"), StandardCharsets.UTF_8)))) {
          out.println(pathBuilder.toString());
        } catch (IOException e) {
          System.err.println("Error writing file: " + e.getMessage());
        }
  
        return pathBuilder.toString();
      }
      currentRandomWordId = nextRandomWordId;
    }
  }

}





