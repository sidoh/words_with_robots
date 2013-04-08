package org.sidoh.words_with_robots.util.io;

import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;

public class StdinPrompts {
	private static final Scanner in = new Scanner(System.in);

  public static long promptForLong(String message) {
    System.out.println(message);
    System.out.print("> ");

    while (true) {
      try {
        long value = in.nextLong();
        in.nextLine();

        return value;
      }
      catch (InputMismatchException e) {
        in.nextLine(); // flush value
        System.out.println("Invalid input. Please try again: ");
        System.out.print("> ");
      }
    }
  }

	public static int promptForInt(String message) {
		System.out.println(message);
		System.out.print("> ");
		
		while (true) {
			try {
				int value = in.nextInt();
        in.nextLine();

        return value;
			}
			catch (InputMismatchException e) {
        in.nextLine(); // flush value
				System.out.println("Invalid input. Please try again: ");
				System.out.print("> ");
			}
		}
	}
	
	public static String promptForLine(String message) {
		System.out.println(message);
		System.out.print("> ");
		
		return in.nextLine();
	}
	
	public static File promptForFile(String message, boolean requireExists, boolean allowEmpty) {
		System.out.println(message);
		System.out.print("> ");
		
		while (true) {
			File file = new File(in.nextLine());
			
			if (requireExists && !file.exists() && (!allowEmpty || !file.getName().isEmpty())) {
				System.out.println("File not found. Please try again: ");
				System.out.print("> ");
			}
			else {
				return file;
			}
		}
	}
}
