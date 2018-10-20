package co.story.server.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Crawler(name = "name_man")
public class BoyNameCrawler extends BaseSeimiCrawler {

    final static String HTTP = "http://xh.5156edu.com";

    Map<String, String> map = new HashMap<>();

    Writer writer;

    int writeCount = 0;
    int totalCount = 0;

    @Override
    public String[] startUrls() {
        return new String[]{"http://xh.5156edu.com/xm/nan.html"};
    }

    @Override
    public void start(Response response) {
        try {
            FileOutputStream out = new FileOutputStream("name_man.sql");
            OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
            writer = new BufferedWriter(osw);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JXDocument doc = response.document();
        try {
            List<JXNode> nodes = doc.selN("//a[@class='fontbox']");
            totalCount = nodes.size();

            for (JXNode node : nodes) {
                Element element = node.asElement();
                String url = HTTP + element.attr("href");
                String key = element.childNode(0).toString();
                logger.info("{}:{}", key, url);
                map.put(url, key);

                push(Request.build(url, "getWords"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getWords(Response response) {
        JXDocument doc = response.document();
        try {
            String xPath = "//font[@color='red']";
            List<JXNode> nodes = doc.selN(xPath);

            String key = map.get(response.getUrl());

            StringBuilder builder = new StringBuilder();

            for (JXNode node : nodes) {
                Element element = node.asElement();
                Node childNode = element.childNode(0);
                String text = childNode.toString();

                if (!text.equals("男")) {
                    continue;
                }

                Node parentNode = element.parentNode();
                Node textNode = parentNode.childNode(0);
                String word = textNode.toString();
                word = word.substring(0, word.length() - 2);
                if (word.compareToIgnoreCase(key) != 0) {
                    builder.append(word);
                    builder.append("|");
                }
            }

            if (builder.length() >= 1) {
                builder.deleteCharAt(builder.length() - 1);
            }

            String content = builder.toString();

            if (content.isEmpty()) return;

            String sql = String.format("INSERT INTO name_build (gender,word_key,words) VALUES (0,'%s','%s');", key, content);
            logger.warn("{}/{}", writeCount, totalCount);

            writer.write(sql);
            writer.write('\n');
            writer.flush();

            writeCount++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
