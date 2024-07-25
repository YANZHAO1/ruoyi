import net.sourceforge.pinyin4j.PinyinHelper;
import org.junit.Test;
import org.springframework.util.StopWatch;

/**
 * 拼音识别
 *
 * @author humorchen
 * @date 2022/9/27 17:28
 */
public class PinyinTest {


    @Test
    public void test() {
        String s = "平安保险2020年-10w";
        for (char c : s.toCharArray()) {
            System.out.println(PinyinHelper.toHanyuPinyinStringArray(c)[0]);
        }
    }

    @Test
    public void testSpeed() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("执行解析拼音");
        String pinyin ="";

            String s = "家喔违心号不好啊";
            for (char c : s.toCharArray()) {
                pinyin += PinyinHelper.toHanyuPinyinStringArray(c)[0];
            }

        stopWatch.stop();
        System.out.println(pinyin);
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}

