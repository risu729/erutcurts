import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Main {
  public static void main(String[] args) {
    System.out.println(Button.primary("test", "test").getMaxPerRow());
  }
}