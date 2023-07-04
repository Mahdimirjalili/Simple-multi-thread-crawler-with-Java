
import java.io.BufferedOutputStream;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Fetcher implements Runnable {

    private final String url;
    private final BufferedOutputStream file;
    private final Semaphore sem;

    public Fetcher(String url, BufferedOutputStream file, Semaphore sem) {
        this.url = url;
        this.file = file;
        this.sem = sem;
    }

    @Override
    public void run() {
        try {
            Document doc = null;
            
            Thread.sleep(500);
            
            long start = System.nanoTime();
            try {
                doc = Jsoup.connect(this.url).get();
                String data = "\n\n*** This page belong to url : " + url + "***" + "\n\n" + doc.html();
                
                this.sem.acquire();
                file.write(data.getBytes());
                System.out.println("url : " + url + " downloaded in " + (System.nanoTime() - start) / 1000_000_000 + "seconds");
                WebCrawler2.successFullDownloadedDocs.incrementAndGet();
                this.sem.release();
                if (doc != null) {
                    try {
                        WebCrawler2.docs.put(doc);
                    } catch (InterruptedException ex) {
                        System.out.println("Error in putting doc in docs queue!");
                    }
                }
                
            } catch (Exception ex) {
                System.err.println("Error in download file from url : " + url);
                System.err.println(ex.getMessage());
            }
            
            WebCrawler2.fetcherSem.release();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Fetcher.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
