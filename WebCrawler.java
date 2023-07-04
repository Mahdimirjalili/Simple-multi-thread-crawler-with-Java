import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.nodes.Document;

public class WebCrawler {

    public static BlockingQueue<String> queue;
    public static BlockingQueue<Document> docs;
    private final ExecutorService threadPool;
    public static Set<String> allUrls;
    public static int count = 2000; //specify number of pages should be crawl
    public static AtomicInteger countOfCrawledPages;
    public static Semaphore fetcherSem;
    public static Semaphore fileSem;
    private final Thread parser;
    public static AtomicInteger successFullDownloadedDocs;
    public static int needle =2000;

    public WebCrawler() {
        this.loadState();
        fetcherSem = new Semaphore(10);
        fileSem = new Semaphore(1);
        this.threadPool = Executors.newFixedThreadPool(10);
        docs = new LinkedBlockingQueue<>();
        successFullDownloadedDocs = new AtomicInteger(0);
        this.parser = new Thread(new Parser());
        parser.start();
    }

    public void crawl(String url) {

        if (countOfCrawledPages.get() == 0) {
            try {
                queue.put(url);
                allUrls.add(url);
            } catch (InterruptedException ex) {
                System.err.println("Error in put url in queue");
            }
        }
        FileOutputStream file = null;
        BufferedOutputStream bos = null;
        try {
            file = new FileOutputStream("file.html", true);
            bos = new BufferedOutputStream(file);

        } catch (FileNotFoundException ex) {
            System.err.println("Error during creating file!");
            System.exit(1);
        }
        long start = System.nanoTime();
        while (countOfCrawledPages.get() < count) {

            try {

                if (successFullDownloadedDocs.get() + (10 - fetcherSem.availablePermits()) >= needle) {
                    continue;
                }

                fetcherSem.acquire();
                url = queue.take();
            } catch (InterruptedException ex) {
                System.err.println("Error in while loop!");
            }

            threadPool.execute(new Fetcher(url, bos, fileSem));

        }

        try {
            threadPool.shutdown();

            while (!threadPool.isTerminated() || parser.isAlive()) {

            }
            System.out.println("\n\nSize OF queue : " + queue.size() + "\n\n");
            System.out.println((System.nanoTime() - start) / 1000_000_000 + "seconds");
            file.close();
            this.saveState();

        } catch (IOException ex) {
            System.err.println("Error in closing file");;
        }
    }

    private void saveState() {
        //third object is count of crawled pages
        //first object is allurls arrayList
        //second object is queue  arrayList

        try {
            FileOutputStream fis = new FileOutputStream("states.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fis);

            oos.writeInt(countOfCrawledPages.get());
            oos.writeObject(allUrls);
            oos.writeObject(queue);

            oos.close();
            fis.close();
        } catch (Exception e) {
            System.err.println("Error in save states");
        }

    }

    private void loadState() {
        try {
            FileInputStream file = new FileInputStream("states.txt");
            ObjectInputStream in = new ObjectInputStream(file);

            countOfCrawledPages = new AtomicInteger(in.readInt());
            allUrls = new ConcurrentHashMap().newKeySet();
            allUrls.addAll((Set<String>) in.readObject());

            queue = (BlockingQueue<String>) in.readObject();
            count += countOfCrawledPages.get();

            in.close();
            file.close();

            System.out.println("crawled : " + countOfCrawledPages);
            System.out.println("allurls: " + allUrls.size());
            System.out.println("queue : " + queue.size());

        } catch (Exception e) {

            System.out.println("##state load error##");
            queue = new LinkedBlockingQueue<String>();
            allUrls = new ConcurrentHashMap().newKeySet();
            countOfCrawledPages = new AtomicInteger(0);
        }
    }

}
