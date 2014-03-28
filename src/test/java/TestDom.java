import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;

/**
 * Title ：
 * Description :
 * Create Time: 14-3-28 上午11:03
 */
public class TestDom {
    public static void main(String[] args) {
        Document document = new DefaultDocument();
        Namespace namespace = new Namespace("evn","http://www.qq.com");
        Namespace namespace1 = new Namespace("","http://www.qq.com");
        Element root = new DefaultElement(new QName("root",namespace));
        Element e1 = new DefaultElement("e1");
        root.add(e1);
        document.setRootElement(root);
        System.out.println(document.asXML());


        Element root1 = new DefaultElement(new QName("root",namespace1));
        Element e2 = new DefaultElement("e1");

        root1.add(e2);

        document.setRootElement(root1);
        System.out.println(document.asXML());
    }
}
