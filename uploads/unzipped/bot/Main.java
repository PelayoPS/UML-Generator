package bot;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce el token del bot:");
        String token = scanner.nextLine();
        scanner.close();
        Bot bot = new Bot();
        bot.start(token);

    }
}