import java.io.File;

/**
 * Title ：
 * Description :
 * Create Time: 14-4-4 下午1:22
 */
public class TestFileModify {
    public static void main(String[] args) throws InterruptedException {
        File file = new File("/Users/crazymouse/Desktop/tt.txt");
        System.out.println(file.lastModified());
        Thread.sleep(10000);
        System.out.println(file.lastModified());
    }
}
