package saferun;

import dev.luckynetwork.alviann.commons.closer.Closer;
import dev.luckynetwork.alviann.commons.internal.Utils;

import java.util.Scanner;

public class SafeRunTest {

    public static void main(String[] args) {
        Utils.safeRun(() -> {
            try (Closer closer = new Closer()) {
                Scanner scanner = closer.add(new Scanner(System.in));

                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    System.out.println(line);
                }
            }

            return null;
        });
    }

}
