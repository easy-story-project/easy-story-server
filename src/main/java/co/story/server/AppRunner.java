package co.story.server;

import cn.wanghaomiao.seimi.core.Seimi;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        Seimi s = new Seimi();
        s.startAll();
    }
}
