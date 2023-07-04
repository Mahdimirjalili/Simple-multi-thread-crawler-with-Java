import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrawlerTest {

    public static BlockingQueue queue = new LinkedBlockingQueue();

    public static void main(String[] args) throws InterruptedException {

        WebCrawler web = new WebCrawler();
        web.crawl("https://www.sums.ac.ir");

        try {

            FileInputStream fis = new FileInputStream("states.txt");

            ObjectInputStream ois = new ObjectInputStream(fis);

            System.out.println(ois.readInt());

            Set<String> set = (Set<String>) ois.readObject();
            Set<String> set2 = new HashSet<>();

            set2.addAll(set);

            System.out.println(set.size() + "    " + set2.size());
            
            BlockingQueue<String> bq = (BlockingQueue<String>) ois.readObject();
            
            System.out.println("Queue size : "+bq.size());
        } catch (Exception ex) {
            Logger.getLogger(CrawlerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
