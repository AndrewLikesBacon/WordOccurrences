module MyJavaFx {
	requires javafx.controls;
	requires javafx.graphics;
	requires org.junit.jupiter.api;
	requires junit;
	requires java.sql;
	
	opens application to javafx.graphics, javafx.fxml;
}
