package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;


public class Main extends Application implements EventHandler<ActionEvent>{
	
	Button button;
	Button button2;
	Stage stage;
	File f = null;
	Scene scene;
	Stage primaryStage;
	Text textBox;
	boolean haslooped = false;
	
	/**
	 * Creates the main window with buttons
	 * 
	 * @param primaryStage
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			
			primaryStage.setTitle("Word occurrence counter");
			
			button = new Button("Select file");
			button2 = new Button("Count Words");
			textBox = new Text();
			textBox.setWrappingWidth(800);
			
			button.setOnAction(this);
			button2.setOnAction(this);
			
			HBox layout = new HBox(button, button2, textBox);
			
			scene = new Scene(layout,960,540);
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates empty database table, launches program, prints database table
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		createTable();
		launch(args);
		
	}
	
	/**
	 * Opens file explorer or counts words depending on which button is pressed
	 * 
	 * @param event Which button is pressed
	 */
	public void handle(ActionEvent event) {
		
		ExtensionFilter ext = new ExtensionFilter("Text Files", "*.txt");
		
		if (event.getSource() == button) {
			
			FileChooser filechooser = new FileChooser();
			filechooser.getExtensionFilters().add(ext);
			f = filechooser.showOpenDialog(stage);
			
		}
		
		if (event.getSource() == button2) {
			
			if (f != null) {
				
				System.out.println(countWords(f));
				textBox.setText(countWords(f));
				
			}
		}
	}
	
	/**
	 * Counts how many times each word occurs in the file
	 * 
	 * @param file The file to count the words from
	 */
	public String countWords(File file) {
		
		HashMap<String, Integer> words = new HashMap<>();
		
		File textFile = file;
		
		try {
			
			Scanner scan = new Scanner(textFile);
			
			while (scan.hasNext()) {
				String word = scan.next();
				
				//remove non-letters
				for (int i=0; i<word.length(); i++) {
					
					if (word.charAt(i) < 'A' || word.charAt(i) > 'z') {
						word = word.substring(0,i) + word.substring(i+1);
						i--;
					}
					
				}
				
				//make words lower case and puts them in the hash map or adds one to frequency if it is already there
				if (word.length() > 0) {
					
					word = word.toLowerCase();
					
					if (words.containsKey(word)) {
						words.put(word, words.get(word) + 1);
					} else {
						words.put(word, 1);
					}
				}
			}
			
			//sorts the hash map in descending order
			Map<String, Integer> descendingWords = words.entrySet().stream().sorted(Collections.reverseOrder(Entry.comparingByValue())).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(),(entry1, entry2) -> entry2, LinkedHashMap::new));
			
			String descendingWordsString = descendingWords.toString();
			
			if (!haslooped) {
				
				String[] finalWordsArray = descendingWordsString.substring(1).split(" ");
				
				for (int i=0; i<finalWordsArray.length; i++) {
					
					String finalWord = finalWordsArray[i].substring(0, finalWordsArray[i].indexOf("="));
					String finalCountString = finalWordsArray[i].substring(finalWordsArray[i].indexOf("=")+1, finalWordsArray[i].length()-1);
					
					int finalCount = Integer.parseInt(finalCountString);
					
					try {
						post(finalWord, finalCount);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				try {
					get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				haslooped = true;
			}
			
			return descendingWordsString;
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			return "File not found.";
			
		}
		
	}
	
	public static ArrayList<String> get() throws Exception {
		
		try {
			
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT word, count FROM word");
			
			ResultSet result = statement.executeQuery();
			
			ArrayList<String> array = new ArrayList<String>();
			
			while(result.next()) {
				
				System.out.print(result.getString("word"));
				System.out.print(" ");
				System.out.println(result.getString("count"));
				
				array.add(result.getString("word"));
				
			}
			
			System.out.println("Table read");
			return array;
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return null;
	}
	
	public static void post(String w, int c) throws Exception {
		
		try {
			
			Connection con = getConnection();
			PreparedStatement posted = con.prepareStatement("INSERT INTO word (word, count) Values ('" + w + "', '" + c + "')");
			posted.executeUpdate();
			
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			System.out.println("Word added to table");
		}
		
	}
	
	public static void createTable() throws Exception{
		
		try {
			
			Connection con = getConnection();
			PreparedStatement delete = con.prepareStatement("DROP TABLE word");
			PreparedStatement create = con.prepareStatement("CREATE TABLE IF NOT EXISTS word(word varchar(255), count int, PRIMARY KEY(word))");
			delete.executeUpdate();
			create.executeUpdate();
			
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			System.out.println("Table created");
		}
		
	}
	
	public static Connection getConnection() throws Exception {
		
		try {
			
			String driver = "com.mysql.cj.jdbc.Driver";
			String url = "jdbc:mysql://localhost:3306/mysql";
			String username = "root";
			String password = "1234";
			Class.forName(driver);
			
			Connection con = DriverManager.getConnection(url, username, password);
			return con;
			
		} catch (Exception e) {
			
			System.out.println(e);
			
		}
		
		return null;
		
	}
}
