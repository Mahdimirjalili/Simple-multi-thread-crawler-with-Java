
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Parser implements Runnable{

    private List<String> parse(Document doc) {
        List<String> links = new ArrayList();

        Elements elements = doc.select("a[href*=sums.ac.ir]");

        elements.forEach((link) -> {
            links.add(link.attr("abs:href"));
        });

        return links;
    }

     private List<String> uniquenessChecker(List<String> urls) {
        List<String> links = new ArrayList<>();
        for (String url : urls) {
          if(!WebCrawler2.allUrls.contains(url)){
              links.add(url);
              WebCrawler2.allUrls.add(url);
          }
        }
        return links;
    }
    @Override
    public void run() {
        
        while(WebCrawler2.countOfCrawledPages.get() < WebCrawler2.count){
            try {
                Document doc = WebCrawler2.docs.take();
                List<String> urls = this.uniquenessChecker(this.parse(doc));
                
                for(String url :urls){
                    WebCrawler2.queue.put(url);
                }
                WebCrawler2.countOfCrawledPages.incrementAndGet();
            } catch (InterruptedException ex) {
                System.err.println("error in parse!");
            }
        }
    }
    

}
