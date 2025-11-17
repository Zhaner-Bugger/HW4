module FoundationCode {
	requires javafx.controls;
	requires java.sql;
	requires junit;
	requires org.junit.jupiter.api;
	
	opens application to javafx.graphics, javafx.fxml, javafx.base;
}
