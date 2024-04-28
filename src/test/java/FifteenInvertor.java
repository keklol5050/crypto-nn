import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FifteenInvertor {
    public static void main(String[] args) throws IOException {
        String path = "C:\\Users\\keklo\\OneDrive\\Рабочий стол\\merge_from_ofoct.csv";
        List<String> lines = Files.readAllLines(Path.of(path));
        PrintWriter writer = new PrintWriter(path);
        writer.println(lines.remove(0));
        for (String str : lines) {
            String[] tokens = str.split(",")[0].split(" ");
            String[] time = tokens[1].split(":");
            String check = time[1] + ":" + time[2];
            if (check.equals("00:00") || check.equals("15:00") || check.equals("30:00") || check.equals("45:00")) writer.println(str);
            writer.flush();
        }
    }
}
